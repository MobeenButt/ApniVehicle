package com.example.apnivehicle.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.NotificationAdapter
import com.example.apnivehicle.databinding.ActivityHomeBinding
import com.example.apnivehicle.databinding.CustomToolbarBinding
import com.example.apnivehicle.fragments.*
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.ChatRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.AppNotificationManager
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(),
    NavigationBarView.OnItemSelectedListener,
    AppNotificationManager.NotificationCountListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toolbarBinding: CustomToolbarBinding
    private var priceDropReceiver: com.example.apnivehicle.receivers.PriceDropBroadcastReceiver? = null
    private var chatUnreadListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var offlineSnackbar: Snackbar? = null
    private var notificationPopup: PopupWindow? = null

    // Stable tags for bottom-nav fragments so we can reuse instances
    private val destinationTags: Map<Int, String> = mapOf(
        R.id.nav_home     to "tag_home",
        R.id.nav_inbox    to "tag_inbox",
        R.id.nav_new_cars  to "tag_add_vehicle",
        R.id.nav_bikes     to "tag_my_ads",
        R.id.nav_more      to "tag_settings"
    )

    private val destinationTitles: Map<Int, String> = mapOf(
        R.id.nav_home     to "Home",
        R.id.nav_inbox    to "Inbox",
        R.id.nav_new_cars  to "Add Vehicle",
        R.id.nav_bikes     to "My Ads",
        R.id.nav_more      to "Settings"
    )

    // Guard flag: true while we're programmatically updating the bottom-nav selection
    // (e.g. from syncToolbarTitle) so onNavigationItemSelected doesn't trigger a fragment swap.
    private var suppressNavCallback = false

    private fun createFragmentForNavId(navId: Int): Fragment = when (navId) {
        R.id.nav_home      -> HomeFragment()
        R.id.nav_inbox     -> ChatListFragment()
        R.id.nav_new_cars  -> AddVehicleFragment()
        R.id.nav_bikes     -> MyAdsFragment()
        R.id.nav_more      -> SettingsFragment()
        else               -> HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)
            toolbarBinding = CustomToolbarBinding.bind(binding.root.findViewById(R.id.toolbar_home))

            setupToolbar()
            requestNotificationPermissionIfNeeded()
            registerFcmToken()

            AppNotificationManager.addListener(this)
            try {
                priceDropReceiver = com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.register(this)
            } catch (e: Exception) { android.util.Log.e("MainActivity", "PriceDrop receiver error", e) }

            binding.bottomNavigation.setOnItemSelectedListener(this)
            binding.fabPostAd.setDebouncedClickListener(1000L) {
                // Don't suppress here — we want the nav callback to fire and open the fragment
                binding.bottomNavigation.selectedItemId = R.id.nav_new_cars
            }

            if (savedInstanceState == null) {
                suppressNavCallback = true
                binding.bottomNavigation.selectedItemId = R.id.nav_home
                suppressNavCallback = false
                openNavFragment(R.id.nav_home)
            }

            setupBackNavigation()
            updateNotificationBadge(AppNotificationManager.getNotificationCount(this))
            observeNetwork()
            listenForChatUnread()

            AuthRepository.getCurrentUser()?.id?.let { userId ->
                VehicleRepository.loadSearchPreferencesFromFirestore(userId)
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
        }
    }

    /**
     * Back navigation rules:
     * 1. If the fragment back stack has entries (toolbar-menu fragments pushed with addToBackStack),
     *    pop the top entry — this returns to whatever was showing before.
     * 2. If we're on a non-Home bottom-nav tab, switch back to Home.
     * 3. If we're already on Home with nothing on the stack, let the system finish the activity.
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fm = supportFragmentManager

                // Pop any toolbar-menu fragment that was pushed onto the back stack.
                // Use a commit listener so we update the title AFTER the transaction completes,
                // not before (popBackStack is asynchronous).
                if (fm.backStackEntryCount > 0) {
                    fm.addOnBackStackChangedListener(object : androidx.fragment.app.FragmentManager.OnBackStackChangedListener {
                        override fun onBackStackChanged() {
                            fm.removeOnBackStackChangedListener(this)
                            val current = fm.findFragmentById(R.id.fragment_container)
                            syncToolbarTitle(current)
                        }
                    })
                    fm.popBackStack()
                    return
                }

                // If not on Home, navigate back to Home
                if (binding.bottomNavigation.selectedItemId != R.id.nav_home) {
                    binding.bottomNavigation.selectedItemId = R.id.nav_home
                    openNavFragment(R.id.nav_home)
                    return
                }

                // Already on Home with empty back stack — close the app
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    /** Restore the toolbar title and bottom-nav selection after a back-stack pop. */
    private fun syncToolbarTitle(fragment: Fragment?) {
        if (fragment == null) return
        // Use suppressNavCallback so setting selectedItemId doesn't trigger openNavFragment again
        suppressNavCallback = true
        val title = when (fragment) {
            is HomeFragment           -> { binding.bottomNavigation.selectedItemId = R.id.nav_home; "Home" }
            is ChatListFragment       -> { binding.bottomNavigation.selectedItemId = R.id.nav_inbox; "Inbox" }
            is AddVehicleFragment     -> { binding.bottomNavigation.selectedItemId = R.id.nav_new_cars; "Add Vehicle" }
            is MyAdsFragment          -> { binding.bottomNavigation.selectedItemId = R.id.nav_bikes; "My Ads" }
            is SettingsFragment       -> { binding.bottomNavigation.selectedItemId = R.id.nav_more; "Settings" }
            is SearchFragment         -> "Search"
            is AdvancedSearchFragment -> "Advanced Search"
            is AnalyticsFragment      -> "Analytics"
            is UserProfileFragment    -> "Profile"
            is ComparisonFragment     -> "Compare"
            is ChatListFragment       -> "Messages"
            is SavedSearchesFragment  -> "Saved Searches"
            is ReviewsFragment        -> "Reviews"
            else -> toolbarBinding.toolbarTitle.text.toString()
        }
        suppressNavCallback = false
        toolbarBinding.toolbarTitle.text = title
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            AuthRepository.saveFcmToken(token)
        }
    }

    private fun observeNetwork() {
        NetworkMonitor.isOnline.observe(this) { online ->
            if (!online) {
                offlineSnackbar = Snackbar.make(binding.root, "You are offline", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Dismiss") { offlineSnackbar?.dismiss() }
                offlineSnackbar?.show()
            } else {
                offlineSnackbar?.dismiss()
            }
        }
    }

    private fun listenForChatUnread() {
        val userId = AuthRepository.getCurrentUser()?.id ?: return
        chatUnreadListener = ChatRepository.listenToUnreadCount(userId) { count ->
            runOnUiThread { updateChatBadge(count) }
        }
    }

    private fun updateChatBadge(count: Int) {
        try {
            val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_inbox)
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
            } else {
                badge.isVisible = false
            }
        } catch (_: Exception) {}
    }

    private fun setupToolbar() {
        try {
            toolbarBinding.actionSearch.setOnClickListener { openFragment(SearchFragment(), "Search") }
            toolbarBinding.actionNotifications.setOnClickListener { showNotifications() }
            toolbarBinding.actionMore.setOnClickListener { view -> showMoreMenu(view) }
        } catch (e: Exception) { android.util.Log.e("MainActivity", "Toolbar setup error", e) }
    }

    private fun showMoreMenu(anchor: View) {
        try {
            val popup = PopupMenu(this, anchor)
            popup.menuInflater.inflate(R.menu.menu_toolbar_more, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_advanced_search -> { openFragment(AdvancedSearchFragment(), "Advanced Search"); true }
                    R.id.action_analytics       -> { openFragment(AnalyticsFragment(), "Analytics"); true }
                    R.id.action_favorites       -> { openFragment(FavoriteFragment(), "Favorites"); true }
                    R.id.action_user_profile    -> { openFragment(UserProfileFragment(), "Profile"); true }
                    R.id.action_comparison      -> { openFragment(ComparisonFragment(), "Compare"); true }
                    R.id.action_chat            -> { openFragment(ChatListFragment(), "Messages"); true }
                    R.id.action_saved_searches  -> { openFragment(SavedSearchesFragment(), "Saved Searches"); true }
                    R.id.action_filter -> {
                        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ToolbarActionHandler)?.onToolbarFilter(); true
                    }
                    R.id.action_sort -> {
                        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ToolbarActionHandler)?.onToolbarSort(); true
                    }
                    R.id.action_toggle_layout -> {
                        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ToolbarActionHandler)?.onToolbarToggleLayout(); true
                    }
                    else -> false
                }
            }
            popup.show()
        } catch (e: Exception) { android.util.Log.e("MainActivity", "Menu error", e) }
    }

    private fun showNotifications() {
        // Dismiss any already-open panel
        notificationPopup?.dismiss()

        val anchor = toolbarBinding.actionNotificationsContainer
        val inflater = LayoutInflater.from(this)
        val panelView = inflater.inflate(R.layout.layout_notification_panel, null)

        val recycler = panelView.findViewById<RecyclerView>(R.id.recycler_notifications)
        val emptyLayout = panelView.findViewById<View>(R.id.layout_empty_notifications)
        val btnClearAll = panelView.findViewById<MaterialButton>(R.id.btn_clear_all)

        val notifications = AppNotificationManager.getNotifications(this)

        val adapter = NotificationAdapter { item ->
            AppNotificationManager.removeNotification(this, item.id)
            // Refresh popup with updated list
            showNotifications()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.submitList(notifications.toMutableList())

        if (notifications.isEmpty()) {
            recycler.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recycler.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
        }

        btnClearAll.setOnClickListener {
            AppNotificationManager.clearAllNotifications(this)
            notificationPopup?.dismiss()
        }

        // Measure panel so we can position it correctly
        panelView.measure(
            View.MeasureSpec.makeMeasureSpec(
                resources.displayMetrics.widthPixels - 32,
                View.MeasureSpec.AT_MOST
            ),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val popup = PopupWindow(
            panelView,
            resources.displayMetrics.widthPixels - 48,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true  // focusable — dismisses on outside tap
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            animationStyle = R.style.NotificationPanelAnimation
            elevation = 24f
        }

        notificationPopup = popup

        // Show below the anchor, aligned to its end edge
        val anchorLoc = IntArray(2)
        anchor.getLocationOnScreen(anchorLoc)
        val popupWidth = resources.displayMetrics.widthPixels - 48
        val xOff = anchorLoc[0] + anchor.width - popupWidth - 8

        popup.showAtLocation(
            binding.root,
            Gravity.NO_GRAVITY,
            xOff.coerceAtLeast(8),
            anchorLoc[1] + anchor.height + 4
        )
    }

    private fun updateNotificationBadge(count: Int) {
        try {
            toolbarBinding.notificationBadge.apply {
                visibility = if (count > 0) View.VISIBLE else View.GONE
                text = if (count > 99) "99+" else count.toString()
            }
        } catch (_: Exception) {}
    }

    override fun onNotificationCountChanged(count: Int) {
        runOnUiThread { updateNotificationBadge(count) }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (suppressNavCallback) return true
        if (!destinationTags.containsKey(item.itemId)) return false
        // Clear any toolbar-menu fragments from the back stack when switching tabs
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        openNavFragment(item.itemId)
        return true
    }

    /**
     * Reuse existing fragment instances by tag to preserve scroll position and loaded data.
     * Bottom-nav fragments are NOT added to the back stack — back navigation is handled
     * by setupBackNavigation() above.
     */
    private fun openNavFragment(navId: Int) {
        try {
            val tag = destinationTags[navId] ?: return
            val title = destinationTitles[navId] ?: ""
            toolbarBinding.toolbarTitle.text = title

            val existing = supportFragmentManager.findFragmentByTag(tag)
            val fragment = existing ?: createFragmentForNavId(navId)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
        } catch (e: Exception) { android.util.Log.e("MainActivity", "Fragment error", e) }
    }

    /**
     * Toolbar-menu fragments (Profile, Analytics, Chat, etc.) are pushed onto the back stack
     * so the back button returns to the previous screen instead of closing the app.
     */
    private fun openFragment(fragment: Fragment, title: String) {
        try {
            toolbarBinding.toolbarTitle.text = title
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(title)   // ← key fix: was missing, causing back to close the app
                .commit()
        } catch (e: Exception) { android.util.Log.e("MainActivity", "Fragment error", e) }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            notificationPopup?.dismiss()
            notificationPopup = null
            AppNotificationManager.removeListener(this)
            com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.unregister(this, priceDropReceiver)
            chatUnreadListener?.remove()
            VehicleRepository.stopListening()
        } catch (_: Exception) {}
    }
}

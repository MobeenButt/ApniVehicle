package com.example.apnivehicle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.models.SearchPreference
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.FormatUtils
import java.text.NumberFormat
import java.util.Locale

class SavedSearchesFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var emptyText: TextView? = null
    private lateinit var adapter: SavedSearchAdapter
    private var viewCreatedOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_saved_searches, container, false)
        recyclerView = view.findViewById(R.id.recycler_saved_searches)
        emptyText = view.findViewById(R.id.text_empty_searches)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SavedSearchAdapter(
            onRun = { pref ->
                // Navigate to AdvancedSearchFragment with pre-filled filters
                val fragment = AdvancedSearchFragment.newInstance(pref)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDelete = { pref ->
                VehicleRepository.deleteSearchPreference(pref.id)
                loadPreferences()
                Toast.makeText(requireContext(), "Search deleted", Toast.LENGTH_SHORT).show()
            },
            onToggleAlert = { pref, enabled ->
                val updated = pref.copy(alertEnabled = enabled)
                VehicleRepository.saveSearchPreference(updated)
                val msg = if (enabled) "Alerts enabled for '${pref.name}'" else "Alerts disabled"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter
        viewCreatedOnce = true
        loadPreferences()
    }

    override fun onResume() {
        super.onResume()
        // Guard: only reload on subsequent resumes, not immediately after onViewCreated.
        if (viewCreatedOnce && recyclerView != null) loadPreferences()
    }

    private fun loadPreferences() {
        val prefs = VehicleRepository.getSearchPreferences()
        adapter.submitList(prefs)
        emptyText?.visibility = if (prefs.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewCreatedOnce = false
        recyclerView = null
        emptyText = null
    }

    // ===== Adapter =====

    inner class SavedSearchAdapter(
        private val onRun: (SearchPreference) -> Unit,
        private val onDelete: (SearchPreference) -> Unit,
        private val onToggleAlert: (SearchPreference, Boolean) -> Unit
    ) : RecyclerView.Adapter<SavedSearchAdapter.ViewHolder>() {

        private val priceFormatter = NumberFormat.getNumberInstance(Locale.getDefault())
        private var items: List<SearchPreference> = emptyList()

        fun submitList(list: List<SearchPreference>) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = items.size
                override fun getNewListSize() = list.size
                override fun areItemsTheSame(o: Int, n: Int) = items[o].id == list[n].id
                override fun areContentsTheSame(o: Int, n: Int) = items[o] == list[n]
            })
            items = list
            diff.dispatchUpdatesTo(this)
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tv_search_name)
            val tvSummary: TextView = v.findViewById(R.id.tv_search_summary)
            val tvDate: TextView = v.findViewById(R.id.tv_search_date)
            val btnRun: com.google.android.material.button.MaterialButton = v.findViewById(R.id.btn_run_search)
            val btnDelete: ImageButton = v.findViewById(R.id.btn_delete_search)
            val switchAlert: com.google.android.material.switchmaterial.SwitchMaterial = v.findViewById(R.id.switch_alert)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_search, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pref = items[position]
            holder.tvName.text = pref.name
            holder.tvSummary.text = buildSummary(pref)
            holder.tvDate.text = FormatUtils.getRelativeTime(pref.createdAt)
            holder.switchAlert.isChecked = pref.alertEnabled
            holder.switchAlert.setOnCheckedChangeListener { _, checked ->
                onToggleAlert(pref, checked)
            }
            holder.btnRun.setOnClickListener { onRun(pref) }
            holder.btnDelete.setOnClickListener { onDelete(pref) }
        }

        override fun getItemCount() = items.size

        private fun buildSummary(pref: SearchPreference): String {
            val parts = mutableListOf<String>()
            if (pref.brand.isNotEmpty()) parts.add(pref.brand)
            if (pref.model.isNotEmpty()) parts.add(pref.model)
            if (pref.city.isNotEmpty()) parts.add(pref.city)
            if (pref.minPrice > 0 || pref.maxPrice < 10000000L) {
                parts.add("PKR ${priceFormatter.format(pref.minPrice)} - ${priceFormatter.format(pref.maxPrice)}")
            }
            if (pref.transmission.isNotEmpty()) parts.add(pref.transmission)
            if (pref.fuelType.isNotEmpty()) parts.add(pref.fuelType)
            return if (parts.isEmpty()) "All vehicles" else parts.joinToString(" • ")
        }
    }
}

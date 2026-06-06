package com.example.apnivehicle.utils

import android.view.View
import com.example.apnivehicle.R

/**
 * Debounce/throttle clicks to prevent rapid multiple-click crashes.
 *
 * IMPORTANT: The debounce timestamp is stored as a tag on the View itself, NOT in a
 * global static map. The old global-map approach keyed by View.id caused two bugs:
 *  1. Android recycles View IDs across screens, so a button on Screen A could accidentally
 *     suppress a button on Screen B that happened to get the same generated ID.
 *  2. The map grew unbounded and was never cleared.
 *
 * Storing the timestamp on the View's tag is safe, scoped to each View instance, and
 * is automatically cleaned up when the View is garbage-collected.
 */
object ClickDebounceUtil {

    private const val DEFAULT_DEBOUNCE_TIME = 600L // ms — fast enough to feel responsive

    /**
     * Attach a debounced click listener. Repeated taps within [debounceTime] ms are ignored.
     */
    fun setDebouncedClickListener(
        view: View,
        debounceTime: Long = DEFAULT_DEBOUNCE_TIME,
        action: (View) -> Unit
    ) {
        view.setOnClickListener { v ->
            val now = System.currentTimeMillis()
            // Use the View's own tag to track the last click time — no global state.
            val last = v.getTag(R.id.tag_last_click_time) as? Long ?: 0L
            if (now - last >= debounceTime) {
                v.setTag(R.id.tag_last_click_time, now)
                action(v)
            }
        }
    }
}

/**
 * Extension function for easy debounce on any View.
 */
fun View.setDebouncedClickListener(
    debounceTime: Long = 600L,
    action: (View) -> Unit
) {
    ClickDebounceUtil.setDebouncedClickListener(this, debounceTime, action)
}

/**
 * Temporarily disable a View for [durationMs] ms, then re-enable it.
 * Useful for preventing double-taps on buttons that trigger async work.
 */
fun View.disableTemporarily(durationMs: Long = 800L) {
    isEnabled = false
    postDelayed({ isEnabled = true }, durationMs)
}


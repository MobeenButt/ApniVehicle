package com.example.apnivehicle.utils

interface ToolbarActionHandler {
    fun onToolbarSearch() {}
    fun onSearchQueryChanged(query: String) {}
    fun onToolbarSort() {}
    fun onToolbarToggleLayout() {}
    fun onToolbarFilter() {}
}


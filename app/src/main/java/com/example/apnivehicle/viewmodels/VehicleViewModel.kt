package com.example.apnivehicle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehicleViewModel : ViewModel() {
    
    private val _vehicles = MutableLiveData<List<Vehicle>>()
    val vehicles: LiveData<List<Vehicle>> = _vehicles
    
    private val _favorites = MutableLiveData<List<Vehicle>>()
    val favorites: LiveData<List<Vehicle>> = _favorites
    
    private val _myAds = MutableLiveData<List<Vehicle>>()
    val myAds: LiveData<List<Vehicle>> = _myAds
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadVehicles()
    }
    
    fun loadVehicles() {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val vehicleList = VehicleRepository.getVehicles()
                withContext(Dispatchers.Main) {
                    _vehicles.value = vehicleList
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val favoriteList = VehicleRepository.getFavorites()
                withContext(Dispatchers.Main) {
                    _favorites.value = favoriteList
                }
            }
        }
    }
    
    fun loadMyAds() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myAdsList = VehicleRepository.getMyAds()
                withContext(Dispatchers.Main) {
                    _myAds.value = myAdsList
                }
            }
        }
    }
    
    fun searchVehicles(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val results = VehicleRepository.searchVehicles(query)
                withContext(Dispatchers.Main) {
                    _vehicles.value = results
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun filterVehicles(
        city: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        brand: String? = null,
        year: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val results = VehicleRepository.filterVehicles(city, minPrice, maxPrice, brand, year)
                withContext(Dispatchers.Main) {
                    _vehicles.value = results
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun sortVehicles(sortOption: VehicleRepository.SortOption) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val sorted = VehicleRepository.sortVehicles(sortOption, _vehicles.value ?: emptyList())
                withContext(Dispatchers.Main) {
                    _vehicles.value = sorted
                }
            }
        }
    }
    
    fun toggleFavorite(vehicleId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                VehicleRepository.toggleFavorite(vehicleId)
                withContext(Dispatchers.Main) {
                    loadVehicles()
                    loadFavorites()
                }
            }
        }
    }
    
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                VehicleRepository.addVehicle(vehicle)
                withContext(Dispatchers.Main) {
                    loadVehicles()
                    loadMyAds()
                }
            }
        }
    }
    
    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                VehicleRepository.deleteVehicle(vehicleId)
                withContext(Dispatchers.Main) {
                    loadVehicles()
                    loadMyAds()
                }
            }
        }
    }
}

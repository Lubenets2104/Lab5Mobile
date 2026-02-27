package com.example.flightsearch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.datastore.SearchPreferencesRepository
import com.example.flightsearch.data.local.entity.Airport
import com.example.flightsearch.data.repository.FlightRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlightRoute(
    val departureAirport: Airport,
    val destinationAirport: Airport,
    val isFavorite: Boolean = false
)

sealed interface FlightSearchUiState {
    data class ShowFavorites(
        val favoriteRoutes: List<FlightRoute> = emptyList()
    ) : FlightSearchUiState

    data class ShowSuggestions(
        val suggestions: List<Airport> = emptyList()
    ) : FlightSearchUiState

    data class ShowFlights(
        val selectedAirport: Airport,
        val flights: List<FlightRoute> = emptyList()
    ) : FlightSearchUiState
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FlightSearchViewModel(
    application: Application,
    private val flightRepository: FlightRepository,
    private val searchPreferencesRepository: SearchPreferencesRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAirport = MutableStateFlow<Airport?>(null)

    val uiState: StateFlow<FlightSearchUiState> = combine(
        _searchQuery.debounce(300),
        _selectedAirport,
        flightRepository.getAllFavorites(),
        flightRepository.getAllAirports()
    ) { query, selected, favorites, allAirports ->
        val airportMap = allAirports.associateBy { it.iataCode }

        when {
            selected != null -> {
                val destinations = allAirports.filter { it.iataCode != selected.iataCode }
                val flights = destinations.map { dest ->
                    FlightRoute(
                        departureAirport = selected,
                        destinationAirport = dest,
                        isFavorite = favorites.any {
                            it.departureCode == selected.iataCode &&
                                    it.destinationCode == dest.iataCode
                        }
                    )
                }
                FlightSearchUiState.ShowFlights(
                    selectedAirport = selected,
                    flights = flights
                )
            }

            query.isNotBlank() -> {
                val filtered = allAirports.filter {
                    it.iataCode.startsWith(query, ignoreCase = true) ||
                            it.name.contains(query, ignoreCase = true)
                }.sortedByDescending { it.passengers }
                FlightSearchUiState.ShowSuggestions(suggestions = filtered)
            }

            else -> {
                val favoriteRoutes = favorites.mapNotNull { fav ->
                    val dep = airportMap[fav.departureCode]
                    val dest = airportMap[fav.destinationCode]
                    if (dep != null && dest != null) {
                        FlightRoute(
                            departureAirport = dep,
                            destinationAirport = dest,
                            isFavorite = true
                        )
                    } else null
                }
                FlightSearchUiState.ShowFavorites(favoriteRoutes = favoriteRoutes)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlightSearchUiState.ShowFavorites()
    )

    init {
        viewModelScope.launch {
            val savedQuery = searchPreferencesRepository.searchQuery.first()
            if (savedQuery.isNotBlank()) {
                _searchQuery.value = savedQuery
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _selectedAirport.value = null
        viewModelScope.launch {
            searchPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun onAirportSelected(airport: Airport) {
        _selectedAirport.value = airport
        _searchQuery.value = airport.iataCode
        viewModelScope.launch {
            searchPreferencesRepository.saveSearchQuery(airport.iataCode)
        }
    }

    fun onClearSearch() {
        _searchQuery.value = ""
        _selectedAirport.value = null
        viewModelScope.launch {
            searchPreferencesRepository.saveSearchQuery("")
        }
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            val existing = flightRepository.getFavorite(departureCode, destinationCode).first()
            if (existing != null) {
                flightRepository.removeFavorite(departureCode, destinationCode)
            } else {
                flightRepository.addFavorite(departureCode, destinationCode)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as FlightSearchApplication
                FlightSearchViewModel(
                    application = application,
                    flightRepository = application.flightRepository,
                    searchPreferencesRepository = application.searchPreferencesRepository
                )
            }
        }
    }
}

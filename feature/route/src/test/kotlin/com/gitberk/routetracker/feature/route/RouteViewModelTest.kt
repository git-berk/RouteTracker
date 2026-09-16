package com.gitberk.routetracker.feature.route

import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.testing.MainDispatcherRule
import com.gitberk.routetracker.core.testing.location.FakeLocationTracker
import com.gitberk.routetracker.core.testing.repository.FakeAddressRepository
import com.gitberk.routetracker.core.testing.repository.FakeRouteRepository
import com.gitberk.routetracker.core.testing.repository.FakeTrackingStateRepository
import com.gitberk.routetracker.core.testing.tracking.FakeTrackingController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class RouteViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val routeRepository = FakeRouteRepository()
    private val trackingStateRepository = FakeTrackingStateRepository()
    private val addressRepository = FakeAddressRepository()

    private lateinit var viewModel: RouteViewModel

    @Before
    fun setUp() {
        viewModel = RouteViewModel(
            routeRepository = routeRepository,
            trackingStateRepository = trackingStateRepository,
            addressRepository = addressRepository,
            trackingController = FakeTrackingController(trackingStateRepository),
            locationTracker = FakeLocationTracker(),
        )
    }

    @Test
    fun uiState_reflectsStoredRouteAndTrackingState() = runTest {
        collectUiState()
        routeRepository.addMarker(point())

        viewModel.startTracking()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.markers.size)
        assertTrue(state.isTracking)
    }

    @Test
    fun stopTracking_clearsTrackingState() = runTest {
        collectUiState()
        viewModel.startTracking()

        viewModel.stopTracking()

        assertFalse(viewModel.uiState.value.isTracking)
    }

    @Test
    fun selectMarker_showsResolvedAddress() = runTest {
        collectUiState()
        routeRepository.addMarker(point())
        addressRepository.address = "İstiklal Cd., Beyoğlu/İstanbul"

        viewModel.selectMarker(routeRepository.currentMarkers.single())

        val selected = viewModel.uiState.value.selectedMarker!!
        assertEquals(1, selected.number)
        assertEquals(AddressState.Loaded("İstiklal Cd., Beyoğlu/İstanbul"), selected.address)
    }

    @Test
    fun selectMarker_withoutKnownAddress_showsNotFound() = runTest {
        collectUiState()
        routeRepository.addMarker(point())

        viewModel.selectMarker(routeRepository.currentMarkers.single())

        assertEquals(AddressState.NotFound, viewModel.uiState.value.selectedMarker?.address)
    }

    @Test
    fun addressFailure_canBeRetried() = runTest {
        collectUiState()
        routeRepository.addMarker(point())
        addressRepository.failure = IOException("offline")
        viewModel.selectMarker(routeRepository.currentMarkers.single())
        assertEquals(AddressState.Failed, viewModel.uiState.value.selectedMarker?.address)

        addressRepository.failure = null
        addressRepository.address = "Kadıköy/İstanbul"
        viewModel.retryAddress()

        assertEquals(AddressState.Loaded("Kadıköy/İstanbul"), viewModel.uiState.value.selectedMarker?.address)
        assertEquals(2, addressRepository.requestCount)
    }

    @Test
    fun resetRoute_removesMarkersAndClosesSelection() = runTest {
        collectUiState()
        routeRepository.addMarker(point())
        viewModel.selectMarker(routeRepository.currentMarkers.single())

        viewModel.resetRoute()

        assertTrue(viewModel.uiState.value.markers.isEmpty())
        assertNull(viewModel.uiState.value.selectedMarker)
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    private fun point() = LocationPoint(latitude = 41.0082, longitude = 28.9784, accuracyMeters = 5f, timestampMillis = 0L)
}

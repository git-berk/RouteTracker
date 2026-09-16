# Implementation plan

This plan was written with Claude Code before any code, then updated as decisions were made.
Where the implementation ended up differing, a note says so.

## Basics

| | |
|---|---|
| Package | `com.gitberk.routetracker` |
| minSdk / targetSdk | 26 / 36 |
| Stack | Kotlin, Jetpack Compose (Material 3), Hilt, Room, DataStore, Play Services Location, Maps Compose, Coroutines/Flow |
| Build | Version catalog + `build-logic` convention plugins |
| Maps key | `MAPS_API_KEY` from `local.properties` or the environment, injected as a manifest placeholder |

## Modules

```
app                  MainActivity, Application, manifest
feature/route        RouteScreen + RouteViewModel
core/model           LocationPoint, RouteMarker (pure Kotlin)
core/common          dispatchers, application scope, haversine distance
core/database        Room: MarkerEntity, MarkerDao, RouteDatabase
core/datastore       isTracking flag
core/location        FusedLocationProvider as Flow, Geocoder
core/data            RouteRepository, TrackingStateRepository, AddressRepository
core/domain          RecordLocationUseCase
core/maps            cached pin icon, RouteMap composable, camera helpers
core/designsystem    theme, overlay buttons, icons
core/tracking        LocationTrackingService, notification, TrackingController
core/testing         fakes shared by unit tests (added during implementation)
```

## Behavior

### 100 m rule (`RecordLocationUseCase`)
- Compare each fix with the **last stored marker**, not the previous fix.
- **Decision (after testing on the emulator):** pins landed 125–190 m apart at speed, because a fix
  only arrives every 5 s. Pins are now placed at the exact 100 m point on the line between the
  previous fix and the new one, and a long gap gets several pins.
- Ignore fixes with accuracy worse than 50 m.
- The first accurate fix on an empty route becomes the start marker.

### Location request
- High accuracy, ~5 s interval, `minUpdateDistanceMeters = 20`.
- **Decision (after testing on the emulator):** pins appeared up to one interval after the user
  passed the 100 m point. Changed to a 1 s interval and `minUpdateDistanceMeters = 5`; battery tuning
  comes later.

### Single source of truth
- The service writes markers to Room, and the UI observes Room only. A reopened app shows the stored
  route until it is reset.

## Foreground service
- `foregroundServiceType="location"`, ongoing notification with the marker count and a Stop action.
- `START_STICKY`, and it survives removal from recents.
- `TrackingController.start()/stop()` starts or stops the service and persists `isTracking`.
- **Decision:** restart on app open only. If `isTracking` is true when the app comes to the
  foreground, the service is started again. No boot receiver, no background location permission.
- **Decision:** no battery optimization exemption for now. Battery tuning may come later.

## Map and markers
- **Decision:** use a location pin (Material `location_on`) for every marker, including the start.
- One `BitmapDescriptor` is created lazily and cached process-wide, then shared by all markers.
- **Decision:** draw a polyline connecting the markers.
- The camera frames the whole route on first load and follows new markers while tracking.

## Address on marker tap
- Bottom sheet with address, coordinates and time.
- Resolved on first tap, then cached in Room's nullable `address` column.
- Loading, not-found and error-with-retry states.

## Screen
- Full-screen edge-to-edge map, a status chip at the top, a my-location button, Reset (with
  confirmation) and Start/Stop at the bottom.
- Start asks for location permission, plus notification permission on 33+. Only precise location
  is accepted, and a denial offers a shortcut to app settings.

## Tests
- Haversine distance, `RecordLocationUseCase`, `RouteViewModel` (fakes + Turbine/coroutines-test).

## Commit sequence
Small commits, one layer at a time: project setup → build logic → model → common → database →
datastore → location → geocoder → repositories → use case → tests → design system → maps →
service → notification → route screen → app → controls → permissions → address sheet →
resume on launch → ViewModel tests → docs.

# RouteTracker

An Android app that records the user's route. It drops a pin on the map every 100 meters, keeps
tracking in the background, and shows the address of a pin when it is tapped.

## Features

- **100 m markers.** Each accurate location fix is compared with the last saved marker. When the
  user has moved 100 m or more, a new pin is added. The first accurate fix becomes the start pin.
- **Background tracking.** Tracking runs in a foreground service (`foregroundServiceType="location"`)
  with an ongoing notification that shows the marker count and has a Stop action.
- **Address on tap.** Tapping a pin opens a bottom sheet with its address, coordinates and time.
  The address is geocoded on first tap and cached in the database.
- **Start / stop / reset.** Buttons over the map control tracking. Reset asks for confirmation.
- **Persistent route.** Markers live in Room and are shown again on relaunch until the route is reset.
- **Route line.** A polyline connects the pins in the order they were recorded.

## Setup

1. Get a Google Maps SDK for Android API key.
2. Add it to `local.properties` (not committed):
   ```properties
   MAPS_API_KEY=your_key_here
   ```
   `MAPS_API_KEY` as an environment variable also works.
3. Open the project in Android Studio and run the `app` configuration.

Requirements: JDK 17+ (Android Studio's bundled JDK works), Android SDK 36, a device or emulator with
Google Play services. Min SDK is 26.

## Architecture

The app is split into a feature module and focused core modules. Build configuration is shared
through convention plugins in `build-logic`.

```
app                  Application, MainActivity, Maps API key
feature/route        The single screen: map, controls, marker sheet, permissions, RouteViewModel
core/model           Plain Kotlin models (LocationPoint, RouteMarker)
core/common          Coroutine dispatchers/scopes, haversine distance
core/database        Room: markers table
core/datastore       DataStore: "is tracking" flag
core/location        FusedLocationProvider as a Flow, Geocoder address lookup
core/data            RouteRepository, TrackingStateRepository, AddressRepository
core/domain          RecordLocationUseCase (the 100 m rule)
core/maps            RouteMap composable, cached pin icon, camera helpers
core/designsystem    Theme, icons, map overlay buttons
core/tracking        LocationTrackingService, notification, TrackingController
core/testing         Fakes and test rules shared by unit tests
```

```
app ──► feature/route ──► core/maps, core/tracking, core/data, core/location, core/designsystem
core/tracking ──► core/domain ──► core/data ──► core/database, core/datastore, core/location
```

### Data flow

```
FusedLocationProvider ──► LocationTrackingService ──► RecordLocationUseCase ──► Room
                                                                                  │
                                            RouteScreen ◄── RouteViewModel ◄──────┘
```

The service and the UI never talk to each other directly. The service writes markers to Room, and the
screen observes Room. So the UI always shows the real route, whether the service is running, was
restarted, or the app process is brand new.

### Notable decisions

- **Distance is measured from the last marker, not the previous fix.** When the user moves slowly,
  small steps still add up to a marker.
- **Fixes with accuracy worse than 50 m are ignored.** Otherwise GPS jitter while standing still
  can place fake markers.
- **Only precise location is accepted.** Approximate location is kilometers off and would never pass
  the accuracy filter, so tracking would silently record nothing.
- **Location requests use a 20 m minimum update distance.** It is well below the 100 m spacing, so
  markers aren't placed late, and the provider can still skip updates when the user doesn't move.
- **The pin icon is rasterized once.** `LocationPinIcon` caches a single `BitmapDescriptor` that
  every marker shares. It is created lazily because the Maps SDK has to be initialized first.
- **Addresses are resolved when a pin is tapped** and stored in Room, so each marker is geocoded at
  most once, and markers that are never opened cost nothing.

### How long tracking survives

- The foreground service keeps receiving updates with the app in the background or removed from
  recents. It returns `START_STICKY`.
- The "tracking on" flag is stored in DataStore. If the system kills the service, it is started again
  the next time the app comes to the foreground (`MainActivity.onStart`).
- A sticky restart while the app is in the background can be refused by Android, because location
  foreground services can't start from the background without background location permission. That
  case is caught, and tracking resumes when the app is opened.
- Not covered on purpose: restarting after a reboot, and asking to be excluded from battery
  optimization. Both need extra permissions. Force stop and aggressive battery killers on some
  devices will still end tracking.

## Tests

```bash
./gradlew testDebugUnitTest
```

- `DistanceTest`: haversine distances.
- `RecordLocationUseCaseTest`: start marker, the 100 m threshold, measuring from the last marker,
  the accuracy filter.
- `RouteViewModelTest`: UI state, start/stop, address loading/not found/failure and retry, reset.

## AI usage

The app was built with Claude Code. As the case requires, the helper files are in the repository:

- [`CLAUDE.md`](CLAUDE.md): project instructions for the assistant.
- [`docs/ai/PLAN.md`](docs/ai/PLAN.md): the implementation plan agreed on before coding.

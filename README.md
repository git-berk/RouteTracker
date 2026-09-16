# RouteTracker

An Android app that records the user's route. It drops a pin on the map every 100 meters, keeps
tracking in the background, and shows the address of a pin when it is tapped.

## Features

- **100 m markers.** Pins are placed exactly 100 m apart, measured in a straight line from the
  previous pin. The first accurate fix becomes the start pin.
- **Background tracking.** Tracking runs in a foreground service (`foregroundServiceType="location"`)
  with an ongoing notification that shows the marker count and has a Stop action.
- **Address on tap.** Tapping a pin opens a bottom sheet with its address, coordinates and time.
  The address is geocoded on first tap and cached in the database.
- **Start / stop / reset.** On first launch the app asks for location and opens zoomed in on the
  user. Tracking starts only when the user taps Start. Reset asks for confirmation.
- **Persistent route.** Markers live in Room and are shown again on relaunch until the route is reset.
- **Route line.** A polyline connects the pins in the order they were recorded.

## Setup

Open the project in Android Studio and run the `app` configuration. No extra setup is needed.

A Google Maps API key is bundled in `gradle.properties` so the map works right away. It is restricted
to Maps SDK for Android, the `com.gitberk.routetracker` package and the SHA-1 of the debug keystore
committed in `keystore/`. That's why debug builds sign with that keystore instead of your own. The
key is for reviewing this project only.

To use your own key, set `MAPS_API_KEY=...` in `local.properties` or as an environment variable.
Either one overrides the bundled key.

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

- **Markers are placed at the exact 100 m point.** Fixes arrive every few seconds, so the user is
  usually already past 100 m when one comes in. The pin goes where the line between the previous fix
  and the new one is exactly 100 m from the last pin, and a long gap gets several pins. The spacing
  stays exact at any speed or update interval. On a sharp turn a pin can sit slightly off the road,
  because it lies on the straight line between two fixes.
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

## Manual testing on the emulator

[`docs/testing/istanbul-route.gpx`](docs/testing/istanbul-route.gpx) is a 3 km ride from Taksim
along İstiklal, past Galata Tower and over Galata Bridge to Eminönü. It has one fix every 5 s at
bike speed (~5 m/s), so it takes about 10 minutes at 1x.

1. Set a location in the emulator (Extended controls → Location), run the app, and allow precise
   location and notifications. The map zooms in to that location. Tap **Start**.
2. Open the emulator's **Extended controls (⋯) → Location → Routes**, choose **Import GPX/KML**,
   pick the file, and press **Play route**.
3. While it plays:
   - Pins appear along the route, connected by a line, and the camera follows the newest one.
   - Press Home: the notification keeps counting markers, so tracking continues in the background.
   - Tap a pin to see its address.
4. Kill the app from recents or with `adb shell am force-stop com.gitberk.routetracker`, then reopen
   it. The route is still there and tracking resumes.
5. Tap **Stop**, then **Reset**, and confirm. The route is cleared.

Pins stay 100 m apart at any playback speed, because each one is placed at the exact 100 m point
between two fixes.

## Tests

```bash
./gradlew testDebugUnitTest
```

- `DistanceTest`: haversine distances.
- `RecordLocationUseCaseTest`: start marker, exact 100 m placement, several pins from one long gap,
  turning paths, interpolated times, the accuracy filter, reset.
- `RouteViewModelTest`: UI state, start/stop, address loading/not found/failure and retry, reset.

## AI usage

The app was built with Claude Code. As the case requires, the helper files are in the repository:

- [`CLAUDE.md`](CLAUDE.md): project instructions for the assistant.
- [`docs/ai/PLAN.md`](docs/ai/PLAN.md): the implementation plan agreed on before coding.

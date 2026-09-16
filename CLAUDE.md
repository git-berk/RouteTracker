# CLAUDE.md

Guidance for AI assistants working in this repository.

## Project

RouteTracker is a single-screen Android app built for a case study. It tracks the user's location,
places a pin every 100 m, and keeps tracking in a foreground service. See `README.md` for features
and architecture, and `docs/ai/PLAN.md` for the original plan.

## Build and test

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :app:lintDebug
```

Use a JDK 17-21. Android Studio's bundled JBR works. A Maps API key goes in `local.properties` as
`MAPS_API_KEY`.

## Conventions

- Modules follow the Now in Android layout. Put build config in the `build-logic` convention plugins,
  not in module build files.
- The service writes to Room and the UI reads from Room. Don't add direct service ↔ UI communication.
- Keep the `core/model` module free of Android dependencies.
- Comments explain *why*, never restate *what* the code does.
- Commit messages are short and imperative, with no trailers or AI attribution.
- Add unit tests with the fakes in `core/testing` for any logic in domain, data, or ViewModels.

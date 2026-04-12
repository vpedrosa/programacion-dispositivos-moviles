# Smart Home KMP - Architecture Guide

## Architecture: Hexagonal with Vertical Slicing

This project follows **Hexagonal Architecture (Ports & Adapters)** organized by **vertical slices** (feature-based).

### Package Structure

```
com.vpedrosa.smarthome/
├── <feature>/                 # One folder per vertical slice
│   ├── domain/
│   │   ├── model/             # Feature-specific entities and value objects
│   │   ├── <Port>.kt          # Port interfaces at domain root (NOT in ports/)
│   │   └── <Repository>.kt   # Repository interfaces at domain root
│   ├── application/           # Use cases (one class per use case)
│   └── infrastructure/
│       ├── persistence/       # Repository implementations
│       ├── <adapter>/         # Other adapter implementations
│       └── (NO ui here)
│
├── ui/                        # ALL screens/ViewModels, organized by feature
│   ├── components/            # Shared reusable composables
│   ├── dashboard/
│   ├── device/
│   ├── room/
│   ├── commissioning/
│   ├── voice/
│   ├── event/
│   ├── settings/
│   └── antisquatter/
│
├── navigation/                # Screen routes & NavHost
├── di/                        # Koin DI modules
└── App.kt
```

### Current Vertical Slices

| Slice | Description |
|-------|-------------|
| `device` | Core domain (Device, DeviceId, RoomId, Color, DeviceEvent…), ports, use cases, infrastructure |
| `room` | Room management (Room, RoomId lives in device/domain/model) |
| `commissioning` | Device discovery & pairing (Matter protocol) |
| `voice` | Voice command parsing & execution |
| `event` | Device events, notifications, sensor simulation |
| `settings` | App configuration (alerts, preferences) |
| `antisquatter` | Presence simulation (anti-squatter) |

### Key Conventions

1. **Layer naming**: `domain/`, `application/`, `infrastructure/` (NOT adapters, NOT usecases)
2. **No `ports/` subfolder**: Port interfaces go at the root of `domain/`
3. **Models in `model/`**: Domain entities and value objects go in `domain/model/`
4. **DTOs in `dto/`**: Data transfer objects go in `domain/dto/`
5. **Repositories at domain root**: Repository interfaces go directly in `domain/` (or `repositories/` if many)
6. **UI is separate**: Screens and ViewModels live in `ui/<feature>/`, NOT inside infrastructure
7. **`device` is the canonical domain slice**: `Device`, `DeviceId`, `RoomId`, `Color`, ports and repositories live in `device/domain/`. Other slices import from there.
8. **Each slice only creates layers it needs** (no empty folders)
9. **Use cases**: One class per use case, named `<Verb><Noun>UseCase`, using `operator fun invoke()`
10. **Android-specific adapters**: Follow the same slice structure under `androidMain/`

### When Creating a New Feature

1. Create the vertical slice folder: `<feature>/`
2. Add domain models in `<feature>/domain/model/`
3. Define ports/repositories at `<feature>/domain/` root
4. Implement use cases in `<feature>/application/`
5. Add adapter implementations in `<feature>/infrastructure/<adapter-type>/`
6. Add UI screens in `ui/<feature>/`
7. Register DI bindings in `di/`
8. If domain entities are needed by 3+ other slices, move them to `device/domain/model/`

### Testing

Tests mirror the slice structure under `commonTest/`:
```
commonTest/kotlin/com/vpedrosa/smarthome/
├── device/          # Tests for device domain, repos and use cases
├── voice/           # Tests for voice command parsing/execution
├── event/           # Tests for event use cases
└── antisquatter/    # Tests for presence simulation
```

### Tech Stack

- **KMP** (Kotlin Multiplatform) with Compose Multiplatform
- **Koin** for dependency injection
- **Kotlin Flows** for reactive state
- **Matter protocol** for device communication (Android)
- **Inline value classes** for type-safe IDs (DeviceId, RoomId)

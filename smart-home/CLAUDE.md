# Smart Home KMP - Architecture Guide

## Architecture: Hexagonal with Vertical Slicing

This project follows **Hexagonal Architecture (Ports & Adapters)** organized by **vertical slices** (feature-based).

### Package Structure

```
com.vpedrosa.smarthome/
├── shared/                    # Shared kernel (cross-cutting domain used by 3+ slices)
│   ├── domain/
│   │   ├── model/             # Entities, value objects (Device, Room, DeviceType, Color...)
│   │   ├── dto/               # Data transfer objects (DeviceDto, RoomDto...)
│   │   ├── DeviceRepository.kt
│   │   ├── RoomRepository.kt
│   │   ├── DeviceEventRepository.kt
│   │   └── DeviceControlPort.kt
│   └── infrastructure/
│       ├── persistence/       # InMemory repository implementations
│       └── matter/            # Matter protocol adapters (androidMain)
│
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
| `shared` | Core domain entities (Device types, Room, DeviceEvent) and ports used by 3+ slices |
| `device` | Device control use cases (toggle, update light/blind/thermostat, bulk toggle) |
| `room` | Room management (no domain/application - entities in shared) |
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
7. **Shared kernel**: Entities used by 3+ slices go to `shared/domain/model/`
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
8. If domain entities are needed by 3+ other slices, move them to `shared/domain/model/`

### Testing

Tests mirror the slice structure under `commonTest/`:
```
commonTest/kotlin/com/vpedrosa/smarthome/
├── shared/          # Tests for shared repos and domain logic
├── device/          # Tests for device use cases
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

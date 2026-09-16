# AGENTS Guidelines for This Repository

This repository contains an Android application project. When working on the project interactively with an AI coding agent, please follow the guidelines below to ensure architectural consistency, maximum performance, and a smooth development experience.

## 1. Project Specifications
- **Minimum SDK:** 24 (or defined by project)
- **Target SDK:** 37
- **Language:** Kotlin (2.0+)
- **Build System:** Gradle (Kotlin DSL preferred)

## 2. Architecture & Design Patterns
We follow the official Android Architecture Guidelines. Each major feature or business domain must have its own dedicated module:
- **Presentation Layer:** StateFlow/SharedFlow in ViewModels. UI defined in Jetpack Compose (or XML if legacy). Unidirectional Data Flow (UDF).
- **Domain Layer:** Optional UseCases for complex business logic.
- **Data Layer:** Repository pattern to abstract data sources (Room for local, Retrofit for remote).
- **Dependency Injection:** Hilt/Dagger (preferred).

## 3. Multi-module architecture
The project **must use a multi-module architecture with clear feature-based separation**.
- **The project must be divided into multiple Gradle modules rather than placing all functionality inside a single `app` module.
- **Each major feature or business domain must have its own dedicated feature module.
- **Feature modules must be as independent as reasonably possible and should not contain code belonging to other features.
- **Shared functionality must be extracted into appropriate `core`, `common`, or infrastructure modules instead of being duplicated across features.
- **Dependencies between modules must follow a clear and intentional direction. Avoid unnecessary circular dependencies and excessive coupling.
- **The `app` module should primarily act as the application entry point and composition root, not as a container for business logic or feature implementation.
- **When a feature requires separation between its public API and implementation, use dedicated `api` and `impl` modules (for example, `feature:profile:api` and `feature:profile:impl`).
- **New features must follow the same modular structure instead of introducing feature-specific code into unrelated modules.
- **The agent must **not collapse the architecture into a single module for convenience**. Any deviation from the multi-module, feature-based architecture must be explicitly justified.

## 4. Asynchronous Programming
- **Concurrency:** Kotlin Coroutines exclusively. Avoid RxJava for new code.
- **Dispatchers:** Inject Dispatchers (don't hardcode `Dispatchers.IO`) to allow testing.

## 5. UI Framework
- **Jetpack Compose:** Default choice for all new features. Follow Compose best practices (state hoisting, modifiers, no side-effects in composables).
- **Navigation:** Jetpack Navigation 3 Compose.

## 6. Testing Philosophy
- **Unit Tests:** JUnit4/JUnit5, MockK for mocking, Turbine for Flow testing.
- **UI Tests:** Compose Test Rule for UI components, Espresso for legacy XML.
- Prefer testing ViewModel state emission over testing implementation details.

## 7. External Documentation
- When asked to implement a functionality that you are not sure of, refer to the official [Android Developer Documentation](https://developer.android.com) or [Kotlin Documentation](https://kotlinlang.org) for additional context and best practices.

## 8. Useful Agent Skills Recap

| Skill Folder          | Purpose                                            |
| --------------------- | -------------------------------------------------- |
| `architecture/`       | Clean architecture, ViewModels, and Data Layer.    |
| `ui/`                 | Jetpack Compose best practices, Coil, Accessibility. |
| `performance/`        | Auditing Compose and Gradle build performance.     |
| `concurrency_and_networking/` | Coroutines fixes, Retrofit networking.             |

---

Following these practices ensures that the agent-assisted development workflow stays reliable and consistent. When in doubt, always refer to the specific agent skills provided in `.github/skills/` for deeper task-specific context!

*Note to developers: Update this file whenever the project makes architectural shifts to ensure AI agents stay aligned with your conventions.*
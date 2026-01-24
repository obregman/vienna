# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vienna is a stock recommendation and tracking Android app built with Kotlin and Jetpack Compose. It provides virtual portfolio management and AI-powered stock analysis using Claude API.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Clean build
./gradlew clean build
```

## Architecture

The app follows **MVVM with Clean Architecture** in three layers:

```
Presentation → Domain → Data
```

### Layer Responsibilities

- **Presentation** (`presentation/`): Compose screens, ViewModels, UI state. Each screen feature has its own package with Screen and ViewModel.
- **Domain** (`domain/`): Business logic use cases, domain models, repository interfaces. Pure Kotlin with no Android dependencies.
- **Data** (`data/`): Repository implementations, Room database, Retrofit APIs, DataStore preferences.

### Key Patterns

- **Dependency Injection**: Hilt modules in `di/` provide singletons for database, network, and repositories
- **Repository Pattern**: Domain defines interfaces (`domain/repository/`), data layer implements them (`data/repository/`)
- **Use Cases**: Single-responsibility classes in `domain/usecase/` orchestrate repository calls
- **UI State**: ViewModels expose state via StateFlow, screens collect and render

### Navigation

Bottom navigation with three tabs: Stocks (home), Portfolio, Settings. Navigation routes defined in `presentation/navigation/NavRoutes.kt`. Stock detail and analysis screens are push destinations.

## External APIs

- **Alpha Vantage** (`StockApi`): Stock quotes, market movers, symbol search. Base URL: `https://www.alphavantage.co/`
- **Claude API** (`ClaudeApi`): AI-powered stock analysis. Base URL: `https://api.anthropic.com/`

API keys are stored in DataStore preferences (`SettingsDataStore`) and configured by users in the Settings screen at runtime.

## Database

Room database (`ViennaDatabase`) with tables:
- `portfolio_holdings` - User's virtual portfolio positions
- `cached_stocks` - Cached stock quotes (1-minute TTL)
- `search_history` - Recent search queries
- `analysis_cache` - Cached AI analysis (6-hour TTL)
- `error_log` - Error tracking for debugging

## Key Dependencies

Managed via Gradle Version Catalog (`gradle/libs.versions.toml`):
- Compose BOM for UI
- Hilt for DI with KSP annotation processing
- Retrofit + OkHttp + Kotlin Serialization for networking
- Room for local persistence
- DataStore for preferences

# Vienna

A stock recommendation and tracking Android app built with Kotlin and Jetpack Compose.

## Features

- **Stock List**: View top gainers, losers, and most actively traded stocks from NASDAQ
- **Stock Search**: Search for stocks by ticker symbol or company name
- **Virtual Portfolio**: Track your stock investments without real money
- **Portfolio Performance**: View gain/loss calculations and overall performance
- **AI-Powered Analysis**: Get AI-generated stock analysis powered by Claude
- **In-App Settings**: Configure API keys directly in the app

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp + Kotlin Serialization
- **Local Database**: Room
- **Preferences**: DataStore
- **Async Operations**: Kotlin Coroutines + Flow
- **Navigation**: Compose Navigation

## Project Structure

```
app/
├── data/
│   ├── local/          # Room database, DataStore, DAOs, entities
│   ├── remote/         # Retrofit APIs, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
├── presentation/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   ├── screens/        # Feature screens with ViewModels
│   └── theme/          # Material theme configuration
└── di/                 # Hilt dependency injection modules
```

## Setup

1. Clone the repository
2. Open in Android Studio and sync Gradle
3. Run on an emulator or device (Android 8.0+)
4. Go to **Settings** (bottom navigation) and enter your API keys

## API Keys

The app requires two API keys, which you can enter in the **Settings** screen:

- **Alpha Vantage**: Free stock market data API
  - Get your free key at [alphavantage.co](https://www.alphavantage.co/support/#api-key)

- **Claude API**: AI-powered stock analysis
  - Get your key at [console.anthropic.com](https://console.anthropic.com/)

Your API keys are stored securely on your device using Android DataStore and are never shared.

## Requirements

- Android 8.0 (API 26) or higher
- Internet connection for real-time data and AI features

## License

This project is for educational purposes.

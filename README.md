# Vienna

A stock recommendation and tracking Android app built with Kotlin and Jetpack Compose.

## Features

- **Stock List**: View top gainers, losers, and most actively traded stocks from NASDAQ
- **Stock Search**: Search for stocks by ticker symbol or company name
- **Virtual Portfolio**: Track your stock investments without real money
- **Portfolio Performance**: View gain/loss calculations and overall performance
- **AI-Powered Analysis**: Get AI-generated stock analysis powered by Claude

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp + Kotlin Serialization
- **Local Database**: Room
- **Async Operations**: Kotlin Coroutines + Flow
- **Navigation**: Compose Navigation

## Project Structure

```
app/
├── data/
│   ├── local/          # Room database, DAOs, entities
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
2. Copy `local.properties.example` to `local.properties`
3. Add your API keys:
   - Get a free Alpha Vantage API key from [alphavantage.co](https://www.alphavantage.co/support/#api-key)
   - Get a Claude API key from [console.anthropic.com](https://console.anthropic.com/)
4. Open in Android Studio and sync Gradle
5. Run on an emulator or device (Android 8.0+)

## API Keys

The app requires two API keys:

- **Alpha Vantage**: Free stock market data API
- **Claude API**: AI-powered stock analysis

Add these to your `local.properties` file:

```properties
ALPHA_VANTAGE_API_KEY=your_key_here
CLAUDE_API_KEY=your_key_here
```

## Requirements

- Android 8.0 (API 26) or higher
- Internet connection for real-time data and AI features

## License

This project is for educational purposes.

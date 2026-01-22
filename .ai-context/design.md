# Vienna - Stock Recommendation & Tracking App

## High-Level Design Document

**Platform:** Android (Kotlin)  
**Version:** 1.0  

---

## 1. Requirements

### 1.1 Functional Requirements

#### FR-1: Stock List Display
- Display a scrollable list of NASDAQ stocks with key information (symbol, company name, current price, price change, volume)
- Support sorting options: by trading volume (high to low), by price increase (percentage gain), and by price decrease (percentage loss)
- Pull-to-refresh functionality for updating stock data
- Pagination or infinite scroll for handling large stock lists

#### FR-2: Stock Search
- Real-time search by ticker symbol or company name
- Display search results as user types (debounced input)
- Show recent search history for quick access
- Handle no-results scenarios gracefully

#### FR-3: Virtual Portfolio Management
- Allow users to add any stock to their virtual portfolio
- Record the "purchase" price and timestamp when adding
- Support adding the same stock multiple times (multiple positions)
- Enable removing stocks from the portfolio
- Persist portfolio data locally across app sessions

#### FR-4: Portfolio Performance Tracking
- Display each holding's current value vs. purchase price
- Calculate and show: absolute gain/loss ($), percentage gain/loss (%), holding period (days)
- Aggregate portfolio summary: total value, total gain/loss, overall return percentage
- Visual indicators (green/red) for positive/negative performance

#### FR-5: AI-Powered Stock Analysis
- On-demand AI summary generation for any stock via Claude API
- Summary includes: company overview, recent performance analysis, key metrics interpretation, and risk factors
- Fetch and display recent news articles related to the selected stock
- AI-generated sentiment analysis based on news content
- Loading states and error handling for AI operations

### 1.2 Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| **Performance** | Stock list loads within 2 seconds; UI remains responsive during API calls |
| **Reliability** | Graceful degradation when APIs are unavailable; offline access to cached data |
| **Security** | Secure storage of API keys; no sensitive user data transmitted unnecessarily |
| **Usability** | Intuitive navigation; consistent Material Design 3 patterns |
| **Scalability** | Handle portfolios of up to 100 stocks without performance degradation |
| **Compatibility** | Support Android 8.0 (API 26) and above |

### 1.3 Constraints

- Must use a free stock quotes API (rate limits apply)
- Claude API calls incur costs; implement caching to minimize redundant requests
- No real money transactions; this is purely a tracking/simulation app
- Internet connection required for real-time data and AI features

---

## 2. Technical Architecture

### 2.1 Architecture Pattern

Vienna follows **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles, organized into three layers:

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │   Compose   │  │  ViewModels │  │    UI State     │  │
│  │   Screens   │  │             │  │    Classes      │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  Use Cases  │  │   Domain    │  │   Repository    │  │
│  │             │  │   Models    │  │   Interfaces    │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                      DATA LAYER                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ Repository  │  │   Remote    │  │     Local       │  │
│  │   Impls     │  │   (APIs)    │  │   (Room DB)     │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9+ |
| UI Framework | Jetpack Compose with Material 3 |
| Navigation | Compose Navigation |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp + Kotlin Serialization |
| Local Database | Room |
| Async Operations | Kotlin Coroutines + Flow |
| Image Loading | Coil |
| Build System | Gradle with Version Catalogs |

### 2.3 Module Structure

```
app/
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── ViennaDatabase.kt
│   │   │   ├── dao/
│   │   │   └── entities/
│   │   └── datastore/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── StockApi.kt
│   │   │   └── ClaudeApi.kt
│   │   └── dto/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── navigation/
│   ├── screens/
│   │   ├── stocklist/
│   │   ├── search/
│   │   ├── portfolio/
│   │   ├── stockdetail/
│   │   └── analysis/
│   ├── components/
│   └── theme/
└── di/
```

---

## 3. Data Models

### 3.1 Domain Models

**Stock**
```
Stock {
    symbol: String          // "AAPL"
    companyName: String     // "Apple Inc."
    currentPrice: Double    // 178.50
    priceChange: Double     // +2.35
    percentChange: Double   // +1.33
    volume: Long            // 52,340,000
    marketCap: Long?        // 2,800,000,000,000
    dayHigh: Double
    dayLow: Double
    lastUpdated: Timestamp
}
```

**PortfolioHolding**
```
PortfolioHolding {
    id: Long (auto-generated)
    symbol: String
    companyName: String
    purchasePrice: Double
    purchaseDate: Timestamp
    shares: Int             // Default: 1 for simplicity
}
```

**StockAnalysis**
```
StockAnalysis {
    symbol: String
    summary: String
    sentiment: Sentiment    // BULLISH, BEARISH, NEUTRAL
    keyPoints: List<String>
    newsArticles: List<NewsArticle>
    generatedAt: Timestamp
    cachedUntil: Timestamp
}
```

**NewsArticle**
```
NewsArticle {
    title: String
    source: String
    url: String
    publishedAt: Timestamp
    snippet: String
    sentiment: Sentiment?
}
```

### 3.2 Database Schema

**Table: portfolio_holdings**
| Column | Type | Constraints |
|--------|------|-------------|
| id | INTEGER | PRIMARY KEY, AUTO_INCREMENT |
| symbol | TEXT | NOT NULL |
| company_name | TEXT | NOT NULL |
| purchase_price | REAL | NOT NULL |
| purchase_date | INTEGER | NOT NULL (epoch millis) |
| shares | INTEGER | NOT NULL, DEFAULT 1 |

**Table: cached_stocks**
| Column | Type | Constraints |
|--------|------|-------------|
| symbol | TEXT | PRIMARY KEY |
| data_json | TEXT | NOT NULL |
| cached_at | INTEGER | NOT NULL |

**Table: search_history**
| Column | Type | Constraints |
|--------|------|-------------|
| query | TEXT | PRIMARY KEY |
| searched_at | INTEGER | NOT NULL |

**Table: analysis_cache**
| Column | Type | Constraints |
|--------|------|-------------|
| symbol | TEXT | PRIMARY KEY |
| analysis_json | TEXT | NOT NULL |
| generated_at | INTEGER | NOT NULL |
| expires_at | INTEGER | NOT NULL |

---

## 4. API Integration

### 4.1 Stock Quotes API

**Recommended Provider:** Alpha Vantage (free tier) or Finnhub

**Endpoints Used:**

| Purpose | Endpoint | Rate Limit Consideration |
|---------|----------|--------------------------|
| Stock List / Market Overview | GET /query?function=TOP_GAINERS_LOSERS | Cache for 5 minutes |
| Stock Quote | GET /query?function=GLOBAL_QUOTE&symbol={symbol} | Cache for 1 minute |
| Stock Search | GET /query?function=SYMBOL_SEARCH&keywords={query} | Debounce 300ms |
| Company Overview | GET /query?function=OVERVIEW&symbol={symbol} | Cache for 24 hours |

**Error Handling:**
- Rate limit exceeded (429): Show cached data with "data may be outdated" notice
- Network error: Display offline indicator, use cached data
- Invalid symbol: Show user-friendly error message

### 4.2 Claude API Integration

**Endpoint:** POST https://api.anthropic.com/v1/messages

**Request Structure:**
```
{
    "model": "claude-sonnet-4-20250514",
    "max_tokens": 1024,
    "messages": [
        {
            "role": "user",
            "content": "Analyze {SYMBOL} stock. Provide: 1) Company summary, 
                       2) Recent performance analysis, 3) Key investment 
                       considerations. Recent news: {NEWS_SNIPPETS}"
        }
    ]
}
```

**Prompt Engineering Strategy:**
- Include structured instructions for consistent output format
- Inject recent news snippets for context-aware analysis
- Request sentiment classification (bullish/neutral/bearish)
- Limit response length to manage costs

**Caching Strategy:**
- Cache AI analysis for 6 hours per stock
- Invalidate cache if significant price movement (>5%) detected
- Store in local database with expiration timestamp

---

## 5. Screen Designs

### 5.1 Navigation Structure

```
Bottom Navigation Bar
├── Stocks (Home)
├── Portfolio
└── Settings

Stock Detail (from any stock tap)
└── AI Analysis (sub-screen)
```

### 5.2 Screen Specifications

#### Screen 1: Stock List (Home)

**Layout:**
- Top app bar with "Vienna" title and search icon
- Sort chips below app bar: "Volume" | "Gainers" | "Losers"
- Scrollable list of stock cards
- Pull-to-refresh gesture
- FAB for quick search access

**Stock Card Contents:**
- Symbol (bold, large)
- Company name (secondary text)
- Current price (right-aligned)
- Price change with percentage (green/red badge)
- Volume indicator (small text)

**Interactions:**
- Tap card → Navigate to Stock Detail
- Long press → Quick add to portfolio (with confirmation)
- Tap sort chip → Re-sort list with animation

#### Screen 2: Search

**Layout:**
- Full-screen search with auto-focus text field
- Recent searches section (when query empty)
- Real-time results list (when typing)
- Empty state illustration (no results)

**Interactions:**
- Type → Debounced search (300ms delay)
- Tap result → Navigate to Stock Detail
- Tap recent search → Populate and search
- Clear button → Reset field

#### Screen 3: Portfolio

**Layout:**
- Summary card at top: total value, total gain/loss, percentage return
- List of holdings sorted by most recent
- Empty state with illustration and "Add your first stock" CTA

**Holding Card Contents:**
- Symbol and company name
- Current value (shares × current price)
- Purchase price vs current price
- Gain/loss amount and percentage
- Days held
- Delete button (icon)

**Interactions:**
- Tap holding → Navigate to Stock Detail
- Swipe left → Reveal delete action
- Pull to refresh → Update all prices

#### Screen 4: Stock Detail

**Layout:**
- Collapsible header with symbol, name, current price
- Price change badge (prominent)
- Key metrics section: Day range, Volume, Market Cap
- "Add to Portfolio" button (or "In Portfolio" badge if already added)
- "Get AI Analysis" button
- Historical price chart (simple line chart, 1 week default)

**Interactions:**
- Tap "Add to Portfolio" → Add with current price, show confirmation
- Tap "Get AI Analysis" → Navigate to Analysis screen

#### Screen 5: AI Analysis

**Layout:**
- Stock header (symbol, name, price)
- Loading state with animated placeholder
- Summary section (expandable)
- Sentiment indicator (bullish/neutral/bearish with icon)
- Key points as bullet list
- Related news articles section (scrollable cards)

**News Card Contents:**
- Article title
- Source name and date
- Snippet preview
- Tap to open in browser

**Interactions:**
- Pull to refresh → Regenerate analysis
- Tap news card → Open URL in Chrome Custom Tab

### 5.3 Visual Design Guidelines

**Color Palette:**
- Primary: Deep Blue (#1E3A5F) - trust, stability
- Secondary: Teal (#00897B) - growth, positivity
- Success/Gain: Green (#27AE60)
- Error/Loss: Red (#E74C3C)
- Background: Off-white (#FAFAFA)
- Surface: White (#FFFFFF)

**Typography:**
- Headlines: Roboto Medium
- Body: Roboto Regular
- Numbers/Prices: Roboto Mono (for alignment)

**Iconography:** Material Symbols (rounded variant)

---

## 6. Data Flow

### 6.1 Stock List Loading

```
User opens app
       │
       ▼
ViewModel requests stock list from UseCase
       │
       ▼
UseCase checks cache freshness (< 5 min?)
       │
  ┌────┴────┐
  │ Fresh   │ Stale
  ▼         ▼
Return      Fetch from API
cached      │
data        ├──► Success: Cache + Return
            └──► Failure: Return cached (if any) + Error state
```

### 6.2 AI Analysis Generation

```
User taps "Get AI Analysis"
       │
       ▼
Check local cache (< 6 hours?)
       │
  ┌────┴────┐
  │ Valid   │ Expired/None
  ▼         ▼
Return      Fetch recent news (from news API or web search)
cached      │
analysis    ▼
            Build Claude prompt with stock data + news
            │
            ▼
            Call Claude API
            │
            ├──► Success: Parse response, cache, return
            └──► Failure: Show error, offer retry
```

### 6.3 Portfolio Update Flow

```
User adds stock to portfolio
       │
       ▼
Record in Room DB:
- Symbol, company name
- Current price as purchase price
- Current timestamp
       │
       ▼
Emit updated portfolio via Flow
       │
       ▼
UI reflects new holding
```

---

## 7. Error Handling Strategy

| Scenario | User Experience | Technical Handling |
|----------|-----------------|---------------------|
| No internet | Banner: "You're offline" + cached data | Catch IOException, check connectivity |
| API rate limit | Toast: "Data may be delayed" + cached data | Catch 429, use exponential backoff |
| Stock not found | Empty state: "Stock not found" | Handle 404, clear search |
| Claude API error | Modal: "Analysis unavailable" + retry button | Catch API errors, log for debugging |
| Database error | Snackbar: "Couldn't save" | Catch Room exceptions, offer retry |

---

## 8. Implementation Plan

### Phase 1: Foundation (Week 1-2)

**Goals:** Project setup, core infrastructure, basic stock display

**Tasks:**
- Initialize Android project with Compose, Hilt, Room, Retrofit
- Implement Stock API integration with repository pattern
- Build Stock List screen with sorting functionality
- Set up local caching for stock data
- Create basic navigation structure

**Deliverables:** App displays live stock list with sorting

### Phase 2: Search & Detail (Week 3)

**Goals:** Stock search and detail viewing

**Tasks:**
- Implement search API integration with debouncing
- Build Search screen with recent history
- Create Stock Detail screen with metrics display
- Add simple price chart using a charting library (MPAndroidChart or similar)

**Deliverables:** Users can search and view individual stocks

### Phase 3: Portfolio (Week 4)

**Goals:** Full portfolio management

**Tasks:**
- Design and implement Room database schema for holdings
- Build Portfolio screen with summary calculations
- Add "Add to Portfolio" functionality from detail screen
- Implement portfolio performance calculations
- Add swipe-to-delete functionality

**Deliverables:** Users can manage virtual portfolio

### Phase 4: AI Integration (Week 5)

**Goals:** Claude API integration for analysis

**Tasks:**
- Implement Claude API client with proper authentication
- Design and test prompts for stock analysis
- Build AI Analysis screen with loading states
- Implement analysis caching strategy
- Add news article display (fetch from news API or Claude web search)

**Deliverables:** AI-powered stock analysis functional

### Phase 5: Polish & Launch (Week 6)

**Goals:** Quality, performance, release readiness

**Tasks:**
- Comprehensive error handling and edge cases
- UI polish and animations
- Performance optimization (lazy loading, memory management)
- Unit and integration testing
- Prepare for Play Store submission

**Deliverables:** Production-ready application

---

## 9. Testing Strategy

### 9.1 Unit Tests

**Coverage Targets:**
- ViewModels: 90%+ coverage
- Use Cases: 100% coverage
- Repository logic: 85%+ coverage

**Key Test Cases:**
- Portfolio gain/loss calculations
- Sort ordering correctness
- Cache expiration logic
- API response parsing

### 9.2 Integration Tests

- Room database CRUD operations
- Retrofit API client behavior
- Repository + Remote + Local interaction

### 9.3 UI Tests

- Navigation flows (Compose UI tests)
- Form validation (search input)
- Loading and error states
- Accessibility compliance

### 9.4 Manual Testing Checklist

- [ ] Stock list loads and sorts correctly
- [ ] Search returns accurate results
- [ ] Portfolio additions persist across app restarts
- [ ] Performance metrics calculate correctly
- [ ] AI analysis generates meaningful content
- [ ] Offline mode shows appropriate fallbacks
- [ ] All error states display correctly

---

## 10. Security Considerations

**API Key Management:**
- Store Claude API key in local.properties (excluded from git)
- Use BuildConfig to inject at build time
- Consider using Android Keystore for production

**Data Privacy:**
- No user authentication required (local portfolio only)
- No PII collected or transmitted
- Stock data is publicly available information

**Network Security:**
- All API calls over HTTPS
- Certificate pinning for Claude API (recommended)
- Network security config to prevent cleartext traffic

---

## 11. Future Enhancements (Post-MVP)

**v1.1 Considerations:**
- Price alerts and notifications
- Multiple portfolios/watchlists
- Historical performance charts (30d, 90d, 1y)
- Social sharing of portfolio performance

**v2.0 Considerations:**
- User accounts with cloud sync
- Real-time price updates via WebSocket
- Advanced AI features: portfolio optimization suggestions
- Widget for home screen

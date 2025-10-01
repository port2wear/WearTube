# WearTube

A clean, fast YouTube browsing experience tailored for Wear OS watches. Built with Jetpack Compose for Wear OS, optimized for small, round screens, and designed for quick, glanceable interactions.

---

## Highlights

- 🎞️ Trending & search-first UX with watch-friendly controls
- 🧭 Swipe-to-dismiss navigation and on-wrist ergonomics
- ⚡ Compose performance patterns for wearable form factors
- 🧩 Reusable UI building blocks (cards, lists, error/loading states)
- 🎬 Shorts-style two-column layout for quick discovery

---

## Screens & Components

- HomeScreen
  - Trending feed by default
  - Real-time search (titles, channels, descriptions)
  - Clear loading and error states
- VideoPlayerScreen
  - Play/Pause mock playback (demo mode)
  - Progress indicator, metadata, and compact controls
- Reusable components
  - VideoItem: thumbnail + title + channel
  - SearchBar: compact, thumb-friendly input
  - LoadingIndicator & ErrorMessage
  - ShortItem & ShortsGrid: 2-column, vertically scrolling layout

> Shorts layout is available via `ShortsGrid(videos, onClick)` from `presentation/components/VideoComponents.kt`.

---

## Tech Stack

- Jetpack Compose for Wear OS (Material components, ScalingLazyColumn)
- ViewModel + StateFlow for state management
- Coil for image loading
- Navigation for Compose
- Retrofit (ready for API integration)

---

## Project Structure

```
app/
└─ src/main/java/com/blake7/weartube/
   ├─ data/
   │  ├─ api/            # API service interfaces
   │  ├─ model/          # YouTube models (search/videos/comments)
   │  └─ repository/     # Mock + real repositories
   └─ presentation/
      ├─ components/     # Reusable composables (VideoItem, ShortsGrid, etc.)
      ├─ screens/        # HomeScreen, VideoPlayerScreen
      ├─ viewmodel/      # HomeViewModel, VideoPlayerViewModel
      └─ theme/          # Theming (colors/typography)
```

---

## Quickstart

Prereqs
- Android Studio Electric Eel (or newer)
- JDK 17 (Android Studio bundled JDK is fine)
- Wear OS Emulator or a physical Wear OS watch

Build & Run
1. Open this project in Android Studio
2. Let Gradle sync dependencies
3. Launch a Wear OS emulator (or connect a device)
4. Run the `app` configuration

CLI build
```
./gradlew :app:assembleDebug
```

---

## YouTube API (optional, production)

Out of the box the app uses a mock repository for a smooth demo experience. To use the real YouTube Data API v3:

1) Create an API key
- Open Google Cloud Console
- Enable “YouTube Data API v3”
- Create an API key

2) Wire the repository
- Replace `MockYouTubeRepository` with `YouTubeRepository` in your ViewModels

```kotlin
// HomeViewModel.kt, VideoPlayerViewModel.kt
private val repository = YouTubeRepository()
```

3) Provide the API key
```kotlin
// YouTubeRepository.kt
private val apiKey = "YOUR_API_KEY"
```

Notes
- Consider quota, caching, pagination, and errors for production
- Some features may require OAuth 2.0 (user context)

---

## Shorts Layout

Use the 2-column Shorts grid wherever you need quick discovery:

```kotlin
ShortsGrid(
  videos = videos,
  onClick = { video -> /* open details */ }
)
```

Under the hood:
- `ShortItem`: 16:9 thumbnail + title + lightweight view line
- `ShortsGrid`: vertical scroll, chunked into rows of two

---

## Cleanup & Housekeeping

Transient build artifacts and IDE caches can balloon quickly. This repo includes a small utility to prune them safely.

Clean the project (repo only)
```
bash scripts/clean.sh --yes
```

Clean + purge local Gradle cache (heavy)
```
bash scripts/clean.sh --yes --all
```

What it removes
- Module and root build dirs (`**/build/`, `/build/`)
- `.gradle/`, `.kotlin/`
- Common Android Studio caches in `.idea/`
- Native build folders (`.externalNativeBuild/`, `.cxx/`)
- Captures, `.DS_Store`

> `.gitignore` is hardened to keep these artifacts out of version control.

---

## Development Tips

- Keep composables preview-friendly: provide safe default parameters in previews
- Watch form factor: prefer short text, large tap targets, and vertical layouts
- Use `ScalingLazyColumn` for watch-optimized scrolling
- Avoid heavy recomposition in list items (remember state, key items)

---

## Roadmap

- ExoPlayer-based playback on-device
- Voice Search
- Tiles & Complications for quick access
- Subscriptions and authenticated feeds
- Offline caching and paging

---

## Troubleshooting

- Gradle sync issues
  - File > Invalidate Caches / Restart
  - Run the cleanup script, then re-sync
- Image loading
  - Verify network and URLs; Coil logs in Logcat can help
- Emulator performance
  - Cold boot, disable animations, increase RAM if needed

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make focused commits with clear messages
4. Test on a Wear OS emulator/device
5. Open a PR (screenshots welcome!)

---

## License & Notice

This project is provided for educational and demonstration purposes. Using the YouTube Data API must comply with YouTube’s Terms of Service and API policies. Ensure you have the right to display any content fetched by the app.

---

Built with ❤️ for Wear OS developers.

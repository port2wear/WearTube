# WearTube

A clean, fast YouTube browsing experience tailored for Wear OS watches. Built with Jetpack Compose for Wear OS, optimized for small, round screens, and designed for quick, glanceable interactions.

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


## Contributing

1. Fork the repository
2. Create a feature branch
3. Make focused commits with clear messages
4. Test on a Wear OS emulator/device
5. Open a PR (screenshots welcome!)


Built with ❤️ for Wear OS users.

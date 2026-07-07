# Neko Record

Android livestream broadcasting app (Kotlin + Jetpack Compose + Material 3),
built GitHub-first with small, reviewable commits and CI-verified milestones.

Package: `com.neko.record` · Min SDK 29 (Android 10+) · Compile/Target SDK 34

> **Status:** Milestone 1 — project scaffold + basic Live Stream UI.
> See [Roadmap](#roadmap) below for what's next.

## Why GitHub-first, no local Android Studio required

This repo is structured so every file is editable directly from the GitHub
web UI, and every push/PR is verified by GitHub Actions:

| Workflow | Trigger | What it does |
|---|---|---|
| `android-build.yml` | push / PR to `main`/`develop` | Assembles a debug APK, uploads it as a build artifact |
| `unit-tests.yml` | push / PR | Runs JVM unit tests, uploads the HTML report |
| `code-quality.yml` | push / PR | Runs Android Lint + Detekt static analysis |

To get an installable APK: push to `main` (or open a PR), open the **Actions**
tab on GitHub, open the latest `Android Debug Build` run, and download the
`neko-record-debug-apk` artifact from the run summary.

## Building locally (optional)

You don't need Android Studio, but you do need a JDK 17 and Gradle 8.7 on
your PATH (the Gradle wrapper JAR binary is intentionally **not** committed —
see `.gitignore` — so CI always uses a known-good Gradle install instead of a
binary blob sitting in git history):

```bash
# one-time, if you don't already have gradle:
sdk install gradle 8.7   # via sdkman, or use your OS package manager

gradle :app:assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

If you do want a `./gradlew` wrapper for convenience, generate it once with:

```bash
gradle wrapper --gradle-version 8.7
```

and commit the resulting `gradle/wrapper/gradle-wrapper.jar` yourself — it's
excluded from this repo on purpose to keep the git history binary-free.

## Project structure

```
app/
  src/main/java/com/neko/record/
    NekoRecordApp.kt          # @HiltAndroidApp entry point
    MainActivity.kt            # single Activity, hosts Compose content
    ui/theme/                  # Color.kt, Type.kt, Theme.kt (Material 3)
    ui/navigation/              # Screen.kt (routes), NavGraph.kt (NavHost + bottom nav)
    ui/home/                    # HomeScreen.kt + HomeViewModel.kt (Live Stream tab)
    ui/components/              # PlatformCard.kt, BottomNavBar.kt
    data/model/                  # Platform.kt (PlatformId, PlatformUiModel)
  src/test/                      # JVM unit tests (HomeViewModelTest)
  src/androidTest/                # Instrumentation smoke test
.github/workflows/                # CI: build / test / lint+detekt
detekt.yml                         # Static analysis rules
```

Architecture follows **MVVM + Repository + Hilt + Coroutines/Flow + Compose**,
per the project spec. Milestone 1 only needs a ViewModel (no repository yet —
the platform list is static); the `data/` and `domain/` layers grow a proper
Repository once real network/RTMP state exists (Milestone 3 onward).

## Roadmap

Each milestone lands as its own set of small commits / PRs, and the repo stays
buildable after every single commit:

1. **Project init + basic UI** ← you are here
2. MediaProjection + Screen Capture (true device aspect ratio, no forced 9:16/16:9)
3. RTMP Engine (YouTube/Facebook/Twitch/TikTok/Twitter/Custom RTMP, login or stream key)
4. Audio (mic / internal audio / both, AGC, noise suppression, echo cancellation)
5. Float Ball (start/stop/settings/live status)
6. Pause Livestream (freeze last frame, keep RTMP socket + stream key, no reconnect)
7. Adaptive Bitrate (CBR/VBR/ABR, network-aware)
8. OpenCV/TFLite template detection (custom sample images, similarity threshold)
9. Automation Rules (detect → stop stream / notify / play alert sound)
10. Performance pass (memory, ANR, dropped-frame hardening)

## Security notes

- Stream keys and RTMP credentials are excluded from Android auto-backup
  (`backup_rules.xml` / `data_extraction_rules.xml`) starting from Milestone 1's
  manifest, even though key storage itself isn't implemented until Milestone 3.
- No signing keystore is ever committed — see `.gitignore`. Release signing
  config will be documented separately when the Play publishing guide is added.

## Publishing to Google Play

Will be documented once Milestone 3 (RTMP Engine) lands and there's a
feature-complete debug build worth signing for internal testing.

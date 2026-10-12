# Chunk Finder — Fabric / Minecraft Java 1.21.11

A client-side HUD/minimap prototype that marks village, mineshaft, and stronghold structure starts **only when they are already present in loaded client-side chunk data**.

## Features

- HUD overlay with a small map and colored structure markers.
- Adjustable search radius from 32 to 512 blocks.
- Keyboard toggles for each structure category.
- Scans twice per second instead of scanning on every rendered frame.
- De-duplicates structure markers found in multiple nearby chunks.
- Does not access server files, force-load chunks, or discover hidden/unloaded structures.

## Controls

| Key | Action |
|---|---|
| `O` | Toggle overlay |
| `[` | Reduce radius by 32 blocks |
| `]` | Increase radius by 32 blocks |
| `V` | Toggle village markers |
| `M` | Toggle mineshaft markers |
| `H` | Toggle stronghold markers |

You can remap keys under **Options → Controls → Key Binds → Chunk Finder**.

## Requirements

- Minecraft Java Edition 1.21.11
- Java Development Kit (JDK) 21
- Fabric Loader for Minecraft 1.21.11
- Matching Fabric API
- Gradle (or a generated Gradle wrapper)

Version coordinates are pinned in `gradle.properties` and `build.gradle`. These have not been verified by a live Gradle build in this package-generation environment; if a repository reports a missing artifact, update the version to one published for Minecraft 1.21.11.

## Build on Windows

1. Install JDK 21.
2. Install Gradle, or generate a wrapper by running `gradle wrapper` in the project directory.
3. Open PowerShell in the project folder.
4. Run `gradle :mod:build` (or `.\gradlew.bat :mod:build` if you generated the wrapper).
5. The JAR should be in `mod\build\libs\`.

## Install

1. Install Fabric Loader for Minecraft 1.21.11.
2. Launch the Fabric profile once, then close Minecraft.
3. Copy `chunk-finder-1.1.0.jar` from `mod\build\libs\` into your Minecraft `mods` folder.
4. Add the matching Fabric API JAR to that same folder.
5. Launch the Fabric profile.

## Limitations

- Multiplayer servers control which chunks and world data are sent to clients. This mod cannot find structures in unloaded or unexplored chunks.
- Markers appear only if structure-start data is exposed by a loaded client chunk; server settings and API behavior may limit what is visible.
- Amethyst geodes are configured features, not normal structure starts, and are not detected.
- This ZIP contains source code, not a precompiled mod JAR. Build it with Java/Gradle first.

## Easiest build option: GitHub Actions

See [`CLOUD_BUILD_GUIDE.md`](CLOUD_BUILD_GUIDE.md). The included `.github/workflows/build.yml` lets GitHub install Java 21 and Gradle on a cloud runner and upload the built JAR as an artifact, so you do not need to install JDK or Gradle locally. The cloud build still needs to be run and may reveal compile/dependency errors.

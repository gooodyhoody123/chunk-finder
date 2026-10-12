# Build Chunk Finder in the cloud (no local JDK or Gradle install)

This project includes a GitHub Actions workflow that installs Java 21 and Gradle on GitHub's build runner and attempts to compile the mod. You do not install either tool on your own PC for this method.

## Steps

1. Sign in to GitHub or create an account at https://github.com/.
2. Click the **+** in the top-right and choose **New repository**. Name it `chunk-finder`, choose **Public** or **Private**, then click **Create repository**.
3. On the new repository page, choose **Add file → Upload files**.
4. Extract this ZIP on your PC first. Upload the *contents* of the extracted folder, including the hidden `.github` folder and its `workflows/build.yml` file. If Windows hides `.github`, use File Explorer's **View → Show → Hidden items** or upload the files using GitHub's web uploader while ensuring `.github/workflows/build.yml` is included.
5. Click **Commit changes**.
6. Open the repository's **Actions** tab. Select **Build Chunk Finder** on the left, click **Run workflow**, then **Run workflow** again.
7. Open the run that appears. If it succeeds, scroll to **Artifacts** at the bottom and download `chunk-finder-jar`.
8. Extract the downloaded artifact ZIP. Copy `chunk-finder-1.1.0.jar` (or the JAR with that project's version) into your Minecraft `.minecraft/mods` folder. Install Fabric API for Minecraft 1.21.11 too.

## Important

- The cloud build is an attempt, not a guarantee. If the build fails, open the failed run and inspect the `Build mod` log; dependency pins or source API compatibility may need adjustment.
- A successful compilation is not the same as an in-game test.
- The mod only marks structure starts exposed in loaded client-side chunks. It cannot reveal hidden/unloaded structures on a multiplayer server.
- Minecraft itself still needs a compatible Java runtime to launch. This method only avoids installing JDK/Gradle as local development tools.

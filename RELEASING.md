# Release Process

Modrinth releases are published only by `.github/workflows/ci.yml` after wrapper validation, the build and exact-JAR verification, the dedicated-server smoke test, and the Farmer's Delight compatibility matrix pass.

## One-Time GitHub Setup

1. Create a GitHub environment named `modrinth-production`.
2. Add the Modrinth token to that environment as the `MODRINTH_TOKEN` secret. Do not add the token to the repository, workflow, Gradle properties, or command output.
3. Restrict deployment approval for `modrinth-production` and release-tag creation to maintainers.
4. Protect `fabric-26.1.2` and require these checks before merging:
   - `Wrapper validation`
   - `Build and verify`
   - `Dedicated server smoke test`
   - `Farmer's Delight compatibility`

## Prepare a Release

1. Set `mod_version` in `gradle.properties`.
2. Start `CHANGELOG.md` with exactly `# <mod_version>` and add the release notes below it.
3. Open a pull request and merge it only after every required check passes.
4. Tag the verified commit using this exact format:

   ```text
   v<mod_version>+<minecraft_version>-fabric
   ```

5. Push the tag. Do not run the Gradle publishing tasks locally.

Branch pushes and pull requests build, test, verify the release JAR, run the server smoke test, and launch both a server and headless client against up to five current compatible Farmer's Delight releases, but they never publish. Only a matching tag push can enter the protected `modrinth-production` environment.

The tag job downloads the exact JAR and SHA-256 checksum produced by `Build and verify`, validates both, and revalidates that downloaded JAR through Gradle. Before upload it checks Modrinth for the release version. An existing version with the same JAR hash is treated as already published; an existing version with a different hash fails the release.

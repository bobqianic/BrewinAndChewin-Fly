# Release Process

Modrinth releases are published only by `.github/workflows/ci.yml` after the wrapper validation, build verification, dedicated-server smoke, and Farmer's Delight compatibility jobs pass.

## One-Time GitHub Setup

1. Create a GitHub environment named `modrinth-production`.
2. Add the Modrinth token to that environment as the `MODRINTH_TOKEN` secret.
3. Protect each maintained release branch and require these checks before merging:
   - `Wrapper validation`
   - `Build and verify`
   - `Dedicated server smoke test`
   - `Farmer's Delight compatibility`
4. Restrict release-tag creation and `modrinth-production` deployment approval to maintainers.

The stable `Farmer's Delight compatibility` check covers the newest compatible stable Farmer's Delight Refabricated releases discovered from Modrinth for this branch, up to five versions. Require that aggregate check rather than the individual versioned matrix jobs.

Do not store the token as a repository secret, Gradle property, workflow literal, or local file.

## Prepare a Release

1. Set `mod_version` in `gradle.properties`.
2. Start `CHANGELOG.md` with exactly `# <mod_version>` and add the release notes below it.
3. Open a pull request and merge it only after all required checks pass.
4. Tag the verified commit using this exact format:

   ```text
   v<mod_version>+<minecraft_version>-fabric
   ```

5. Push the tag. Do not use the manual Gradle publishing tasks.

The tag workflow downloads the exact JAR produced by `Build and verify`, verifies its transferred SHA-256 checksum, and revalidates that artifact before publishing. It also checks Modrinth first: an existing version with the same JAR hash succeeds without uploading, while an existing version with a different JAR hash fails.

Pull requests, branch pushes, and manual workflow runs verify the project but never publish. Gradle rejects Modrinth publishing unless it is running in the matching tag invocation of `.github/workflows/ci.yml`.

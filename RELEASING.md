# Release Process

Modrinth releases are published only by `.github/workflows/ci.yml` after the build, artifact validation, and dedicated-server smoke jobs pass.

## One-Time GitHub Setup

1. Rotate the previously exposed Modrinth token.
2. Create a GitHub environment named `modrinth-production`.
3. Add the rotated token to that environment as the `MODRINTH_TOKEN` secret.
4. Protect each maintained release branch and require these checks before merging:
   - `Wrapper validation`
   - `Build and verify`
   - `Dedicated server smoke test`
5. Restrict release-tag creation and `modrinth-production` deployment approval to maintainers.

## Release

1. Set `mod_version` in `gradle.properties`.
2. Start `CHANGELOG.md` with `# <mod_version>` and add the release notes.
3. Open a pull request and merge it only after all required checks pass.
4. Tag the verified commit using this exact format:

   ```text
   v<mod_version>+<minecraft_version>-fabric
   ```

5. Push the tag. The tag workflow downloads the JAR produced by `Build and verify`, checks its SHA-256 digest, revalidates that exact JAR, and publishes it to Modrinth.

The Gradle publishing tasks reject local runs and tags that do not match the versions in `gradle.properties`.

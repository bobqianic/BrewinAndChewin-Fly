# Brewin' And Chewin' Fly

**Brewin' And Chewin' Fly** is a Fabric fork of [Brewin' And Chewin'](https://github.com/ChefsDelights/BrewinAndChewin) for Minecraft 26.3.

This fork keeps the original `brewinandchewin` mod id for world, resource-pack, datapack, and recipe compatibility.

## Features

- Fermenting and brewing addon content for Farmer's Delight.
- Create-Fly filling recipes for Brewin' And Chewin' drinks when a compatible Create-Fly release is available.
- Existing Brewin' And Chewin' ids remain stable for pack compatibility.
- Fabric 26.3 dependency metadata and build output are named for the fork.

## What's New

- Players can drink directly from barrels.
- Satiety and saturation values have been recalculated for better food balance.
- Fermentation tanks can hold any liquid, and their temperature now changes gradually.
- Large kegs have been added.
- Kegs now show more realistic steam and cold-air particles.
- Empty kegs can be ignited, while heated liquid-filled kegs will evaporate.
- Coaster fixes cover texture corruption, item stacking, and missing collision boxes for 3D models.

## Required Dependencies

- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated)

## Optional Compatibility

- JEI
- AppleSkin

Create-Fly support is on hold until a Minecraft 26.3 version is available. The integration remains in the source for a later release.

## Gradle

Use the full Modrinth version string, for example `4.5.5-fly+26.3-fabric`.

```groovy
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "maven.modrinth:brewinandchewin-fly:${bnc_fly_version}"
}
```

## Upstream

This fork is based on Brewin' And Chewin' by Probleyes, Umpaz, and MerchantCalico.

# Brewin' And Chewin' Fly

**Brewin' And Chewin' Fly** is a Fabric fork of Brewin' And Chewin' for Minecraft 1.21.10.

This fork keeps the original `brewinandchewin` mod id for world, resource-pack, datapack, and recipe compatibility.

## Features

- Fermenting and brewing addon content for Farmer's Delight.
- Optional Create-Fly filling recipes for Brewin' And Chewin' drinks.
- Existing Brewin' And Chewin' ids remain stable for pack compatibility.
- Fabric 1.21.10 dependency metadata and build output are named for the fork.

## What's New

- Wine can be drunk directly from barrels.
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
- Greenhouse Config

## Optional Compatibility

- [Create-Fly](https://modrinth.com/mod/create-fly), published on Modrinth Maven as `maven.modrinth:create-fly`
- JEI
- AppleSkin
- Styled Chat

## Gradle

```groovy
repositories {
    maven {
        name = "Greenhouse Maven"
        url = "https://maven.greenhouse.lgbt/releases/"
    }
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "umpaz.brewinandchewin:BrewinAndChewin-Fly:${bnc_fly_version}+${minecraft_version}"
    modCompileOnly "maven.modrinth:create-fly:${create_fly_version}"
    modLocalRuntime "maven.modrinth:create-fly:${create_fly_version}"
}
```

## Upstream

This fork is based on Brewin' And Chewin' by Probleyes, Umpaz, and MerchantCalico.

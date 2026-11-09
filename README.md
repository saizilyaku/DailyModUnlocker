# Daily Mod Unlocker

Minecraft Forge 1.20.1 mod that unlocks one installed mod per real-world day. Locked mods have their recipes hidden and their mobs and drops restricted. Unlock state is saved with the world.

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.x
- Java 17

## Build

On Windows, run `gradlew.bat build` from the project folder. The distributable JAR is created under `build/libs/`.

## In game

- `/unlock-mod <modid>` unlocks a mod immediately (operator permission level 2).
- `/unlockmodgui` opens the client-side unlock status screen.
- The common config is `config/dailymodunlocker-common.toml`. `alwaysUnlockedMods` lists mods that are never locked, and `unlockAddonsWithParent` controls whether dependent mods unlock with their parent.

Start a world/server after installing the mod. The first eligible mod is unlocked when the world loads; a further mod is unlocked when the server observes a new local calendar day. Progress is stored per world.

## Development

Import `build.gradle` into IntelliJ IDEA or Eclipse and use the generated Forge run configurations. Source code is under `src/main/java`.

# Infectious Patch (Unofficial Patch)

A small patch for [Infectious - Zombie Apocalypse](https://www.curseforge.com/minecraft/mc-mods/infectious-zombie-apocalypse) 1.7 on Minecraft 1.20.1 Forge. Infectious itself is still required.

## Bug Fixes
- The `enableLootdrops` gamerule works: with it off, the Lootdrop announcement and sound no longer play.
- Infectious mobs no longer drop their hardcoded items, so loot tables (for example with LootJS) decide their drops.
- Zombies from spawners, commands, spawn eggs and other placed spawns stay in lit places. Infectious removed them as they spawned when no player was within 5 blocks and the spot was lit, which emptied spawners and `/summon` in lit rooms. Natural spawns keep that check, so light still keeps zombies off lit streets and out of bases.

Everything else about Infectious' zombies looks and plays the same.

The log has one line for each patched class, saying how many changes applied.

## Building
1. Download Infectious 1.7 for Forge 1.20.1 from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/infectious-zombie-apocalypse/files/7121624).
2. Put it in `libs/` as `Infectious-forge-1_20_1-1_7.jar` (the name is set in `gradle.properties`). `libs/` is gitignored.
3. Run `gradlew build` with Java 17. The mod is `build/libs/infectiouspatch-<version>.jar`; the `-verify` jar next to it is a dev-only check mod and is never shipped.

### Credit
This is an unofficial patch, not affiliated with Infectious. [Infectious](https://www.curseforge.com/minecraft/mc-mods/infectious-zombie-apocalypse) is made by Zcoper.

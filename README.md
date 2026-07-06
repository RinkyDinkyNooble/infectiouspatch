# Infectious Patch

Fixes the broken `enableLootdrops` gamerule by suppressing the Lootdrop announcement (`tellraw`) and sound when loot drops are disabled.

## Building

This project depends on the **Infectious - Zombie Apocalypse** mod JAR.

1. Create a `libs/` directory in the project root.
2. Place the Infectious mod JAR inside `libs/`.

The required JAR can be downloaded here:
https://www.curseforge.com/minecraft/mc-mods/infectious-zombie-apocalypse/files/7121624

> **Note:** The `libs/` directory is intentionally `.gitignore`d, so it is not included in this repository.

## Environment

- Minecraft 1.20.1
- Forge 47.4.10

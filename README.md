# SamplesEssentials

All-in-one essentials suite for **Paper 26.2 - 26.3**, made by **SampleCraft**.

Every plugin from the old server stack was merged into one plugin. Each former
plugin is now a toggleable **module**, grouped in categorias, with its own
config folder, and all texts live in a per-categoria language folder.

## Excluded on purpose

These stay as separate jars on the server (never merged, never decompiled):

| Plugin | Reason |
| --- | --- |
| ExcellentEconomy | stays standalone |
| ExcellentCrates | stays standalone |
| Sentry | stays standalone |
| AxInventoryRestore | stays standalone |
| Genius-Shop / Shop | stays standalone |
| LibertyBans | stays standalone |
| **TAB** | stays standalone (scoreboard/tablist needs its own lifecycle) |

## Build & download

Everything is built by GitHub Actions - no local toolchain needed.

1. Push to any branch (or run the *Build SamplesEssentials* workflow manually).
2. Open **Actions -> Build SamplesEssentials -> latest run**.
3. Download the **SamplesEssentials-jar** artifact.

Locally: `./gradlew build` -> `build/libs/SamplesEssentials-1.0.0.jar`.

Tagging a commit (`git tag v1.0.0 && git push --tags`) also attaches the jar to
a GitHub release.

## Folder layout on the server

```
plugins/SamplesEssentials/
├── config.yml                  # settings + one toggle per module
├── lang/
│   ├── common.yml              # prefix + generic answers ("no permission", ...)
│   ├── chat/chatclear.yml      # one language file per module, per categoria
│   ├── chat/chatformatter.yml
│   ├── chat/announcements.yml
│   ├── economy/vaultbridge.yml
│   ├── general/starterkit.yml
│   ├── gameplay/...
│   ├── moderation/...
│   ├── rtp/...
│   └── world/...
├── chat/chatclear/config.yml   # one folder per module with its own config
├── chat/chatformatter/config.yml
├── ... (same categoria/module tree as lang/)
```

Add another language by creating `lang/<locale>/` mirroring the English tree;
missing keys fall back to English.

## Commands

| Command | Purpose |
| --- | --- |
| `/samplese status` | list all modules and their state |
| `/samplese modules` | grouped overview with the original jar per module |
| `/samplese toggle <categoria/module>` | enable/disable a module at runtime |
| `/samplese reload` | reload configs, modules and language files |


> Requires **Java 25** at runtime - Paper 26.x is built for Java 25 and the
> plugin targets the newest stable Paper 26.2 API build.

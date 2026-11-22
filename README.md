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

## Deliberately not merged

These jars stay standalone: merging them would mean re-implementing a rendering or
networking stack the plugin cannot own. Each is still installed on the server.

| Plugin | Why it stays a separate jar |
| --- | --- |
| EliteMobs (full framework) | instanced dungeons, procedural items, quests and NPC guilds; only the custom-boss core was merged (`elitemobs`) |
| FancyNpcs (packet renderer) | packet-based player NPCs need packetevents; the NPC data model and action system were merged on real entities (`npcs`) |
| EvenMoreFish (addons/journal) | item, requirement and reward addons plus the fish journal GUI; rarities, fish and competitions were merged (`fishing`) |
| TAB, LibertyBans, Sentry, ExcellentEconomy, ExcellentCrates, AxInventoryRestore, Genius-Shop | own lifecycle / external service, kept standalone by decision |
| VaultUnlocked, PlaceholderAPI, Skript, FAWE, Via*, packetevents, GrimAC, LuckPerms, CoreProtect, EssentialsX, nightcore | infrastructure or platform APIs the merged modules integrate with |
| CrazyAuctions, AsBounty, StarterKitV2, SimplePlayerFreeze, Maintenance, OpenSit, LushRewards, SetHomeGUI, etc. | already covered by an equivalent merged module (`auction`, `bounty`, `starterkit`, `freeze`, `maintenance`, `opensit`, `rewards`, `homes`) |

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

## Modules

48 modules, all toggled from `config.yml` and `/samplese toggle`.

### chat

| Module | Merged from | What it does |
| --- | --- | --- |
| `announcement` | announcement-1.4.jar | Rotating MiniMessage announcements with template |
| `chatclear` | ChatClear.jar | Clears the server chat with a reason |
| `chatformatter` | LPC - Chat Formatter.jar | LuckPerms-style chat format with prefix/suffix, groups and colours |
| `fancytags` | FancyTags-unspecified.jar | Player-head chat tags rendered through object components |

### economy

| Module | Merged from | What it does |
| --- | --- | --- |
| `auction` | CrazyAuctions-1.7.0.jar | Auction house with selling, bidding, expired items and categories |
| `bounty` | AsBounty-1.0.0.jar | Bounty GUI with sorting, search and Vault payouts |
| `clans` | vrx-clans-1.0.4.jar | Clans with ranks, bank, levels, diplomacy, base and clan chat |
| `jobs` | UJobs-1.0.7.jar | Jobs with per-action XP/money rewards, levels and leaderboards |
| `orders` | DonutOrders-1.9.2.jar | Buy-order market with delivery, stash, tax and expiry |
| `rankmenu` | AxRankMenu-1.17.0.jar | Rank shop GUI with weighted ranks, discounts and configurable buy actions |
| `trade` | AxTrade-1.28.2.jar | Secure player-to-player trading with items, money and XP |
| `vaultbridge` | VaultUnlocked-2.20.2.jar | Vault economy bridge with optional balance-on-join |

### gameplay

| Module | Merged from | What it does |
| --- | --- | --- |
| `afkzone` | AxAFKZone-1.13.0.jar | Cuboid AFK zones with timed, chance-based rewards and scoreboard-style HUD |
| `amethysttools` | amethysttools-1.2.1.jar | Area-mining amethyst tools that expire after a few days |
| `biggerenderchest` | BiggerEnderChest1.2.1.jar | Per-player bigger ender chests with /ec, /ecsee and selector GUI |
| `darkdrop` | DarkDrop-1.0.0(2).jar | Descending chest loot drop with good/bad outcome on open |
| `dragoneggenhancements` | DragonEggEnhancements-1.5.jar | Dragon egg powers, container/void/explosion protection and /dragoneggcoords |
| `elitemobs` | EliteMobs(1).jar | Custom bosses with levels, powers, health bars and scaled loot |
| `fishing` | EvenMoreFish-2.5.1.jar | Custom fish rarities and scheduled fishing competitions |
| `graves` | GravesX-2026.4.9.1.jar | Death graves with holograms, protection, teleport and expiry |
| `legendaryweapons` | demoralegendaryweapons-5.2.jar | Legendary weapons with abilities, cooldowns and attribute modifiers |
| `leveltools` | LevelTools-2.1.2.jar | Items gain XP/levels from mining, combat and fishing with rewards |
| `npcs` | FancyNpcs-2.12.1.jar | NPCs with click actions, glowing, interaction cooldowns and distance visibility |
| `opensit` | OpenSit-1.0.6.jar | Sit on stairs and slabs with an invisible seat entity |
| `spawnelytra` | spawnelytra-1.5.jar | Elytra gliding inside configured spawn areas with boosts |

### general

| Module | Merged from | What it does |
| --- | --- | --- |
| `inventorysave` | InventorySave-0.1.jar | Save and load player inventories by id |
| `localinventory` | LocalInventory-1.0.jar | Save, clear and restore your inventory (/sli, /lli, /ali) |
| `rewards` | LushRewards-3.3.2.jar | Daily streak rewards and playtime reward goals with GUI |
| `signitem` | RHSignItem-Paper-1.21_R9.jar | Signs, locks and renames items (lore signature with signer + date) |
| `starterkit` | StarterKitV2.jar | First-join starter kit with /starterkit |

### moderation

| Module | Merged from | What it does |
| --- | --- | --- |
| `freeze` | SimplePlayerFreeze-2.3.0.jar | Freeze players, with /freezeall and /freezelist |
| `inventoryaccess` | OpenInv.jar | View and edit other players' inventories and ender chests |
| `maintenance` | Maintenance-Paper-5.1.0.jar | Maintenance mode with whitelist, timers and MOTD |
| `pvp` | PvPManager.jar | Combat tagging with punishments, PvP toggle and newbie protection |
| `stafftools` | SModeration.jar | Vanish, socialspy and staff chat |

### rtp

| Module | Merged from | What it does |
| --- | --- | --- |
| `ezrtp` | EzRTP-3.4.3.jar | Random teleport with limits, countdown, search patterns and /forcertp |
| `premiumrtp` | RexonRTP.jar | Premium random teleport with cost and own world bounds |
| `simplertp` | Simple_RTP.jar | Lightweight /tpr random teleport without cooldown |

### social

| Module | Merged from | What it does |
| --- | --- | --- |
| `friends` | FriendZ-1.1.jar | Friends, friend requests, /msf direct messages and friendship levels |

### world

| Module | Merged from | What it does |
| --- | --- | --- |
| `advancedhunt` | AdvancedHunt-4.0.4.jar | Treasure-hunt collections with place mode, particles, hints and rewards |
| `buildpaste` | BuildPastePlugin-2.02.jar | Save cuboid builds to files, share them by id and paste them back with undo |
| `chunkmanager` | ChunkManager-1.3.1.jar | World unload, chunk GC and memory diagnostics |
| `chunky` | Chunky-Bukkit-1.4.40.jar | Pre-generates chunks around a centre with progress and ETA |
| `homes` | SetHome_GUI-3.0.5.jar | Homes with GUI menu, chat prompt, limits, cooldown and /homeadmin |
| `lagfixer` | LagFixer-1.7.1.jar | Entity/redstone limits, instant leaf decay, trash disposal and ability cooldowns |
| `protect` | protect-3.4.1-all.jar | Cuboid land claims with owners, members and per-area flags |
| `smartspawner` | SmartSpawner-1.8.3.jar | Spawner editing with eggs and accelerated spawn timing |
| `xaero` | XaeroForceDisabler-1.5.jar | Forces fair-mode / disables the Xaero minimap per world |


## Commands

| Command | Purpose |
| --- | --- |
| `/samplese status` | list all modules and their state |
| `/samplese modules` | grouped overview with the original jar per module |
| `/samplese toggle <categoria/module>` | enable/disable a module at runtime |
| `/samplese reload` | reload configs, modules and language files |


> Requires **Java 25** at runtime - Paper 26.x is built for Java 25 and the
> plugin targets the newest stable Paper 26.2 API build.

package com.fastasyncworldedit.core.configuration;

import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.limit.PropertyRemap;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Settings extends Config {
   @Config.Ignore
   static Settings INSTANCE = new Settings();
   @Config.Ignore
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static final Settings IMP = INSTANCE;
   @Config.Ignore
   public boolean PROTOCOL_SUPPORT_FIX = false;
   @Config.Comment({"These first 6 aren't configurable"})
   @Config.Final
   public String ISSUES = "https://github.com/IntellectualSites/FastAsyncWorldEdit/issues";
   @Config.Final
   public String WIKI = "https://intellectualsites.github.io/fastasyncworldedit-documentation/";
   @Config.Final
   public String DATE;
   @Config.Final
   public String BUILD;
   @Config.Final
   public String COMMIT;
   @Config.Final
   public String PLATFORM;
   @Config.Comment({"Set true to enable WorldEdit restrictions per region (e.g. PlotSquared or WorldGuard).", "To be allowed to WorldEdit in a region, users need the appropriate", "fawe.<plugin>  permission. See the Permissions page for supported region plugins."})
   public boolean REGION_RESTRICTIONS = true;
   @Config.Comment({"FAWE will cancel non admin edits when memory consumption exceeds this %", " - Bypass with `/wea` or `//fast` or `fawe.bypass`", " - Disable with 100 or -1."})
   public int MAX_MEMORY_PERCENT = 95;
   @Config.Comment({"When percent memory usage reaches this threshold some aspects of editing will be slowed down:", " - FAWE-Asynchronous chunk loading when writing changes (see queue.async-chunk-load-write)"})
   public int SLOWER_MEMORY_PERCENT = 80;
   @Config.Create
   public Settings.ENABLED_COMPONENTS ENABLED_COMPONENTS;
   @Config.Create
   public Settings.CLIPBOARD CLIPBOARD;
   @Config.Create
   public Settings.LIGHTING LIGHTING;
   @Config.Create
   public Settings.TICK_LIMITER TICK_LIMITER;
   @Config.Create
   public Settings.WEB WEB;
   @Config.Create
   public Settings.EXTENT EXTENT;
   @Config.Create
   public Settings.EXPERIMENTAL EXPERIMENTAL;
   @Config.Create
   public Settings.QUEUE QUEUE;
   @Config.Create
   public Settings.HISTORY HISTORY;
   @Config.Create
   public Settings.PATHS PATHS;
   @Config.Create
   public Settings.REGION_RESTRICTIONS_OPTIONS REGION_RESTRICTIONS_OPTIONS;
   @Config.Create
   public Settings.GENERAL GENERAL;
   @Config.Create
   public Config.ConfigBlock<Settings.LIMITS> LIMITS;

   private Settings() {
      INSTANCE = this;
   }

   public static Settings settings() {
      return INSTANCE;
   }

   public void reload(File file) {
      this.load(file);
      this.save(file);
   }

   public FaweLimit getLimit(Actor actor) {
      if (actor.hasPermission("fawe.limit.unlimited")) {
         return FaweLimit.MAX.copy();
      } else {
         FaweLimit limit = new FaweLimit();
         ArrayList<String> keys = new ArrayList<>(this.LIMITS.getSections());
         if (keys.remove("default")) {
            keys.add("default");
         }

         boolean limitFound = false;

         for (String key : keys) {
            if (actor.hasPermission("fawe.limit." + key) || !limitFound && key.equals("default")) {
               limitFound = true;
               Settings.LIMITS newLimit = this.LIMITS.get(key);
               limit.MAX_ACTIONS = Math.max(limit.MAX_ACTIONS, newLimit.MAX_ACTIONS != -1 ? newLimit.MAX_ACTIONS : Integer.MAX_VALUE);
               limit.MAX_CHANGES.set(Math.max(limit.MAX_CHANGES.get(), newLimit.MAX_CHANGES != -1L ? newLimit.MAX_CHANGES : Long.MAX_VALUE));
               limit.MAX_BLOCKSTATES.set(Math.max(limit.MAX_BLOCKSTATES.get(), newLimit.MAX_BLOCKSTATES != -1 ? newLimit.MAX_BLOCKSTATES : Integer.MAX_VALUE));
               limit.MAX_CHECKS.set(Math.max(limit.MAX_CHECKS.get(), newLimit.MAX_CHECKS != -1L ? newLimit.MAX_CHECKS : Long.MAX_VALUE));
               limit.MAX_ENTITIES.set(Math.max(limit.MAX_ENTITIES.get(), newLimit.MAX_ENTITIES != -1 ? newLimit.MAX_ENTITIES : Integer.MAX_VALUE));
               limit.MAX_FAILS.set(Math.max(limit.MAX_FAILS.get(), newLimit.MAX_FAILS != -1 ? newLimit.MAX_FAILS : Integer.MAX_VALUE));
               limit.MAX_ITERATIONS.set(Math.max(limit.MAX_ITERATIONS.get(), newLimit.MAX_ITERATIONS != -1 ? newLimit.MAX_ITERATIONS : Integer.MAX_VALUE));
               limit.MAX_RADIUS = Math.max(limit.MAX_RADIUS, newLimit.MAX_RADIUS != -1 ? newLimit.MAX_RADIUS : Integer.MAX_VALUE);
               limit.MAX_SUPER_PICKAXE_SIZE = Math.max(
                  limit.MAX_SUPER_PICKAXE_SIZE, newLimit.MAX_SUPER_PICKAXE_SIZE != -1 ? newLimit.MAX_SUPER_PICKAXE_SIZE : Integer.MAX_VALUE
               );
               limit.MAX_BRUSH_RADIUS = Math.max(limit.MAX_BRUSH_RADIUS, newLimit.MAX_BRUSH_RADIUS != -1 ? newLimit.MAX_BRUSH_RADIUS : Integer.MAX_VALUE);
               limit.MAX_BUTCHER_RADIUS = Math.max(
                  limit.MAX_BUTCHER_RADIUS, newLimit.MAX_BUTCHER_RADIUS != -1 ? newLimit.MAX_BUTCHER_RADIUS : Integer.MAX_VALUE
               );
               limit.MAX_HISTORY = Math.max(limit.MAX_HISTORY, newLimit.MAX_HISTORY_MB != -1 ? newLimit.MAX_HISTORY_MB : Integer.MAX_VALUE);
               limit.SCHEM_FILE_NUM_LIMIT = Math.max(
                  limit.SCHEM_FILE_NUM_LIMIT, newLimit.SCHEM_FILE_NUM_LIMIT != -1 ? newLimit.SCHEM_FILE_NUM_LIMIT : Integer.MAX_VALUE
               );
               limit.SCHEM_FILE_SIZE_LIMIT = Math.max(
                  limit.SCHEM_FILE_SIZE_LIMIT, newLimit.SCHEM_FILE_SIZE_LIMIT != -1 ? newLimit.SCHEM_FILE_SIZE_LIMIT : Integer.MAX_VALUE
               );
               limit.MAX_EXPRESSION_MS = Math.max(limit.MAX_EXPRESSION_MS, newLimit.MAX_EXPRESSION_MS != -1 ? newLimit.MAX_EXPRESSION_MS : Integer.MAX_VALUE);
               limit.INVENTORY_MODE = Math.min(limit.INVENTORY_MODE, newLimit.INVENTORY_MODE);
               limit.SPEED_REDUCTION = Math.min(limit.SPEED_REDUCTION, newLimit.SPEED_REDUCTION);
               limit.FAST_PLACEMENT = limit.FAST_PLACEMENT | newLimit.FAST_PLACEMENT;
               limit.CONFIRM_LARGE = limit.CONFIRM_LARGE & newLimit.CONFIRM_LARGE;
               limit.RESTRICT_HISTORY_TO_REGIONS = limit.RESTRICT_HISTORY_TO_REGIONS & newLimit.RESTRICT_HISTORY_TO_REGIONS;
               if (limit.STRIP_NBT == null) {
                  limit.STRIP_NBT = (Set<String>)(newLimit.STRIP_NBT.isEmpty() ? Collections.emptySet() : new HashSet<>(newLimit.STRIP_NBT));
               } else if (!limit.STRIP_NBT.isEmpty() && !newLimit.STRIP_NBT.isEmpty()) {
                  limit.STRIP_NBT = new HashSet<>(limit.STRIP_NBT);
                  limit.STRIP_NBT.retainAll(newLimit.STRIP_NBT);
                  if (limit.STRIP_NBT.isEmpty()) {
                     limit.STRIP_NBT = Collections.emptySet();
                  }
               } else {
                  limit.STRIP_NBT = Collections.emptySet();
               }

               limit.UNIVERSAL_DISALLOWED_BLOCKS = limit.UNIVERSAL_DISALLOWED_BLOCKS & newLimit.UNIVERSAL_DISALLOWED_BLOCKS;
               limit.ALLOW_LEGACY = limit.ALLOW_LEGACY & newLimit.ALLOW_LEGACY;
               limit.SKIP_ENTITY_SPAWN_EVENTS = limit.SKIP_ENTITY_SPAWN_EVENTS | newLimit.SKIP_CREATURE_SPAWN_EVENTS;
               if (limit.DISALLOWED_BLOCKS == null) {
                  limit.DISALLOWED_BLOCKS = (Set<String>)(newLimit.DISALLOWED_BLOCKS.isEmpty()
                     ? Collections.emptySet()
                     : new HashSet<>(newLimit.DISALLOWED_BLOCKS));
               } else if (!limit.DISALLOWED_BLOCKS.isEmpty() && !newLimit.DISALLOWED_BLOCKS.isEmpty()) {
                  limit.DISALLOWED_BLOCKS = new HashSet<>(limit.DISALLOWED_BLOCKS);
                  limit.DISALLOWED_BLOCKS
                     .retainAll(
                        newLimit.DISALLOWED_BLOCKS
                           .stream()
                           .map(s -> s.contains(":") ? s.toLowerCase(Locale.ROOT) : ("minecraft:" + s).toLowerCase(Locale.ROOT))
                           .collect(Collectors.toSet())
                     );
                  if (limit.DISALLOWED_BLOCKS.isEmpty()) {
                     limit.DISALLOWED_BLOCKS = Collections.emptySet();
                  }
               } else {
                  limit.DISALLOWED_BLOCKS = Collections.emptySet();
               }

               if (limit.REMAP_PROPERTIES == null) {
                  limit.REMAP_PROPERTIES = newLimit.REMAP_PROPERTIES.isEmpty() ? Collections.emptySet() : newLimit.REMAP_PROPERTIES.stream().flatMap(s -> {
                     String propertyStr = s.substring(0, s.indexOf(91));
                     List<Property<?>> properties = BlockTypesCache.getAllProperties().get(propertyStr.toLowerCase(Locale.ROOT));
                     if (properties != null && !properties.isEmpty()) {
                        String[] mappings = s.substring(s.indexOf(91) + 1, s.indexOf(93)).split(",");
                        Set<PropertyRemap<?>> remaps = new HashSet<>();

                        for (Property<?> property : properties) {
                           for (String mapping : mappings) {
                              try {
                                 String[] fromTo = mapping.split(":");
                                 remaps.add(property.getRemap(property.getValueFor(fromTo[0]), property.getValueFor(fromTo[1])));
                              } catch (IllegalArgumentException var12) {
                                 break;
                              }
                           }
                        }

                        return remaps.stream();
                     } else {
                        return Stream.empty();
                     }
                  }).collect(Collectors.toSet());
               } else if (!limit.REMAP_PROPERTIES.isEmpty() && !newLimit.REMAP_PROPERTIES.isEmpty()) {
                  limit.REMAP_PROPERTIES = new HashSet<>(limit.REMAP_PROPERTIES);
                  limit.REMAP_PROPERTIES.retainAll(newLimit.REMAP_PROPERTIES.stream().flatMap(s -> {
                     String propertyStr = s.substring(0, s.indexOf(91));
                     List<Property<?>> properties = BlockTypesCache.getAllProperties().get(propertyStr.toLowerCase(Locale.ROOT));
                     if (properties != null && !properties.isEmpty()) {
                        String[] mappings = s.substring(s.indexOf(91) + 1, s.indexOf(93)).split(",");
                        Set<PropertyRemap<?>> remaps = new HashSet<>();

                        for (Property<?> property : properties) {
                           for (String mapping : mappings) {
                              try {
                                 String[] fromTo = mapping.split(":");
                                 remaps.add(property.getRemap(property.getValueFor(fromTo[0]), property.getValueFor(fromTo[1])));
                              } catch (IllegalArgumentException var12) {
                                 break;
                              }
                           }
                        }

                        return remaps.stream();
                     } else {
                        return Stream.empty();
                     }
                  }).collect(Collectors.toSet()));
                  if (limit.REMAP_PROPERTIES.isEmpty()) {
                     limit.REMAP_PROPERTIES = Collections.emptySet();
                  }
               } else {
                  limit.REMAP_PROPERTIES = Collections.emptySet();
               }
            }
         }

         return limit;
      }
   }

   public static class CLIPBOARD {
      @Config.Comment({"Store the clipboard on disk instead of memory", " - Will be slightly slower", " - Uses 2 bytes per block"})
      public boolean USE_DISK = true;
      @Config.Comment({"Compress the clipboard to reduce the size:", " - TODO: Buffered random access with compression is not implemented on disk yet", " - 0 = No compression", " - 1 = Fast compression", " - 2-17 = Slower compression", " - levels over 6 require ZSTD 1.4.8+ to be installed to the system"})
      public int COMPRESSION_LEVEL = 1;
      @Config.Comment({"Number of days to keep history on disk before deleting it"})
      public int DELETE_AFTER_DAYS = 1;
      @Config.Comment({"If a player's clipboard should be deleted upon logout"})
      public boolean DELETE_ON_LOGOUT = false;
      @Config.Comment({"Allows NBT stored in a clipboard to be written to disk", " - Requires clipboard.use-disk to be enabled"})
      public boolean SAVE_CLIPBOARD_NBT_TO_DISK = true;
      @Config.Comment({"Apply a file lock on the clipboard file (only relevant if clipboad.on-disk is enabled)", " - Prevents other processes using the file whilst in use by FAWE", " - This extends to other servers, useful if you have multiple servers using a unified clipboard folder", " - May run into issues where a file lock is not correctly lifted"})
      public boolean LOCK_CLIPBOARD_FILE = false;
   }

   @Config.Comment({"Enable or disable core components"})
   public static final class ENABLED_COMPONENTS {
      public boolean COMMANDS = true;
      @Config.Comment({"Show additional information in console. It helps us at IntellectualSites to find out more about an issue.", "Leave it off if you don't need it, it can spam your console."})
      public boolean DEBUG = false;
      @Config.Migrate("enabled-components.update-notification")
      @Config.Comment({"Whether or not FAWE should notify you on startup about new available snapshots."})
      public boolean SNAPSHOT_UPDATE_NOTIFICATIONS = true;
      @Config.Migrate("enabled-components.update-notification")
      @Config.Comment({"Whether or not FAWE should notify you on startup about new releases."})
      public boolean RELEASE_UPDATE_NOTIFICATIONS = true;
      @Config.Migrate("enabled-components.update-notification")
      @Config.Comment({"Whether or not FAWE should notify you for updates (snapshot / release) on join (with the required permission)"})
      public boolean NOTIFY_UPDATE_INGAME = true;
   }

   @Config.Comment({"Experimental options, use at your own risk", " - UNSAFE = Can cause permanent damage to the server", " - SAFE = Can be buggy but unlikely to cause any damage"})
   public static class EXPERIMENTAL {
      @Config.Comment({"Undo operation batch size", " - The size defines the number of changes read at once.", " - Larger numbers might reduce overhead but increase latency for edits with only few changes.", " - 0 means undo operations are not batched."})
      public int UNDO_BATCH_SIZE = 128;
      @Config.Comment({"[UNSAFE] Directly modify the region files. (OBSOLETE - USE ANVIL COMMANDS)", " - IMPROPER USE CAN CAUSE WORLD CORRUPTION!"})
      public boolean ANVIL_QUEUE_MODE = false;
      @Config.Comment({"[SAFE] Dynamically increase the number of chunks rendered", " - Requires Paper", " - Set your server view distance to 1 (spigot.yml, server.properties)", " - Based on tps and player movement", " - Note: If entities become hidden, increase the server view distance to 3"})
      public int DYNAMIC_CHUNK_RENDERING = -1;
      @Config.Comment({"Allows brushes to be persistent (default: true)"})
      public boolean PERSISTENT_BRUSHES = true;
      @Config.Comment({"[SAFE] Keep entities that are positioned in non-air blocks when editing an area (default: true)", " - Might cause client-side FPS lag in some situations", " - Requires fast-placement to be true"})
      public boolean KEEP_ENTITIES_IN_BLOCKS = true;
      @Config.Comment({"[SAFE] Attempt to remove entities from the world if they were not present in the expected chunk (default: true)", " - Sometimes an entity may have moved into a different chunk to that which FAWE expected", " - This option allows FAWE to attempt to remove the entity, even if present in a different chunk", " - If the entity is in an unloaded or partially loaded chunk, this will fail", " - If an entity cannot be removed, it is possible duplicate entities may be created when using undo and/or redo"})
      public boolean REMOVE_ENTITY_FROM_WORLD_ON_CHUNK_FAIL = true;
      @Config.Comment({"[SAFE] Perform operations involving entities on chunk load", " - Allows entities that might not otherwise be captured due to unloaded chunks to be captured", " - Main use-case is copying larger areas with entities"})
      public boolean IMPROVED_ENTITY_EDITS = true;
      @Config.Comment({"Increased debug logging for brush actions and processor setup"})
      public boolean OTHER = false;
      @Config.Comment({"Allow fluids placed by FAWE to tick (flow). This could cause the big lags.", "This has no effect on existing blocks one way or the other.", "Changes due to fluid flow will not be tracked by history, thus may have unintended consequences"})
      public boolean ALLOW_TICK_FLUIDS = false;
      @Config.Comment({"Whether FAWE should use the incubator Vector API to accelerate some operations"})
      public boolean USE_VECTOR_API = false;
   }

   public static class EXTENT {
      @Config.Comment({"Don't bug console when these plugins slow down WorldEdit operations", " - You'll see a message in console or ingame if you need to change this option"})
      public List<String> ALLOWED_PLUGINS = new ArrayList<>(Collections.singleton("com.example.ExamplePlugin"));
      @Config.Comment({"Should debug messages be sent when third party extents are used?"})
      public boolean DEBUG = true;
   }

   public static class GENERAL {
      @Config.Comment({"If the player should be relocated/unstuck when a generation command would bury them"})
      public boolean UNSTUCK_ON_GENERATE = true;
      @Config.Comment({"If unlimited limits should still require /confirm on large. Defaults to limits.default.confirm-large otherwise."})
      public boolean LIMIT_UNLIMITED_CONFIRMS = true;
      @Config.Comment({"If unlimited limits should skip sending creature spawn events. Using //fast will still disable spawn events."})
      public boolean LIMIT_UNLIMITED_SKIPS_CREATURE_SPAWN_EVENTS = true;
   }

   public static class HISTORY {
      @Config.Comment({"Should history be saved on disk:", " - Frees up a lot of memory", " - Persists restarts", " - Unlimited undo", " - Does not affect edit performance if `combine-stages`"})
      public boolean USE_DISK = true;
      @Config.Comment({"Use a database to store disk storage summaries:", " - Enables inspection and rollback", " - Does not impact performance"})
      public boolean USE_DATABASE = true;
      @Config.Comment({"Record history with dispatching:", " - Much faster as it avoids duplicate block checks", " - Slightly worse compression since dispatch order is different"})
      public boolean COMBINE_STAGES = true;
      @Config.Comment({"Do not wait for a chunk's history to save before sending it", " - Undo/redo commands will wait until the history has been written to disk before executing", " - Requires combine-stages = true"})
      public boolean SEND_BEFORE_HISTORY = true;
      @Config.Comment({"Higher compression reduces the size of history at the expense of CPU", "0 = Uncompressed byte array (fastest)", "1 = 1 pass fast compressor (default)", "2 = 2 x fast", "3 = 3 x fast", "4 = 1 x medium, 1 x fast", "5 = 1 x medium, 2 x fast", "6 = 1 x medium, 3 x fast", "7 = 1 x high, 1 x medium, 1 x fast", "8 = 1 x high, 1 x medium, 2 x fast", "9 = 1 x high, 1 x medium, 3 x fast (best compression)", "NOTE: If using disk, do some compression (3+) as smaller files save faster", " - levels over 6 require ZSTD 1.4.8+ to be installed to the system"})
      public int COMPRESSION_LEVEL = 3;
      @Config.Comment({"The buffer size for compression:", " - Larger = better ratio but uses more upfront memory", " - Must be in the range [64, 33554432]"})
      public int BUFFER_SIZE = 531441;
      @Config.Comment({"Delete history on disk after a number of days"})
      public int DELETE_AFTER_DAYS = 7;
      @Config.Comment({"Delete history in memory on logout (does not effect disk)"})
      public boolean DELETE_ON_LOGOUT = true;
      @Config.Comment({"Delete history on disk on logout"})
      @Config.CopiedFrom("history.delete-on-logout")
      public boolean DELETE_DISK_ON_LOGOUT = false;
      @Config.Comment({"If history should be enabled by default for plugins using WorldEdit:", " - It is faster to have disabled", " - It is faster to have disabled", " - Use of the FAWE API will not be effected"})
      public boolean ENABLE_FOR_CONSOLE = true;
      @Config.Comment({"Should redo information be stored:", " - History is about 20% larger", " - Enables use of /redo"})
      public boolean STORE_REDO = true;
      @Config.Comment({"Assumes all edits are smaller than 4096x256x4096:", " - Reduces history size by ~10%"})
      public boolean SMALL_EDITS = false;
   }

   public static class LIGHTING {
      @Config.Comment({"If packet sending should be delayed until relight is finished"})
      public boolean DELAY_PACKET_SENDING = true;
      public boolean ASYNC = true;
      @Config.Comment({"The relighting mode to use:", " - 0 = None (Do no relighting)", " - 1 = Optimal (Relight changed light sources and changed blocks)", " - 2 = All (Slowly relight every blocks)"})
      public int MODE = 1;
      @Config.Comment({"If existing lighting should be removed before relighting"})
      public boolean REMOVE_FIRST = true;
   }

   @Config.Comment({"The \"default\" limit group affects those without a specific limit permission.", "To grant someone different limits, copy the default limits group", "and give it a different name (e.g. newbie). Then give the user the limit ", "permission node with that limit name (e.g. fawe.limit.newbie  )"})
   @Config.BlockName({"default"})
   public static class LIMITS extends Config.ConfigBlock {
      @Config.Comment({"Max actions that can be run concurrently (i.e. commands)"})
      public int MAX_ACTIONS = 1;
      @Config.Comment({"Max number of block changes (e.g. by `//set stone`)."})
      public long MAX_CHANGES = 50000000L;
      @Config.Comment({"Max number of blocks checked (e.g. `//count stone` which doesn't change blocks)"})
      public long MAX_CHECKS = 50000000L;
      @Config.Comment({"Number of times a change can fail (e.g. if the player can't access that region)"})
      public int MAX_FAILS = 50000000;
      @Config.Comment({"Allowed brush iterations (e.g. `//brush smooth`)"})
      public int MAX_ITERATIONS = 1000;
      @Config.Comment({"Max allowed entities (e.g. cows)"})
      public int MAX_ENTITIES = 1337;
      @Config.Comment({"Max allowed radius (e.g. for //sphere)"})
      public int MAX_RADIUS;
      @Config.Comment({"Max allowed superpickaxe size"})
      public int MAX_SUPER_PICKAXE_SIZE;
      @Config.Comment({"Max allowed brush radius"})
      public int MAX_BRUSH_RADIUS;
      @Config.Comment({"Max allowed butcher radius"})
      public int MAX_BUTCHER_RADIUS;
      @Config.Comment({"Blockstates include Banner, Beacon, BrewingStand, Chest, CommandBlock, ", "CreatureSpawner, Dispenser, Dropper, EndGateway, Furnace, Hopper, Jukebox, ", "NoteBlock, Sign, Skull, Structure"})
      public int MAX_BLOCKSTATES;
      @Config.Comment({"Maximum size of the player's history in Megabytes:", " - History on disk or memory will be deleted"})
      public int MAX_HISTORY_MB;
      @Config.Comment({"Sets a maximum limit (in kb) for the size of a player's schematics directory (per-player mode only)", "Set to -1 to disable"})
      @Config.Migrate("experimental.per-player-file-size-limit")
      public int SCHEM_FILE_SIZE_LIMIT;
      @Config.Comment({"Sets a maximum limit for the amount of schematics in a player's schematics directory (per-player mode only)", "Set to -1 to disable"})
      @Config.Migrate("experimental.per-player-file-num-limit")
      public int SCHEM_FILE_NUM_LIMIT;
      @Config.Comment({"Maximum time in milliseconds //calc can execute"})
      public int MAX_EXPRESSION_MS;
      @Config.Comment({"Cinematic block placement:", " - Adds a delay to block placement (nanoseconds/block)", " - Having an artificial delay will use more CPU/Memory"})
      public int SPEED_REDUCTION;
      @Config.Comment({"Place chunks instead of individual blocks:", " - Disabling this will negatively impact performance", " - Only disable this for compatibility or cinematic placement"})
      public boolean FAST_PLACEMENT;
      @Config.Comment({"Should WorldEdit use inventory?", "0 = No inventory usage (creative)", "1 = Inventory for removing and placing (freebuild)", "2 = Inventory for placing (survival)"})
      public int INVENTORY_MODE;
      @Config.Comment({"Should large edits require confirmation (>16384 chunks)"})
      public boolean CONFIRM_LARGE;
      @Config.Comment({"If undo and redo commands should be restricted to allowed regions", " - Prevents scenarios where players can delete/reset a region, and then continue to undo/redo on it"})
      public boolean RESTRICT_HISTORY_TO_REGIONS;
      @Config.Comment({"List of nbt tags to strip from blocks, e.g. Items"})
      public List<String> STRIP_NBT;
      @Config.Comment({"If the disallowed blocks listed in worldedit-config.yml should be disallowed in all edits,", "not just where blocks patterns are used.", " - Can prevent blocks being pasted from clipboards, etc.", " - If fast-placement is disabled, this may cause edits to be slower."})
      public boolean UNIVERSAL_DISALLOWED_BLOCKS;
      @Config.Comment({"If legacy, mumerical, blocks IDs should be able to be used (i.e. 12:2)."})
      public boolean ALLOW_LEGACY;
      @Config.Comment({"If sending creature spawn events should be skipped upon creation.", " - Will not work when setting entities directly to a world, rather than using an EditSession(Builder)", " - Setting fast-placement to false forces writing directly to the world (for example)."})
      public boolean SKIP_CREATURE_SPAWN_EVENTS;
      @Config.Comment({"List of blocks to deny use of. Can be either an entire block type or a block with a specific property value.", "Where block properties are specified, any blockstate with the property will be disallowed (e.g. all directions", "of a waterlogged fence). For blocking/remapping of all occurrences of a property like waterlogged, see", "remap-properties below.", "To generate a blank list, substitute the default content with a set of square brackets [] instead.", "The 'worldedit.anyblock' permission is not considered here.", "Example block property blocking:", " - \"minecraft:conduit[waterlogged=true]\"", " - \"minecraft:piston[extended=false,facing=west]\"", " - \"minecraft:wheat[age=7]\""})
      public List<String> DISALLOWED_BLOCKS;
      @Config.Comment({"List of block properties that should be remapped if used in an edit. Entries should take the form", "\"property_name[value1_old:value1_new,value2_old:value2_new]\". For example:", " - \"waterlogged[true:false]\"", " - \"age[7:4,6:4,5:4]\"", " - \"extended[true:false]\""})
      public List<String> REMAP_PROPERTIES;

      public LIMITS() {
         this.MAX_RADIUS = LocalConfiguration.MAX_RADIUS;
         this.MAX_SUPER_PICKAXE_SIZE = LocalConfiguration.MAX_SUPER_RADIUS;
         this.MAX_BRUSH_RADIUS = LocalConfiguration.MAX_BRUSH_RADIUS;
         this.MAX_BUTCHER_RADIUS = LocalConfiguration.MAX_BUTCHER_RADIUS;
         this.MAX_BLOCKSTATES = 1337;
         this.MAX_HISTORY_MB = -1;
         this.SCHEM_FILE_SIZE_LIMIT = -1;
         this.SCHEM_FILE_NUM_LIMIT = -1;
         this.MAX_EXPRESSION_MS = 50;
         this.SPEED_REDUCTION = 0;
         this.FAST_PLACEMENT = true;
         this.INVENTORY_MODE = 0;
         this.CONFIRM_LARGE = true;
         this.RESTRICT_HISTORY_TO_REGIONS = true;
         this.STRIP_NBT = new ArrayList<>();
         this.UNIVERSAL_DISALLOWED_BLOCKS = true;
         this.ALLOW_LEGACY = true;
         this.SKIP_CREATURE_SPAWN_EVENTS = false;
         this.DISALLOWED_BLOCKS = Arrays.asList("minecraft:wheat", "minecraft:fire", "minecraft:redstone_wire");
         this.REMAP_PROPERTIES = new ArrayList<>();
      }
   }

   @Config.Comment({"Paths for various directories"})
   public static final class PATHS {
      @Config.Comment({"Put any minecraft or mod jars for FAWE to be aware of block textures"})
      public String TEXTURES = "textures";
      public String HEIGHTMAP = "heightmap";
      public String HISTORY = "history";
      @Config.Comment({"Multiple servers can use the same clipboards", " - Use a shared directory or NFS/Samba"})
      public String CLIPBOARD = "clipboard";
      @Config.Comment({"Each player has his or her own sub directory for schematics"})
      public boolean PER_PLAYER_SCHEMATICS = false;
   }

   @Config.Comment({"This relates to how FAWE places chunks"})
   public static class QUEUE {
      @Config.Create
      public static Settings.QUEUE.PROGRESS PROGRESS;
      @Config.Comment({"This should equal the number of processors you have"})
      public int PARALLEL_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors());
      @Config.Comment({"When doing edits that effect more than this many chunks:", " - FAWE will start placing before all calculations are finished", " - A larger value will use slightly less CPU time", " - A smaller value will reduce memory usage", " - A value too small may break some operations (deform?)", " - Values smaller than the configured parallel-threads are not accepted", " - It is recommended this option be at least 4x greater than parallel-threads"})
      public int TARGET_SIZE = 8 * Runtime.getRuntime().availableProcessors();
      @Config.Comment({"Increase or decrease queue intensity (ms) [-50,50]:", "    0 = balance of performance / stability", "    -10 = Allocate 10ms less for chunk placement", "Too high can cause lag spikes (you might be okay with this)", "Too low will result in slow edits"})
      public int EXTRA_TIME_MS = 0;
      @Config.Comment({"Loading the right amount of chunks beforehand can speed up operations", " - Low values may result in FAWE waiting on requests to the main thread", " - Higher values use more memory and isn't noticeably faster", " - A good (relatively) safe way to set this is", " - Use 128 x GB of RAM / number of players expected to be using WE at the same time", " - Paper and derivatives only. (requires delay-chunk-unloads-by to be set)."})
      public int PRELOAD_CHUNK_COUNT = 512;
      @Config.Comment({"If pooling is enabled (reduces GC, higher memory usage)", " - Enable to improve performance at the expense of memory"})
      public boolean POOL = true;
      @Config.Comment({"If chunk loading for writing edits to the world should be performed asynchronously to FAWE", " - Enable to improve performance at the expense of memory", " - If experience out of memory crashed, disable this or reduce slower-memory-percent"})
      public boolean ASYNC_CHUNK_LOAD_WRITE = true;
      @Config.Comment({"Percentage of queue.target-size to use per thread in multi-threaded operations", " - Minimum of 100 / queue.parallel-threads (queue.target-size split across threads)", " - Maximum of 100 (queue.target-size per thread)", " - Higher performance at the expense of memory", " - I.e. target-size=400, parallel-threads=8 and threads-target-size=25 means target-size of 100 per thread", " - Defaults to 100 * 2 / parallel-threads"})
      @Config.ComputedFrom(
         node = "queue.parallel-threads",
         computer = ConfigOptComputation.THREAD_TARGET_SIZE_COMPUTATION.class
      )
      public int THREAD_TARGET_SIZE_PERCENT = 200 / Runtime.getRuntime().availableProcessors();

      public static class PROGRESS {
         @Config.Comment({"Display constant titles about the progress of a user's edit", " - false = disabled", " - title = Display progress titles", " - chat = Display progress in chat", " - Currently not implemented"})
         public String DISPLAY = "false";
         @Config.Comment({"How often edit progress is displayed"})
         public int INTERVAL = 1;
         @Config.Comment({"Delay sending progress in milliseconds (so quick edits don't spam)"})
         public int DELAY = 5000;
      }
   }

   @Config.Comment({"Region restriction settings"})
   public static final class REGION_RESTRICTIONS_OPTIONS {
      @Config.Comment({"What type of users are allowed to WorldEdit in a region", " - MEMBER = Players added to a region", " - OWNER = Players who own the region"})
      public String MODE = "MEMBER";
      @Config.Comment({"Allow region blacklists.", " - Currently only implemented for WorldGuard", " - see region-restrictions-options.worldguard-region-blacklist"})
      public boolean ALLOW_BLACKLISTS = false;
      @Config.Comment({"List of plugin mask managers that should be exclusive. Exclusive managers are not ", "checked for edit restrictions if another manager already allowed an edit, and further ", "managers are not checked if an exclusive manager allows an edit.", " - May be useful to add PlotSquared if using both P2 and WorldGuard on a server", " - Some custom-implementations in other plugins may override this setting"})
      public List<String> EXCLUSIVE_MANAGERS = new ArrayList<>(Collections.singleton("ExamplePlugin"));
      @Config.Comment({"If a worldguard-protected world should be considered as a region blacklist.", " - This will create a blacklist of regions where an edit cannot operate.", " - Useful for a \"freebuild\" worlds with few protected areas.", " - May cause performance loss with large numbers of protected areas.", " - Requires region-restrictions-options.allow-blacklists be true.", " - Will still search for current allowed regions to limit the edit to.", " - Any blacklist regions are likely to override any internal allowed regions."})
      public boolean WORLDGUARD_REGION_BLACKLIST = false;
      @Config.Comment({"Restrict all edits to within the safe chunk limits of +/- 30 million blocks", " - Edits outside this range may induce crashing", " - Forcefully prevents any edit outside this range"})
      public boolean RESTRICT_TO_SAFE_RANGE = true;
   }

   @Deprecated(
      since = "2.0.0"
   )
   @Config.Comment({"Generic tick limiter (not necessarily WorldEdit related, but useful to stop abuse)"})
   public static class TICK_LIMITER {
      @Config.Comment({"Enable the limiter"})
      public boolean ENABLED = false;
      @Config.Comment({"The interval in ticks"})
      public int INTERVAL = 20;
      @Config.Comment({"Max falling blocks per interval (per chunk)"})
      public int FALLING = 64;
      @Config.Comment({"Max physics per interval (excluding redstone)"})
      public int PHYSICS_MS = 10;
      @Config.Comment({"Max item spawns per interval (per chunk)"})
      public int ITEMS = 256;
   }

   @Config.Comment({"Web/HTTP connection related settings"})
   public static class WEB {
      @Config.Comment({"The web interface for clipboards", " - All schematics are anonymous and private", " - Downloads can be deleted by the user", " - Supports clipboard uploads, downloads and saves"})
      public String URL = "https://schem.intellectualsites.com/fawe/";
      @Config.Comment({"The url of the backend server (Arkitektonika)"})
      public String ARKITEKTONIKA_BACKEND_URL = "https://api.schematic.cloud/";
      @Config.Comment({"The url used to generate a download link from.", "{key} will be replaced with the generated key"})
      public String ARKITEKTONIKA_DOWNLOAD_URL = "https://schematic.cloud/download/{key}";
      @Config.Comment({"The url used to generate a deletion link from.", "{key} will be replaced with the generated key"})
      public String ARKITEKTONIKA_DELETE_URL = "https://schematic.cloud/delete/{key}";
      @Config.Comment({"The maximum amount of time in seconds the plugin can attempt to load images for."})
      public int MAX_IMAGE_LOAD_TIME = 5;
      @Config.Comment({"The maximum size (width x length) an image being loaded can be.", " - 8294400 is 3840x2160"})
      public int MAX_IMAGE_SIZE = 8294400;
      @Config.Comment({"Whitelist of hostnames to allow images to be downloaded from", " - Adding '*' to the list will allow any host, but this is NOT adviseable", " - Crash exploits exist with malformed images", " - See: https://medium.com/chargebee-engineering/perils-of-parsing-pixel-flood-attack-on-java-imageio-a97aeb06637d"})
      public List<String> ALLOWED_IMAGE_HOSTS = new ArrayList<>(Collections.singleton("i.imgur.com"));
   }
}

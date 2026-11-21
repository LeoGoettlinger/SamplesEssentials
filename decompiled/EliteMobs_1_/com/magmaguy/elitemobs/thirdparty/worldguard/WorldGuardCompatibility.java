package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import lombok.Generated;
import org.bukkit.Bukkit;

public class WorldGuardCompatibility {
   private static final State allow = State.ALLOW;
   private static final State deny = State.DENY;
   private static StateFlag ELITEMOBS_SPAWN_FLAG;
   private static StateFlag ELITEMOBS_ONLY_SPAWN_FLAG;
   private static StateFlag ELITEMOBS_ANTIEXPLOIT;
   private static StateFlag ELITEMOBS_DUNGEON;
   private static StateFlag ELITEMOBS_EVENTS;
   private static IntegerFlag ELITEMOBS_MINIMUM_LEVEL;
   private static IntegerFlag ELITEMOBS_MAXIMUM_LEVEL;
   private static StateFlag ELITEMOBS_EXPLOSION_REGEN;
   private static StateFlag ELITEMOBS_EXPLOSION_BLOCK_DAMAGE;

   public static boolean initialize() {
      if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
         return false;
      } else {
         Logger.info(" WorldGuard detected.");
         FlagRegistry registry = null;

         try {
            registry = WorldGuard.getInstance().getFlagRegistry();
         } catch (Exception var11) {
            Logger.warn("Something went wrong while loading WorldGuard. Are you using the right WorldGuard version?");
            return false;
         }

         try {
            StateFlag spawn = register(registry, new StateFlag("elitemob-spawning", true), StateFlag.class);
            StateFlag onlySpawn = register(registry, new StateFlag("elitemob-only-spawning", false), StateFlag.class);
            StateFlag antiExploit = register(registry, new StateFlag("elitemobs-antiexploit", true), StateFlag.class);
            StateFlag dungeon = register(registry, new StateFlag("elitemobs-dungeon", false), StateFlag.class);
            StateFlag events = register(registry, new StateFlag("elitemobs-events", true), StateFlag.class);
            IntegerFlag minimum = register(registry, new IntegerFlag("elitemobs-minimum-level"), IntegerFlag.class);
            IntegerFlag maximum = register(registry, new IntegerFlag("elitemobs-maximum-level"), IntegerFlag.class);
            StateFlag regeneration = register(registry, new StateFlag("elitemobs-explosion-regen", true), StateFlag.class);
            StateFlag blockDamage = register(registry, new StateFlag("elitemobs-explosion-block-damage", true), StateFlag.class);
            ELITEMOBS_SPAWN_FLAG = spawn;
            ELITEMOBS_ONLY_SPAWN_FLAG = onlySpawn;
            ELITEMOBS_ANTIEXPLOIT = antiExploit;
            ELITEMOBS_DUNGEON = dungeon;
            ELITEMOBS_EVENTS = events;
            ELITEMOBS_MINIMUM_LEVEL = minimum;
            ELITEMOBS_MAXIMUM_LEVEL = maximum;
            ELITEMOBS_EXPLOSION_REGEN = regeneration;
            ELITEMOBS_EXPLOSION_BLOCK_DAMAGE = blockDamage;
            return true;
         } catch (IllegalStateException var10) {
            Logger.warn("Could not initialize EliteMobs WorldGuard flags: " + var10.getMessage());
            return false;
         }
      }
   }

   private static <T extends Flag<?>> T register(FlagRegistry registry, T requested, Class<T> type) {
      try {
         registry.register(requested);
         return requested;
      } catch (IllegalStateException | FlagConflictException var5) {
         Flag<?> existing = registry.get(requested.getName());
         if (!type.isInstance(existing)) {
            throw new IllegalStateException("Flag " + requested.getName() + " is missing or is not a " + type.getSimpleName(), var5);
         } else {
            return type.cast(existing);
         }
      }
   }

   @Generated
   public static State getAllow() {
      return allow;
   }

   @Generated
   public static State getDeny() {
      return deny;
   }

   @Generated
   public static StateFlag getELITEMOBS_SPAWN_FLAG() {
      return ELITEMOBS_SPAWN_FLAG;
   }

   @Generated
   public static StateFlag getELITEMOBS_ONLY_SPAWN_FLAG() {
      return ELITEMOBS_ONLY_SPAWN_FLAG;
   }

   @Generated
   public static StateFlag getELITEMOBS_ANTIEXPLOIT() {
      return ELITEMOBS_ANTIEXPLOIT;
   }

   @Generated
   public static StateFlag getELITEMOBS_DUNGEON() {
      return ELITEMOBS_DUNGEON;
   }

   @Generated
   public static StateFlag getELITEMOBS_EVENTS() {
      return ELITEMOBS_EVENTS;
   }

   @Generated
   public static IntegerFlag getELITEMOBS_MINIMUM_LEVEL() {
      return ELITEMOBS_MINIMUM_LEVEL;
   }

   @Generated
   public static IntegerFlag getELITEMOBS_MAXIMUM_LEVEL() {
      return ELITEMOBS_MAXIMUM_LEVEL;
   }

   @Generated
   public static StateFlag getELITEMOBS_EXPLOSION_REGEN() {
      return ELITEMOBS_EXPLOSION_REGEN;
   }

   @Generated
   public static StateFlag getELITEMOBS_EXPLOSION_BLOCK_DAMAGE() {
      return ELITEMOBS_EXPLOSION_BLOCK_DAMAGE;
   }
}

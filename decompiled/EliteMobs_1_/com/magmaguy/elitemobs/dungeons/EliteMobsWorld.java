package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.magmacore.instance.InstanceProtector;
import com.magmaguy.elitemobs.magmacore.instance.WorldProtectionRules;
import com.magmaguy.elitemobs.magmacore.util.TemporaryWorldManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class EliteMobsWorld {
   private static final HashMap<UUID, EliteMobsWorld> eliteMobsWorlds = new HashMap<>();
   private static final Set<String> loadingWorlds = new HashSet<>();
   private final ContentPackagesConfigFields contentPackagesConfigFields;
   private final boolean allowExplosions;

   private EliteMobsWorld(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
      this.contentPackagesConfigFields = contentPackagesConfigFields;
      this.allowExplosions = contentPackagesConfigFields.isAllowExplosions();
      eliteMobsWorlds.put(worldUUID, this);
      if (contentPackagesConfigFields.isProtect()) {
         World world = Bukkit.getWorld(worldUUID);
         if (world != null) {
            InstanceProtector.addProtectedWorld(world, rulesFor(contentPackagesConfigFields));
         }
      }
   }

   private static WorldProtectionRules rulesFor(ContentPackagesConfigFields fields) {
      return new WorldProtectionRules()
         .setAllowExplosions(fields.isAllowExplosions())
         .setAllowLiquidFlow(fields.isAllowLiquidFlow())
         .setAllowElytra(fields.isAllowElytra())
         .setPreventFlyToggle(CombatTagConfig.isPreventFlyToggleInDungeons())
         .setPreventFriendlyFire(!DungeonsConfig.isFriendlyFireInDungeons())
         .setPreventVanillaMobSpawning(true)
         .setFireDamageMultiplier(DungeonsConfig.getFireDamageMultiplier())
         .setPoisonDamageMultiplier(DungeonsConfig.getPoisonDamageMultiplier())
         .setWitherDamageMultiplier(DungeonsConfig.getWitherDamageMultiplier());
   }

   public static void shutdown() {
      eliteMobsWorlds.keySet().removeIf(worldUUID -> Bukkit.getWorld(worldUUID) == null);
   }

   @Nullable
   public static EliteMobsWorld getEliteMobsWorld(UUID worldUUID) {
      return eliteMobsWorlds.get(worldUUID);
   }

   public static boolean isEliteMobsWorld(UUID worldUUID) {
      if (eliteMobsWorlds.containsKey(worldUUID)) {
         return true;
      } else {
         World world = Bukkit.getWorld(worldUUID);
         return world != null && loadingWorlds.contains(world.getName());
      }
   }

   public static World loadWorld(String worldName, Environment environment, ContentPackagesConfigFields fields) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Dungeon worlds must load on the server thread");
      } else {
         boolean added = loadingWorlds.add(worldName);

         World var5;
         try {
            World world = TemporaryWorldManager.loadVoidTemporaryWorld(worldName, environment);
            if (world != null) {
               create(world.getUID(), fields);
            }

            var5 = world;
         } finally {
            if (added) {
               loadingWorlds.remove(worldName);
            }
         }

         return var5;
      }
   }

   public static void create(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
      new EliteMobsWorld(worldUUID, contentPackagesConfigFields);
   }

   public static void destroy(UUID worldUUID) {
      EliteMobsWorld removed = eliteMobsWorlds.remove(worldUUID);
      if (removed != null) {
         World world = Bukkit.getWorld(worldUUID);
         if (world != null) {
            InstanceProtector.removeProtectedWorld(world);
         }
      }
   }

   @Generated
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Generated
   public boolean isAllowExplosions() {
      return this.allowExplosions;
   }
}

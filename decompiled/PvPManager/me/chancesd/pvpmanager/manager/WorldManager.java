package me.chancesd.pvpmanager.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldManager {
   private final Map<String, CombatWorld> worlds = new HashMap<>();
   private final Storage storage;

   public WorldManager(PvPManager plugin) {
      this.storage = plugin.getStorageManager().getStorage();
      this.loadWorlds();
   }

   private void loadWorlds() {
      for (World world : Bukkit.getWorlds()) {
         this.loadWorld(world);
      }
   }

   @NotNull
   private CombatWorld loadWorld(World world) {
      Map<String, Object> worldData = this.storage.getWorldData(world.getName());
      CombatWorld combatWorld;
      if (worldData.isEmpty()) {
         combatWorld = this.createDefaultCombatWorld(world);
      } else {
         combatWorld = this.loadWorldData(world, worldData);
      }

      this.worlds.put(world.getName(), combatWorld);
      return combatWorld;
   }

   @NotNull
   private CombatWorld loadWorldData(World world, Map<String, Object> worldData) {
      boolean pvpState = true;
      CombatWorld.WorldOptionState forcePVP = CombatWorld.WorldOptionState.NONE;
      Object pvpstate = worldData.get("pvp");
      if (pvpstate instanceof Integer) {
         pvpState = (Integer)pvpstate != 0;
      } else if (pvpstate instanceof Boolean) {
         pvpState = (Boolean)pvpstate;
      }

      if (worldData.get("forcepvp") instanceof String string) {
         forcePVP = CombatWorld.WorldOptionState.valueOf(string);
      }

      return new CombatWorld(world.getUID(), world.getName(), pvpState, forcePVP);
   }

   @NotNull
   private CombatWorld createDefaultCombatWorld(World world) {
      return new CombatWorld(world.getUID(), world.getName(), true, CombatWorld.WorldOptionState.NONE);
   }

   public void saveWorldData(CombatWorld combatWorld) {
      ScheduleUtils.runAsync(() -> this.storage.saveWorldData(combatWorld.getName(), combatWorld.getWorldData()));
   }

   @NotNull
   public CombatWorld getWorld(World world) {
      CombatWorld combatWorld = this.getWorld(world.getName());
      if (combatWorld == null) {
         Log.infoColor(ChatColor.GREEN + "Loaded new combat world: " + ChatColor.GOLD + world.getName());
         return this.loadWorld(world);
      } else {
         return combatWorld;
      }
   }

   @Nullable
   public CombatWorld getWorld(String worldName) {
      return this.worlds.get(worldName);
   }

   public Collection<CombatWorld> getWorlds() {
      return this.worlds.values();
   }
}

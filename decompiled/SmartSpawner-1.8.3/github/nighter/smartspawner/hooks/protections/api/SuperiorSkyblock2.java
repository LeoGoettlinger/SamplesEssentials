package github.nighter.smartspawner.hooks.protections.api;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class SuperiorSkyblock2 implements Listener, ProtectionHook {
   private static final String SPAWNER_STACK_PERM = "spawner_stack";
   private static final String SPAWNER_OPEN_MENU_PERM = "spawner_open_menu";
   private static IslandPrivilege SPAWNER_STACK;
   private static IslandPrivilege SPAWNER_OPEN_MENU;
   private static boolean registered = false;

   public SuperiorSkyblock2() {
      register();
   }

   public static void register() {
      if (!registered) {
         SPAWNER_STACK = getOrRegisterPrivilege("spawner_stack");
         SPAWNER_OPEN_MENU = getOrRegisterPrivilege("spawner_open_menu");
         registered = true;
      }
   }

   private static IslandPrivilege getOrRegisterPrivilege(String name) {
      try {
         return IslandPrivilege.getByName(name);
      } catch (NullPointerException var2) {
         IslandPrivilege.register(name);
         return IslandPrivilege.getByName(name);
      }
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      Island island = SuperiorSkyblockAPI.getIslandAt(location);
      return island != null ? island.hasPermission(SuperiorSkyblockAPI.getPlayer(player.getUniqueId()), SPAWNER_STACK) : true;
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      Island island = SuperiorSkyblockAPI.getIslandAt(location);
      return island != null ? island.hasPermission(SuperiorSkyblockAPI.getPlayer(player.getUniqueId()), SPAWNER_OPEN_MENU) : true;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onIslandDisband(IslandDisbandEvent event) {
      if (!event.isCancelled() && event.getIsland() != null) {
         Island island = event.getIsland();

         for (Dimension dimension : Dimension.values()) {
            try {
               island.getAllChunksAsync(dimension, 3, chunk -> {
                  for (BlockState state : chunk.getTileEntities(block -> block.getType() == Material.SPAWNER, false)) {
                     SpawnerData spawner = SmartSpawner.getInstance().getSpawnerManager().getSpawnerByLocation(state.getBlock().getLocation());
                     if (spawner != null) {
                        SmartSpawner.getInstance().getSpawnerManager().removeGhostSpawner(spawner.getSpawnerId());
                     }
                  }
               });
            } catch (NullPointerException var6) {
            }
         }
      }
   }
}

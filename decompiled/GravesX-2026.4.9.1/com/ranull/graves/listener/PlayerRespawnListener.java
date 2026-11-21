package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerRespawnListener implements Listener {
   private final Graves plugin;

   public PlayerRespawnListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      List<String> permissionList = this.plugin.getConfigManager().getPermissionList(player);
      List<Grave> graveList = this.plugin.getGraveManager().getGraveList(player);
      if (!graveList.isEmpty()) {
         Grave grave = graveList.get(graveList.size() - 1);
         Location respawnLoc = event.getRespawnLocation().clone();
         this.scheduleRespawnFunction(player, permissionList, grave, respawnLoc);
         this.applyPotionEffectIfWithinTime(player, permissionList, grave, respawnLoc);
         if (this.shouldGiveCompass(player, permissionList, grave)) {
            this.giveCompassToPlayer(event, player, grave, respawnLoc);
         }
      }
   }

   private void scheduleRespawnFunction(Player player, List<String> permissionList, Grave grave, Location respawnLoc) {
      this.plugin
         .getSchedulerManager()
         .runTaskLater(
            () -> this.plugin
               .getSchedulerManager()
               .execute(
                  respawnLoc,
                  () -> this.plugin
                     .getEntityManager()
                     .runFunction(
                        player,
                        this.plugin.getConfigManager().getConfigSection("respawn.function", player, permissionList).getString("respawn.function", "none"),
                        grave
                     )
               ),
            1L
         );
   }

   private void applyPotionEffectIfWithinTime(Player player, List<String> permissionList, Grave grave, Location respawnLoc) {
      this.plugin
         .getSchedulerManager()
         .runTaskLater(
            () -> this.plugin
               .getSchedulerManager()
               .execute(
                  respawnLoc,
                  () -> {
                     boolean enabled = this.plugin
                        .getConfigManager()
                        .getConfigSection("respawn.potion-effect", player, permissionList)
                        .getBoolean("respawn.potion-effect");
                     boolean hasPerm = this.plugin.getPermissionManager().hasGrantedPermission("graves.potion-effect", player);
                     if (enabled && hasPerm) {
                        long limitMs = this.plugin
                              .getConfigManager()
                              .getConfigSection("respawn.potion-effect-time-limit", player, permissionList)
                              .getInt("respawn.potion-effect-time-limit")
                           * 1000L;
                        if (grave.getLivedTime() <= limitMs) {
                           int durationTicks = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("respawn.potion-effect-duration", player, permissionList)
                                 .getInt("respawn.potion-effect-duration")
                              * 20;
                           PotionEffect resist = new PotionEffect(
                              this.plugin.getVersionManager().getPotionEffectTypeFromVersion("RESISTANCE"), durationTicks, 4
                           );
                           PotionEffect fireResist = new PotionEffect(
                              this.plugin.getVersionManager().getPotionEffectTypeFromVersion("FIRE_RESISTANCE"), durationTicks, 0
                           );
                           player.addPotionEffect(resist);
                           player.addPotionEffect(fireResist);
                        }
                     }
                  }
               ),
            1L
         );
   }

   private boolean shouldGiveCompass(Player player, List<String> permissionList, Grave grave) {
      return this.plugin.getVersionManager().hasCompassMeta()
         && this.plugin.getConfigManager().getConfigSection("respawn.compass", player, permissionList).getBoolean("respawn.compass")
         && grave.getLivedTime()
            <= this.plugin.getConfigManager().getConfigSection("respawn.compass-time", player, permissionList).getInt("respawn.compass-time") * 1000L;
   }

   private void giveCompassToPlayer(PlayerRespawnEvent event, Player player, Grave grave, Location respawnLoc) {
      this.plugin.getSchedulerManager().runTaskLater(() -> this.plugin.getSchedulerManager().execute(respawnLoc, () -> {
         List<Location> targets = this.plugin.getGraveManager().getGraveLocationList(event.getRespawnLocation(), grave);
         if (!targets.isEmpty()) {
            ItemStack compass = this.plugin.getEntityManager().createGraveCompass(player, targets.get(0), grave);
            if (compass != null) {
               player.getInventory().addItem(new ItemStack[]{compass});
            }
         }
      }), 1L);
   }
}

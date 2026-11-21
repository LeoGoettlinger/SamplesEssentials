package dev.cwhead.GravesX.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.manager.SchedulerManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public final class PlayerAfterRespawnListener implements Listener {
   private final Graves plugin;
   private final Map<UUID, Long> lastHandled = new ConcurrentHashMap<>();
   private static final long DEDUPE_MS = 1500L;

   public PlayerAfterRespawnListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPostRespawn(PlayerPostRespawnEvent event) {
      Player player = event.getPlayer();
      Location respawnLoc = event.getRespawnedLocation();
      if (!this.dedupe(player)) {
         this.handleAfterRespawn(player, respawnLoc);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBukkitRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      Location respawnLoc = event.getRespawnLocation();
      if (!this.dedupe(player)) {
         this.handleAfterRespawn(player, respawnLoc);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      long start = System.currentTimeMillis();
      SchedulerManager.Task[] ref = new SchedulerManager.Task[1];
      ref[0] = this.plugin.getSchedulerManager().returnable().runTaskTimer(() -> {
         if (System.currentTimeMillis() - start > 10000L) {
            ref[0].cancel();
         } else {
            if (!player.isDead()) {
               if (!this.dedupe(player)) {
                  this.handleAfterRespawn(player, player.getLocation());
               }

               ref[0].cancel();
            }
         }
      }, 20L, 20L);
   }

   private void handleAfterRespawn(Player player, Location respawnLoc) {
      List<String> perms = this.plugin.getPermissionList(player);
      List<Grave> graves = this.plugin.getGraveManager().getGraveList(player);
      if (!graves.isEmpty()) {
         Grave grave = graves.get(graves.size() - 1);
         Location anchor = (respawnLoc != null ? respawnLoc : player.getLocation()).clone();
         this.plugin
            .getSchedulerManager()
            .runTaskLater(
               () -> this.plugin
                  .getSchedulerManager()
                  .execute(
                     anchor,
                     () -> this.plugin
                        .getEntityManager()
                        .runFunction(
                           player,
                           this.plugin.getConfigManager().getConfigSection("respawn.function", player, perms).getString("respawn.function", "none"),
                           grave
                        )
                  ),
               1L
            );
         this.plugin
            .getSchedulerManager()
            .runTaskLater(
               () -> this.plugin
                  .getSchedulerManager()
                  .execute(
                     anchor,
                     () -> {
                        boolean enabled = this.plugin
                           .getConfigManager()
                           .getConfigSection("respawn.potion-effect", player, perms)
                           .getBoolean("respawn.potion-effect");
                        boolean hasPerm = this.plugin.getPermissionManager().hasGrantedPermission("graves.potion-effect", player);
                        if (enabled && hasPerm) {
                           long limitMs = this.plugin
                                 .getConfigManager()
                                 .getConfigSection("respawn.potion-effect-time-limit", player, perms)
                                 .getInt("respawn.potion-effect-time-limit")
                              * 1000L;
                           if (grave.getLivedTime() <= limitMs) {
                              int dur = this.plugin
                                    .getConfigManager()
                                    .getConfigSection("respawn.potion-effect-duration", player, perms)
                                    .getInt("respawn.potion-effect-duration")
                                 * 20;
                              PotionEffect resist = new PotionEffect(this.plugin.getVersionManager().getPotionEffectTypeFromVersion("RESISTANCE"), dur, 4);
                              PotionEffect fireResist = new PotionEffect(
                                 this.plugin.getVersionManager().getPotionEffectTypeFromVersion("FIRE_RESISTANCE"), dur, 0
                              );
                              player.addPotionEffect(resist);
                              player.addPotionEffect(fireResist);
                           }
                        }
                     }
                  ),
               1L
            );
         if (this.shouldGiveCompass(player, perms, grave)) {
            this.plugin.getSchedulerManager().runTaskLater(() -> this.plugin.getSchedulerManager().execute(anchor, () -> {
               List<Location> locs = this.plugin.getGraveManager().getGraveLocationList(anchor, grave);
               if (!locs.isEmpty()) {
                  ItemStack compass = this.plugin.getEntityManager().createGraveCompass(player, locs.get(0), grave);
                  if (compass != null) {
                     player.getInventory().addItem(new ItemStack[]{compass});
                  }
               }
            }), 1L);
         }
      }
   }

   private boolean shouldGiveCompass(Player player, List<String> permissionList, Grave grave) {
      return this.plugin.getVersionManager().hasCompassMeta()
         && this.plugin.getConfigManager().getConfigSection("respawn.compass", player, permissionList).getBoolean("respawn.compass")
         && grave.getLivedTime()
            <= this.plugin.getConfigManager().getConfigSection("respawn.compass-time", player, permissionList).getInt("respawn.compass-time") * 1000L;
   }

   private boolean dedupe(Player p) {
      long now = System.currentTimeMillis();
      Long prev = this.lastHandled.put(p.getUniqueId(), now);
      return prev != null && now - prev < 1500L;
   }
}

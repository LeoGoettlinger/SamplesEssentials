package org.sethomegui.Managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.Utils.Utils;

public class TeleportManager {
   private final SetHomeGUI plugin;
   private final Map<UUID, ScheduledTask> activeTasks = new HashMap<>();

   public TeleportManager(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public void queueTeleport(Player player, Location targetLocation) {
      UUID uuid = player.getUniqueId();
      this.cancelActiveTeleport(uuid);
      if (player.hasPermission("sethome.cooldown.bypass")) {
         player.teleportAsync(targetLocation).thenAccept(success -> {
            if (success) {
               YamlDocument actionsConfigx = this.plugin.getActionsConfig();
               this.executePostTeleportActions(player, actionsConfigx.getSection("teleported-actions"));
            }
         });
      } else {
         int cooldown = this.plugin.getMainConfig().getInt("teleport-cooldown", 5);
         boolean cancelOnMove = this.plugin.getMainConfig().getBoolean("cancel-on-move", true);
         Location startLoc = player.getLocation().clone();
         YamlDocument actionsConfig = this.plugin.getActionsConfig();
         Section teleportActions = actionsConfig.getSection("teleport-actions");
         this.executePreTeleportActions(player, teleportActions, cooldown, true);
         int[] secondsElapsed = new int[]{0};
         ScheduledTask task = Bukkit.getAsyncScheduler()
            .runAtFixedRate(
               this.plugin,
               scheduledTask -> {
                  if (!player.isOnline()) {
                     scheduledTask.cancel();
                     this.activeTasks.remove(uuid);
                  } else {
                     if (cancelOnMove) {
                        Location current = player.getLocation();
                        if (current.getBlockX() != startLoc.getBlockX()
                           || current.getBlockY() != startLoc.getBlockY()
                           || current.getBlockZ() != startLoc.getBlockZ()) {
                           scheduledTask.cancel();
                           this.activeTasks.remove(uuid);
                           String moveCancelledMsg = this.plugin
                              .getMainConfig()
                              .getString(
                                 "messages.home-action-messages.teleport-cancelled-move", "&#ef6603[SetHomeGUI] &cTeleportation cancelled because you moved!"
                              );
                           player.sendMessage(Utils.color(moveCancelledMsg));
                           player.sendTitle("", "", 0, 0, 0);
                           return;
                        }
                     }

                     secondsElapsed[0]++;
                     int currentRemaining = cooldown - secondsElapsed[0];
                     if (currentRemaining <= 0) {
                        scheduledTask.cancel();
                        this.activeTasks.remove(uuid);
                        player.teleportAsync(targetLocation).thenAccept(success -> {
                           if (success) {
                              this.executePostTeleportActions(player, actionsConfig.getSection("teleported-actions"));
                           }
                        });
                     } else {
                        this.executePreTeleportActions(player, teleportActions, currentRemaining, false);
                     }
                  }
               },
               1L,
               1L,
               TimeUnit.SECONDS
            );
         this.activeTasks.put(uuid, task);
      }
   }

   private void executePreTeleportActions(Player player, Section section, int seconds, boolean isInitialRun) {
      if (section != null) {
         Section titleSec = section.getSection("title");
         if (titleSec != null && titleSec.getBoolean("enabled", false)) {
            String type = titleSec.getString("type", "COOLDOWN");
            if (type.equalsIgnoreCase("COOLDOWN") || type.equalsIgnoreCase("STATIC")) {
               String title = Utils.setPlaceholders(player, titleSec.getString("title", ""), this.plugin);
               String sub = Utils.setPlaceholders(player, titleSec.getString("subtitle", ""), this.plugin).replace("%seconds%", String.valueOf(seconds));
               player.sendTitle(Utils.color(title), Utils.color(sub), 0, 20, 5);
            }
         }

         Section actionbarSec = section.getSection("actionbar");
         if (actionbarSec != null && actionbarSec.getBoolean("enabled", false)) {
            String type = actionbarSec.getString("type", "COOLDOWN");
            if (type.equalsIgnoreCase("COOLDOWN") || type.equalsIgnoreCase("STATIC")) {
               String msg = Utils.setPlaceholders(player, actionbarSec.getString("message", ""), this.plugin).replace("%seconds%", String.valueOf(seconds));
               player.sendActionBar(Utils.color(msg));
            }
         }

         Section msgSec = section.getSection("message");
         if (msgSec != null && msgSec.getBoolean("enabled", false)) {
            String type = msgSec.getString("type", "COOLDOWN");
            if (type.equalsIgnoreCase("COOLDOWN") || type.equalsIgnoreCase("STATIC") && isInitialRun) {
               String msg = Utils.setPlaceholders(player, msgSec.getString("message", ""), this.plugin).replace("%seconds%", String.valueOf(seconds));
               player.sendMessage(Utils.color(msg));
            }
         }
      }
   }

   private void executePostTeleportActions(Player player, Section section) {
      if (section != null) {
         Section titleSec = section.getSection("title");
         if (titleSec != null && titleSec.getBoolean("enabled", false)) {
            String title = Utils.setPlaceholders(player, titleSec.getString("title", ""), this.plugin);
            String sub = Utils.setPlaceholders(player, titleSec.getString("subtitle", ""), this.plugin);
            int ticks = titleSec.getInt("duration", 2) * 20;
            player.sendTitle(Utils.color(title), Utils.color(sub), 10, ticks, 10);
         }

         Section actionbarSec = section.getSection("actionbar");
         if (actionbarSec != null && actionbarSec.getBoolean("enabled", false)) {
            String msg = Utils.setPlaceholders(player, actionbarSec.getString("message", ""), this.plugin);
            player.sendActionBar(Utils.color(msg));
         }

         Section msgSec = section.getSection("message");
         if (msgSec != null && msgSec.getBoolean("enabled", false)) {
            String msg = Utils.setPlaceholders(player, msgSec.getString("message", ""), this.plugin);
            player.sendMessage(Utils.color(msg));
         }
      }
   }

   public void cancelActiveTeleport(UUID uuid) {
      if (this.activeTasks.containsKey(uuid)) {
         this.activeTasks.get(uuid).cancel();
         this.activeTasks.remove(uuid);
      }
   }
}

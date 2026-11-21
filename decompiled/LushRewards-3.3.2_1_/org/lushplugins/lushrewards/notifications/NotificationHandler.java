package org.lushplugins.lushrewards.notifications;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.bukkit.Bukkit;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.morepaperlib.scheduling.ScheduledTask;

public class NotificationHandler {
   private ScheduledTask notificationTask;

   public void startNotificationTask(int reminderPeriod) {
      if (reminderPeriod > 0) {
         int reminderPeriodMs = reminderPeriod * 50;
         if (this.notificationTask != null) {
            this.notificationTask.cancel();
         }

         this.notificationTask = LushRewards.getMorePaperLib()
            .scheduling()
            .asyncScheduler()
            .runAtFixedRate(
               () -> Bukkit.getOnlinePlayers()
                  .forEach(
                     player -> {
                        if (LushRewards.getInstance()
                           .getEnabledRewardModules()
                           .stream()
                           .anyMatch(
                              rewardModule -> rewardModule.shouldNotify()
                                 && rewardModule.hasClaimableRewards(player)
                                 && player.hasPermission("lushrewards.use." + rewardModule.getId())
                           )) {
                           ChatColorHandler.sendMessage(player, LushRewards.getInstance().getConfigManager().getMessage("reminder"));
                           player.playSound(player.getLocation(), LushRewards.getInstance().getConfigManager().getReminderSound(), 1.0F, 1.5F);
                        }
                     }
                  ),
               Duration.of(Math.round(reminderPeriodMs / 3.0), ChronoUnit.MILLIS),
               Duration.of(reminderPeriodMs, ChronoUnit.MILLIS)
            );
      }
   }

   public void stopNotificationTask() {
      if (this.notificationTask != null) {
         this.notificationTask.cancel();
         this.notificationTask = null;
      }
   }

   public void reloadNotifications() {
      this.stopNotificationTask();
      this.startNotificationTask(LushRewards.getInstance().getConfigManager().getReminderPeriod());
   }
}

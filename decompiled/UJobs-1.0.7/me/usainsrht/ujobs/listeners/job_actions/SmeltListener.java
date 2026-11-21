package me.usainsrht.ujobs.listeners.job_actions;

import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class SmeltListener implements Listener {
   private final JobManager jobManager;

   public SmeltListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSmelt(FurnaceExtractEvent e) {
      Player player = e.getPlayer();
      if (!this.jobManager.shouldIgnore(player)) {
         if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.SMELT)) {
            String value = e.getItemType().name();
            int amount = e.getItemAmount();
            if (amount > 0) {
               for (Job job : this.jobManager.getJobsWithAction(BuiltInActions.Material.SMELT)) {
                  this.jobManager.processAction(player, BuiltInActions.Material.SMELT, value, job, amount);
               }
            }
         }
      }
   }
}

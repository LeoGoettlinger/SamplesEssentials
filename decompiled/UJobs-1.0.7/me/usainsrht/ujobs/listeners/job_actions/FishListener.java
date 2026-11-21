package me.usainsrht.ujobs.listeners.job_actions;

import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class FishListener implements Listener {
   JobManager jobManager;

   public FishListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onFish(PlayerFishEvent e) {
      Player player = e.getPlayer();
      if (!this.jobManager.shouldIgnore(player)) {
         if (e.getState() == State.CAUGHT_FISH) {
            if (e.getCaught() instanceof Item item) {
               String var7 = item.getItemStack().getType().name();
               if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.FISH)) {
                  for (Job job : this.jobManager.getJobsWithAction(BuiltInActions.Material.FISH)) {
                     this.jobManager.processAction(player, BuiltInActions.Material.FISH, var7, job, 1);
                  }
               }
            }
         }
      }
   }
}

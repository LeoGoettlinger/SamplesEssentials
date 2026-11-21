package me.usainsrht.ujobs.listeners.job_actions;

import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceListener implements Listener {
   private final JobManager jobManager;

   public PlaceListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlace(BlockPlaceEvent e) {
      Player player = e.getPlayer();
      if (!this.jobManager.shouldIgnore(player)) {
         Block block = e.getBlock();
         String value;
         if (block.getBlockData() instanceof Ageable ageable) {
            value = block.getType().name() + ageable.getAge();
         } else {
            value = block.getType().name();
         }

         if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.PLACE)) {
            for (Job job : this.jobManager.getJobsWithAction(BuiltInActions.Material.PLACE)) {
               this.jobManager.processAction(player, BuiltInActions.Material.PLACE, value, job, 1);
            }
         }
      }
   }
}

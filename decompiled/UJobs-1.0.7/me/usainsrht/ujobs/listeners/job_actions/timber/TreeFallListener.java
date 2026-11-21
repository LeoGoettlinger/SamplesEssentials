package me.usainsrht.ujobs.listeners.job_actions.timber;

import com.songoda.ultimatetimber.events.TreeFallEvent;
import com.songoda.ultimatetimber.tree.TreeBlockSet;
import java.util.HashMap;
import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TreeFallListener implements Listener {
   JobManager jobManager;

   public TreeFallListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBreak(TreeFallEvent e) {
      TreeBlockSet<Block> blockSet = e.getDetectedTree().getDetectedTreeBlocks();
      HashMap<Material, Integer> amounts = new HashMap<>();
      blockSet.getLogBlocks().forEach(treeBlock -> {
         Material material = ((Block)treeBlock.getBlock()).getType();
         amounts.put(material, amounts.getOrDefault(material, 0) + 1);
      });
      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.BREAK)) {
         for (Job job : this.jobManager.getJobsWithAction(BuiltInActions.Material.BREAK)) {
            amounts.forEach((log, amount) -> this.jobManager.processAction(e.getPlayer(), BuiltInActions.Material.BREAK, log.name(), job, amount));
         }
      }
   }
}

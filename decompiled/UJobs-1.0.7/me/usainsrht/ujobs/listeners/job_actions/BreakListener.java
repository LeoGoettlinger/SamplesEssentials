package me.usainsrht.ujobs.listeners.job_actions;

import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakListener implements Listener {
   JobManager jobManager;

   public BreakListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent e) {
      Player player = e.getPlayer();
      if (!this.jobManager.shouldIgnore(player)) {
         Block block = e.getBlock();
         ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
         if (item.isEmpty()
            || item.getEnchantmentLevel(Enchantment.SILK_TOUCH) <= 0
            || !this.jobManager.getPlugin().getConfig().getStringList("silktouch_blacklist").contains(block.getType().toString())) {
            String value;
            if (block.getBlockData() instanceof Ageable ageable) {
               value = block.getType().name() + ageable.getAge();
            } else {
               value = block.getType().name();
            }

            if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.BREAK)) {
               for (Job job : this.jobManager.getJobsWithAction(BuiltInActions.Material.BREAK)) {
                  this.jobManager.processAction(player, BuiltInActions.Material.BREAK, value, job, 1);
               }
            }
         }
      }
   }
}

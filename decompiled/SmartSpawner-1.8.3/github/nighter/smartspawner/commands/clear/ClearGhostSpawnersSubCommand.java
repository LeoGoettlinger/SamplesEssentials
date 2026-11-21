package github.nighter.smartspawner.commands.clear;

import com.mojang.brigadier.context.CommandContext;
import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ClearGhostSpawnersSubCommand extends BaseSubCommand {
   public ClearGhostSpawnersSubCommand(SmartSpawner plugin) {
      super(plugin);
   }

   @Override
   public String getName() {
      return "ghost_spawners";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.clear";
   }

   @Override
   public String getDescription() {
      return "Check and remove all ghost spawners asynchronously";
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.plugin.getMessageService().sendMessage(sender, "clear.ghost_check_start");
      List<SpawnerData> allSpawners = this.plugin.getSpawnerManager().getAllSpawners();
      AtomicInteger removedCount = new AtomicInteger(0);

      for (SpawnerData spawner : allSpawners) {
         Location loc = spawner.getSpawnerLocation();
         if (loc != null && loc.getWorld() != null) {
            Scheduler.runLocationTask(loc, () -> {
               if (this.plugin.getSpawnerManager().isGhostSpawner(spawner)) {
                  this.plugin.getSpawnerManager().removeGhostSpawner(spawner.getSpawnerId());
                  removedCount.incrementAndGet();
               }
            });
         }
      }

      Scheduler.runTaskLater(() -> {
         int count = removedCount.get();
         if (count > 0) {
            this.plugin.getMessageService().sendMessage(sender, "clear.ghost_cleared", Map.of("count", String.valueOf(count)));
         } else {
            this.plugin.getMessageService().sendMessage(sender, "clear.ghost_none_found");
         }
      }, 100L);
      return 1;
   }
}

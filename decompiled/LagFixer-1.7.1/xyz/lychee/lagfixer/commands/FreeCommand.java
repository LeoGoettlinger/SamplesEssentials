package xyz.lychee.lagfixer.commands;

import java.lang.management.ManagementFactory;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class FreeCommand extends CommandManager.Subcommand {
   private boolean explicitGCDisabled = false;

   public FreeCommand(CommandManager commandManager) {
      super(commandManager, "free", "run garbage collector");
   }

   @Override
   public void load() {
      this.explicitGCDisabled = ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XX:+DisableExplicitGC");
   }

   @Override
   public void unload() {
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (this.explicitGCDisabled) {
         MessageUtils.sendMessage(true, sender, "&7Unable to free RAM, you need to remove jvm argument: &e&n-XX:+DisableExplicitGC&7!");
         return false;
      } else {
         SupportManager.getInstance().getFork().runNow(true, null, () -> {
            Runtime runtime = Runtime.getRuntime();
            long before = runtime.totalMemory() - runtime.freeMemory();

            try {
               runtime.gc();
               Thread.sleep(300L);
            } catch (InterruptedException var10) {
            }

            long after = runtime.totalMemory() - runtime.freeMemory();
            long diff = before - after;
            if (diff <= 0L) {
               MessageUtils.sendMessage(true, sender, "&7No memory found to clear!");
            } else {
               long freedMB = diff / 1048576L;
               MessageUtils.sendMessage(true, sender, "&7Successfully freed &e" + freedMB + " &7MB of memory.");
            }
         });
         return true;
      }
   }
}

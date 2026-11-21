package com.artillexstudios.axtrade.libs.axapi.updatechecker;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.scheduler.ScheduledTask;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axtrade.libs.axapi.updatechecker.sources.UpdateCheckSource;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class UpdateChecker {
   private final UpdateCheckSource source;
   private final ArtifactVersion current;
   private ScheduledTask task;
   private String permission = "";
   private BooleanSupplier enabled = () -> false;
   private UpdateCheck lastCheck = null;
   private long timeOfLastCheck = 0L;
   private Duration timeBetweenChecks = Duration.ofMinutes(5L);
   private BiConsumer<CommandSender, UpdateCheck> consumer = (senders, check) -> {};

   public UpdateChecker(UpdateCheckSource source) {
      this.source = source;
      this.current = new ArtifactVersion(((AxPlugin)AxPlugin.getPlugin(AxPlugin.class)).getDescription().getVersion());
   }

   public UpdateChecker onCheck(BiConsumer<CommandSender, UpdateCheck> consumer) {
      this.consumer = consumer;
      return this;
   }

   public UpdateChecker timeBetweenChecks(Duration duration) {
      this.timeBetweenChecks = duration;
      return this;
   }

   public UpdateChecker register(String permission, BooleanSupplier enabled) {
      this.permission = permission;
      this.enabled = enabled;
      Bukkit.getPluginManager().registerEvents(new Listener() {
         @EventHandler
         public void onPlayerJoinEvent(PlayerJoinEvent event) {
            if (UpdateChecker.this.enabled.getAsBoolean()) {
               if (event.getPlayer().hasPermission(UpdateChecker.this.permission)) {
                  UpdateChecker.this.check(event.getPlayer());
               }
            }
         }
      }, AxPlugin.getPlugin(AxPlugin.class));
      return this;
   }

   public UpdateChecker check(CommandSender... sender) {
      Scheduler.get().runAsync(() -> {
         UpdateCheck check = this.lastCheck;
         if (check == null || this.timeOfLastCheck + this.timeBetweenChecks.toMillis() <= System.currentTimeMillis()) {
            check = this.source.check(this.current);
            this.lastCheck = check;
            this.timeOfLastCheck = System.currentTimeMillis();
         }

         for (CommandSender commandSender : sender) {
            this.consumer.accept(commandSender, check);
         }
      });
      return this;
   }

   public UpdateChecker checkEvery(double hours) {
      if (this.task != null) {
         this.task.cancel();
      }

      double minutes = 60.0 * hours;
      double seconds = 60.0 * minutes;
      long ticks = (long)(20.0 * seconds);
      this.task = Scheduler.get().runAsyncTimer(() -> this.check(Bukkit.getConsoleSender()), ticks, ticks);
      return this;
   }

   public ArtifactVersion current() {
      return this.current;
   }
}

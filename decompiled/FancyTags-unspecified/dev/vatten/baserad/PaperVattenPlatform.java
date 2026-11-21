package dev.vatten.baserad;

import dev.vatten.baserad.commands.Command;
import dev.vatten.baserad.events.PlayerLeaveEvent;
import dev.vatten.baserad.events.PlayerLoadInEvent;
import dev.vatten.library.bstats.bukkit.Metrics;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

public class PaperVattenPlatform extends JavaPlugin implements VattenPlatform<Player, BukkitTask>, Listener {
   private VattenPlugin plugin;

   public void onEnable() {
      this.plugin = new Plugin(this, VattenPlugin.Type.SERVER, this.getDataFolder().toPath());
      if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new PaperFancyTagsExpansion((Plugin)this.plugin).register();
      }

      if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
         ((Plugin)this.plugin).registerMiniPlaceholders();
      }

      this.getServer().getPluginManager().registerEvents(this, this);
      int pluginId = 27900;
      new Metrics(this, pluginId);
   }

   public BukkitScheduledTask scheduleTask(Runnable runnable, int delay) {
      return new BukkitScheduledTask(this.getServer().getScheduler().runTaskLater(this, runnable, delay / 50));
   }

   public BukkitScheduledTask scheduleRepeatingTask(Runnable runnable, int delay, int period) {
      return new BukkitScheduledTask(this.getServer().getScheduler().runTaskTimer(this, runnable, delay / 50, period / 50));
   }

   @Override
   public void registerCommand(final Command command) {
      this.registerCommand(
         command.getName(),
         command.getAliases(),
         new BasicCommand() {
            {
               Objects.requireNonNull(PaperVattenPlatform.this);
            }

            public void execute(CommandSourceStack commandSourceStack, String[] args) {
               if (commandSourceStack.getSender() instanceof Player player) {
                  command.execute(PaperVattenPlatform.this.wrapPlayer(player), args);
               } else {
                  commandSourceStack.getSender().sendMessage("This command can only be used by players.");
               }
            }

            public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
               return commandSourceStack.getSender() instanceof Player player
                  ? command.onTabComplete(PaperVattenPlatform.this.wrapPlayer(player), args)
                  : List.of();
            }

            @Nullable
            public String permission() {
               return command.permission();
            }
         }
      );
   }

   public VattenPlayer wrapPlayer(Player player) {
      VattenPlayer existingPlayer = this.plugin.getPlayer(player.getUniqueId());
      return existingPlayer != null ? existingPlayer : new VattenPlayer(player.getUniqueId(), player.getName(), player);
   }

   @org.bukkit.event.EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.plugin.getEventHandler().dispatchEvent(new dev.vatten.baserad.events.PlayerJoinEvent(this.wrapPlayer(event.getPlayer())));
   }

   @org.bukkit.event.EventHandler
   public void onPlayerLeave(PlayerQuitEvent event) {
      this.plugin.getEventHandler().dispatchEvent(new PlayerLeaveEvent(this.wrapPlayer(event.getPlayer())));
   }

   @org.bukkit.event.EventHandler
   public void onPlayerLoadedIn(PlayerClientLoadedWorldEvent event) {
      this.plugin.getEventHandler().dispatchEvent(new PlayerLoadInEvent(this.wrapPlayer(event.getPlayer())));
   }
}

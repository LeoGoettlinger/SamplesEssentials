package org.popcraft.chunky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.command.ChunkyCommand;
import org.popcraft.chunky.command.CommandArguments;
import org.popcraft.chunky.integration.WorldBorderIntegration;
import org.popcraft.chunky.lib.bstats.bukkit.Metrics;
import org.popcraft.chunky.lib.bstats.charts.SimplePie;
import org.popcraft.chunky.platform.BukkitConfig;
import org.popcraft.chunky.platform.BukkitPlayer;
import org.popcraft.chunky.platform.BukkitSender;
import org.popcraft.chunky.platform.BukkitServer;
import org.popcraft.chunky.platform.Folia;
import org.popcraft.chunky.platform.Paper;
import org.popcraft.chunky.platform.Sender;
import org.popcraft.chunky.util.Input;
import org.popcraft.chunky.util.Translator;
import org.popcraft.chunky.util.Version;

public final class ChunkyBukkit extends JavaPlugin implements Listener {
   private static final String COMMAND_PERMISSION_KEY = "chunky.command.";
   private Chunky chunky;

   public void onEnable() {
      this.chunky = new Chunky(new BukkitServer(this), new BukkitConfig(this));
      Version currentVersion = new Version(Bukkit.getBukkitVersion(), true);
      if (currentVersion.isValid() && Version.MINECRAFT_1_13_2.isHigherThan(currentVersion)) {
         this.getLogger().severe(() -> Translator.translate("error_version"));
         this.getServer().getPluginManager().disablePlugin(this);
      }

      if (this.isEnabled()) {
         this.getServer().getServicesManager().register(Chunky.class, this.chunky, this, ServicePriority.Normal);
         this.getServer().getServicesManager().register(ChunkyAPI.class, this.chunky.getApi(), this, ServicePriority.Normal);
         if (this.chunky.getConfig().getContinueOnRestart()) {
            Runnable continueTask = () -> this.chunky.getCommands().get("continue").execute(this.chunky.getServer().getConsole(), CommandArguments.empty());
            if (Folia.isFolia()) {
               Folia.onServerInit(this, continueTask);
            } else {
               this.getServer().getScheduler().scheduleSyncDelayedTask(this, continueTask);
            }
         }

         if (this.getServer().getPluginManager().getPlugin("WorldBorder") != null) {
            this.chunky.getServer().getIntegrations().put("border", new WorldBorderIntegration());
         }

         Metrics metrics = new Metrics(this, 8211);
         metrics.addCustomChart(new SimplePie("language", () -> this.chunky.getConfig().getLanguage()));
         this.getServer().getPluginManager().registerEvents(this, this);
         if (!Paper.isPaper()) {
            this.disablePauseWhenEmptySeconds();
         }
      }
   }

   public void onDisable() {
      HandlerList.unregisterAll(this);
      if (this.chunky != null) {
         this.chunky.disable();
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      Sender bukkitSender = (Sender)(sender instanceof Player player ? new BukkitPlayer(player) : new BukkitSender(sender));
      Map<String, ChunkyCommand> commands = this.chunky.getCommands();
      CommandArguments arguments = CommandArguments.of(Arrays.copyOfRange(args, Math.min(1, args.length), args.length));
      if (args.length <= 0 || !commands.containsKey(args[0].toLowerCase())) {
         commands.get("help").execute(bukkitSender, arguments);
      } else if (sender.hasPermission("chunky.command." + args[0].toLowerCase())) {
         commands.get(args[0].toLowerCase()).execute(bukkitSender, arguments);
      } else {
         bukkitSender.sendMessage("command_no_permission");
      }

      return true;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length < 1) {
         return List.of();
      } else {
         List<String> suggestions = new ArrayList<>();
         Map<String, ChunkyCommand> commands = this.chunky.getCommands();
         if (args.length == 1) {
            commands.keySet().stream().filter(name -> sender.hasPermission("chunky.command." + name)).forEach(suggestions::add);
         } else if (commands.containsKey(args[0].toLowerCase()) && sender.hasPermission("chunky.command." + args[0].toLowerCase())) {
            CommandArguments arguments = CommandArguments.of(Arrays.copyOfRange(args, 1, args.length));
            suggestions.addAll(commands.get(args[0].toLowerCase()).suggestions(arguments));
         }

         return suggestions.stream().filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase())).toList();
      }
   }

   public Chunky getChunky() {
      return this.chunky;
   }

   @EventHandler
   public void onWorldInit(WorldInitEvent event) {
      this.chunky.getRegionCache().clear(event.getWorld().getName());
   }

   private void disablePauseWhenEmptySeconds() {
      Path serverPropertiesPath = Path.of(".").resolve("server.properties");
      File serverPropertiesFile = serverPropertiesPath.toFile();
      Properties serverProperties = new Properties();

      try (FileInputStream serverPropertiesFileInputStream = new FileInputStream(serverPropertiesFile)) {
         serverProperties.load(serverPropertiesFileInputStream);
         Optional<Integer> pauseWhenEmptySeconds = Input.tryInteger(serverProperties.getProperty("pause-when-empty-seconds"));
         if (pauseWhenEmptySeconds.isPresent() && pauseWhenEmptySeconds.get() > 0) {
            serverProperties.setProperty("pause-when-empty-seconds", "0");

            try (FileOutputStream serverPropertiesFileOutputStream = new FileOutputStream(serverPropertiesFile)) {
               serverProperties.store(serverPropertiesFileOutputStream, "Minecraft server properties");
               this.getLogger().warning(() -> Translator.translate("error_pause_when_empty_seconds"));
            } catch (IOException var12) {
               var12.printStackTrace();
            }
         }
      } catch (IOException var14) {
         var14.printStackTrace();
      }
   }
}

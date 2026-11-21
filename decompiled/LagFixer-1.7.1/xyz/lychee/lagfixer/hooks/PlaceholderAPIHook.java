package xyz.lychee.lagfixer.hooks;

import java.lang.management.ManagementFactory;
import lombok.Generated;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.objects.WorldsMonitor;

public class PlaceholderAPIHook extends AbstractHook {
   private PlaceholderAPIHook.PapiImplementation papi;

   public PlaceholderAPIHook(LagFixer plugin, HookManager manager) {
      super(plugin, "PlaceholderAPI", manager);
   }

   public String applyPlaceholders(Player p, String text) {
      return PlaceholderAPI.setPlaceholders(p, text);
   }

   @Override
   public void load() {
      this.papi = new PlaceholderAPIHook.PapiImplementation(this.getPlugin());
      this.papi.register();
   }

   @Override
   public void disable() {
      this.papi.unregister();
   }

   @Generated
   public PlaceholderAPIHook.PapiImplementation getPapi() {
      return this.papi;
   }

   public static class PapiImplementation extends PlaceholderExpansion {
      private final LagFixer plugin;

      public PapiImplementation(LagFixer plugin) {
         this.plugin = plugin;
      }

      @NotNull
      public String getIdentifier() {
         return "lagfixer";
      }

      @NotNull
      public String getAuthor() {
         return "lajczik, Syncara Host";
      }

      @NotNull
      public String getVersion() {
         return this.plugin.getDescription().getVersion();
      }

      public String onPlaceholderRequest(Player p, @NotNull String id) {
         return this.response(p, id);
      }

      public String onRequest(OfflinePlayer p, @NotNull String id) {
         return this.response(p, id);
      }

      public boolean persist() {
         return true;
      }

      public boolean canRegister() {
         return true;
      }

      public String response(OfflinePlayer player, String id) {
         SupportManager support = SupportManager.getInstance();
         ResourceMonitor resourceMonitor = support.getResourceMonitor();
         WorldsMonitor worldsMonitor = support.getWorldsMonitor();
         ModuleManager moduleManager = ModuleManager.getInstance();
         WorldCleanerModule worldCleaner = moduleManager.get(WorldCleanerModule.class);
         AFKOptimizerModule afkOptimizer = moduleManager.get(AFKOptimizerModule.class);
         Runtime runtime = Runtime.getRuntime();
         String var10 = id.toLowerCase();
         switch (var10) {
            case "tps":
               return Double.toString(resourceMonitor.getTps());
            case "tps_color": {
               double tps = resourceMonitor.getTps();
               String color = tps >= 18.0 ? "&a" : (tps >= 15.0 ? "&e" : "&c");
               return color + String.format("%.1f", tps);
            }
            case "mspt":
               return Double.toString(resourceMonitor.getMspt());
            case "mspt_color": {
               double mspt = resourceMonitor.getMspt();
               String color = mspt <= 40.0 ? "&a" : (mspt <= 50.0 ? "&e" : "&c");
               return color + String.format("%.1f", mspt);
            }
            case "cpu":
            case "cpuprocess":
               return Double.toString(resourceMonitor.getCpuProcess());
            case "cpusystem":
               return Double.toString(resourceMonitor.getCpuSystem());
            case "entities_total":
            case "entities":
               return Integer.toString(worldsMonitor.getEntities());
            case "entities_mobs":
            case "mobs":
            case "creatures":
               return Integer.toString(worldsMonitor.getCreatures());
            case "entities_items":
            case "items":
               return Integer.toString(worldsMonitor.getItems());
            case "entities_projectiles":
            case "projectiles":
               return Integer.toString(worldsMonitor.getProjectiles());
            case "entities_vehicles":
            case "vehicles":
               return Integer.toString(worldsMonitor.getVehicles());
            case "afk_time": {
               AFKOptimizerModule.AfkPlayer afkPlayer = afkOptimizer.getAfk_players().get(player.getUniqueId());
               return afkPlayer == null ? "0" : afkPlayer.getAfkTime().toString();
            }
            case "is_afk_position":
            case "is_afk_location": {
               AFKOptimizerModule.AfkPlayer afkPlayer = afkOptimizer.getAfk_players().get(player.getUniqueId());
               return Boolean.toString(afkPlayer != null && afkPlayer.isPositionAfk());
            }
            case "is_afk_rotation": {
               AFKOptimizerModule.AfkPlayer afkPlayer = afkOptimizer.getAfk_players().get(player.getUniqueId());
               return Boolean.toString(afkPlayer != null && afkPlayer.isRotationAfk());
            }
            case "memory_used":
            case "ram_used":
               return (runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L + "MB";
            case "memory_max":
            case "ram_max":
               return runtime.maxMemory() / 1024L / 1024L + "MB";
            case "memory_free":
            case "ram_free":
               return runtime.freeMemory() / 1024L / 1024L + "MB";
            case "memory_percent":
            case "ram_percent": {
               long used = runtime.totalMemory() - runtime.freeMemory();
               long max = runtime.maxMemory();
               int percent = (int)(used * 100L / max);
               return percent + "%";
            }
            case "memory_bar": {
               long used = runtime.totalMemory() - runtime.freeMemory();
               long max = runtime.maxMemory();
               int percent = (int)(used * 100L / max);
               int filled = percent / 10;
               StringBuilder bar = new StringBuilder("&8[");

               for (int i = 0; i < 10; i++) {
                  bar.append(i < filled ? "&a■" : "&7■");
               }

               bar.append("&8]");
               return bar.toString();
            }
            case "players":
            case "online":
               return Integer.toString(Bukkit.getOnlinePlayers().size());
            case "players_max":
            case "max_players":
               return Integer.toString(Bukkit.getMaxPlayers());
            case "worlds":
               return Integer.toString(Bukkit.getWorlds().size());
            case "chunks":
            case "loaded_chunks":
               int chunks = 0;

               for (World world : Bukkit.getWorlds()) {
                  chunks += world.getLoadedChunks().length;
               }

               return Integer.toString(chunks);
            case "uptime":
               long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
               long hours = uptime / 3600L;
               long minutes = uptime % 3600L / 60L;
               long seconds = uptime % 60L;
               return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            case "uptime_hours":
               return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000L / 3600L);
            case "uptime_minutes":
               return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000L / 60L);
            case "uptime_seconds":
               return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000L);
            case "worldcleaner_timer":
            case "worldcleaner":
               return worldCleaner != null && worldCleaner.isLoaded() ? worldCleaner.getSecond() + "s" : "0s";
            case "worldcleaner_seconds":
               return worldCleaner != null && worldCleaner.isLoaded() ? Integer.toString(worldCleaner.getSecond()) : "0";
            case "worldcleaner_formatted":
               if (worldCleaner != null && worldCleaner.isLoaded()) {
                  int secondsx = worldCleaner.getSecond();
                  int mins = secondsx / 60;
                  int secs = secondsx % 60;
                  return String.format("%02d:%02d", mins, secs);
               }

               return "00:00";
            case "worldcleaner_interval":
               return worldCleaner != null && worldCleaner.isLoaded() ? Integer.toString(worldCleaner.getInterval()) : "0";
            case "worldcleaner_enabled":
               return Boolean.toString(worldCleaner != null && worldCleaner.isLoaded());
            case "worldcleaner_progress":
               if (worldCleaner != null && worldCleaner.isLoaded()) {
                  int interval = worldCleaner.getInterval();
                  int remaining = worldCleaner.getSecond();
                  int elapsed = interval - remaining;
                  int percentx = interval > 0 ? elapsed * 100 / interval : 0;
                  return percentx + "%";
               }

               return "0%";
            case "worldcleaner_bar":
               if (worldCleaner != null && worldCleaner.isLoaded()) {
                  int interval = worldCleaner.getInterval();
                  int remaining = worldCleaner.getSecond();
                  int elapsed = interval - remaining;
                  int percentx = interval > 0 ? elapsed * 100 / interval : 0;
                  int filled = percentx / 10;
                  StringBuilder bar = new StringBuilder("&8[");

                  for (int i = 0; i < 10; i++) {
                     bar.append(i < filled ? "&c■" : "&a■");
                  }

                  bar.append("&8]");
                  return bar.toString();
               }

               return "&8[&7■■■■■■■■■■&8]";
            default:
               return null;
         }
      }
   }
}

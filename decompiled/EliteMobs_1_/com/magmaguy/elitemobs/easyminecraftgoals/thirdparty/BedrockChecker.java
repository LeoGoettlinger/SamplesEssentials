package com.magmaguy.elitemobs.easyminecraftgoals.thirdparty;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class BedrockChecker {
   private static final Pattern BEDROCK_NAME_PATTERN = Pattern.compile("^\\..*\\d{4}$");
   private static volatile BedrockChecker.Providers tracker;

   private BedrockChecker() {
   }

   public static boolean isBedrock(Player player) {
      if (player == null) {
         return false;
      } else if (player.getUniqueId().getMostSignificantBits() == 0L) {
         return true;
      } else {
         String name = player.getName();
         if (name != null && BEDROCK_NAME_PATTERN.matcher(name).matches()) {
            return true;
         } else {
            BedrockChecker.Available available = available();
            return available.floodgate() != null && available.floodgate().isEnabled() && Floodgate.isBedrock(player)
               ? true
               : available.geyser() != null && available.geyser().isEnabled() && Geyser.isBedrock(player);
         }
      }
   }

   public static boolean isBedrockSupportPresent() {
      BedrockChecker.Available available = available();
      return available.floodgate() != null && available.floodgate().isEnabled() || available.geyser() != null && available.geyser().isEnabled();
   }

   private static BedrockChecker.Available available() {
      PluginManager manager = Bukkit.getPluginManager();
      BedrockChecker.Providers current = tracker;
      if (current != null && current.manager == manager && current.owner.isEnabled()) {
         return current.available;
      } else {
         MagmaCore core = MagmaCore.getInstance();
         Plugin owner = core == null ? null : core.getRequestingPlugin();
         if (owner != null && owner.isEnabled() && Bukkit.isPrimaryThread()) {
            synchronized (BedrockChecker.class) {
               current = tracker;
               if (current != null && current.manager == manager && current.owner.isEnabled()) {
                  return current.available;
               } else {
                  if (current != null) {
                     HandlerList.unregisterAll(current);
                  }

                  current = new BedrockChecker.Providers(manager, owner);
                  manager.registerEvents(current, owner);
                  tracker = current;
                  return current.available;
               }
            }
         } else {
            return discover(manager, null);
         }
      }
   }

   private static BedrockChecker.Available discover(PluginManager manager, Plugin excluded) {
      Plugin floodgate = null;
      Plugin geyser = null;

      for (Plugin plugin : manager.getPlugins()) {
         if (plugin != excluded && plugin.isEnabled()) {
            if (plugin.getName().equalsIgnoreCase("floodgate")) {
               floodgate = plugin;
            } else if (isGeyser(plugin.getName())) {
               geyser = plugin;
            }
         }
      }

      return new BedrockChecker.Available(floodgate, geyser);
   }

   private static boolean isGeyser(String name) {
      return name.equalsIgnoreCase("Geyser-Spigot") || name.equalsIgnoreCase("Geyser-Bukkit") || name.equalsIgnoreCase("Geyser");
   }

   private record Available(Plugin floodgate, Plugin geyser) {
   }

   private static final class Providers implements Listener {
      private final PluginManager manager;
      private final Plugin owner;
      private volatile BedrockChecker.Available available;

      private Providers(PluginManager manager, Plugin owner) {
         this.manager = manager;
         this.owner = owner;
         this.available = BedrockChecker.discover(manager, null);
      }

      @EventHandler
      public void onEnable(PluginEnableEvent event) {
         if (this.relevant(event.getPlugin())) {
            this.available = BedrockChecker.discover(this.manager, null);
         }
      }

      @EventHandler
      public void onDisable(PluginDisableEvent event) {
         if (event.getPlugin() == this.owner) {
            if (BedrockChecker.tracker == this) {
               BedrockChecker.tracker = null;
            }

            HandlerList.unregisterAll(this);
         } else if (this.relevant(event.getPlugin())) {
            this.available = BedrockChecker.discover(this.manager, event.getPlugin());
         }
      }

      private boolean relevant(Plugin plugin) {
         return plugin.getName().equalsIgnoreCase("floodgate") || BedrockChecker.isGeyser(plugin.getName());
      }
   }
}

package me.wikmor.lpc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class LPC extends JavaPlugin implements Listener {
   private int pluginConfiguration;
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Pattern BUKKIT_HEX_PATTERN = Pattern.compile("&x(&[A-Fa-f0-9]){6}");
   private LuckPerms luckPerms;

   public void onEnable() {
      this.pluginConfiguration = this.initializePluginConfiguration();
      this.luckPerms = (LuckPerms)this.getServer().getServicesManager().load(LuckPerms.class);
      if (this.luckPerms == null) {
         this.getLogger().severe("LuckPerms not found! LPC requires LuckPerms to function.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.saveDefaultConfig();
         boolean paperChat = false;

         try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            paperChat = true;
         } catch (ClassNotFoundException var7) {
         }

         if (paperChat) {
            this.getServer().getPluginManager().registerEvents(new PaperChatListener(this), this);
         } else {
            this.getServer().getPluginManager().registerEvents(this, this);
         }

         String[] chatPlugins = new String[]{"EssentialsChat", "VentureChat", "HeroChat", "DeluxeChat", "ChatManager", "ChatEx", "UltraChat", "TownyChat"};

         for (String pluginName : chatPlugins) {
            if (this.getServer().getPluginManager().isPluginEnabled(pluginName)) {
               this.getLogger()
                  .warning(
                     "Detected " + pluginName + " which may also format chat. To avoid message duplication, disable chat formatting in " + pluginName + "."
                  );
            }
         }
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 1 && "reload".equals(args[0]) && sender.hasPermission("lpc.reload")) {
         this.reloadConfig();
         sender.sendMessage(this.colorize("&aLPC has been reloaded."));
         return true;
      } else if (args.length == 1 && "clear".equals(args[0]) && sender.hasPermission("lpc.clearchat")) {
         for (Player player : this.getServer().getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
               player.sendMessage("");
            }
         }

         String clearMessage = this.getConfig().getString("clear-chat-message", "&7Chat has been cleared by a staff member.");
         this.getServer().broadcastMessage(this.colorize(clearMessage));
         return true;
      } else if (args.length == 2 && "debug".equals(args[0]) && sender.hasPermission("lpc.debug")) {
         Player target = this.getServer().getPlayer(args[1]);
         if (target == null) {
            sender.sendMessage(this.colorize("&cPlayer not found."));
            return true;
         } else {
            CachedMetaData debugMeta = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(target);
            sender.sendMessage(this.colorize("&6&lLPC Debug: &f" + target.getName()));
            sender.sendMessage(this.colorize("&7Primary Group: &f" + debugMeta.getPrimaryGroup()));
            sender.sendMessage(this.colorize("&7Prefix: &f" + (debugMeta.getPrefix() != null ? debugMeta.getPrefix() : "&cnone")));
            sender.sendMessage(this.colorize("&7Suffix: &f" + (debugMeta.getSuffix() != null ? debugMeta.getSuffix() : "&cnone")));
            sender.sendMessage(this.colorize("&7All Prefixes (by weight):"));
            debugMeta.getPrefixes().forEach((weight, prefix) -> sender.sendMessage(this.colorize("  &7[" + weight + "] &f" + prefix)));
            sender.sendMessage(this.colorize("&7All Suffixes (by weight):"));
            debugMeta.getSuffixes().forEach((weight, suffix) -> sender.sendMessage(this.colorize("  &7[" + weight + "] &f" + suffix)));
            sender.sendMessage(
               this.colorize("&7Username-color: &f" + (debugMeta.getMetaValue("username-color") != null ? debugMeta.getMetaValue("username-color") : "&cnone"))
            );
            sender.sendMessage(
               this.colorize("&7Message-color: &f" + (debugMeta.getMetaValue("message-color") != null ? debugMeta.getMetaValue("message-color") : "&cnone"))
            );
            sender.sendMessage(
               this.colorize(
                  "&7Group format: &f"
                     + (
                        this.getConfig().getString("group-formats." + debugMeta.getPrimaryGroup()) != null
                           ? "group-formats." + debugMeta.getPrimaryGroup()
                           : "chat-format (default)"
                     )
               )
            );
            sender.sendMessage(
               this.colorize("&7PAPI: &f" + (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&ahooked" : "&cnot found"))
            );
            sender.sendMessage(this.colorize("&7Has lpc.colorcodes: &f" + target.hasPermission("lpc.colorcodes")));
            sender.sendMessage(this.colorize("&7Has lpc.rgbcodes: &f" + target.hasPermission("lpc.rgbcodes")));
            return true;
         }
      } else {
         return false;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         List<String> completions = new ArrayList<>();
         if (sender.hasPermission("lpc.reload")) {
            completions.add("reload");
         }

         if (sender.hasPermission("lpc.clearchat")) {
            completions.add("clear");
         }

         if (sender.hasPermission("lpc.debug")) {
            completions.add("debug");
         }

         return completions;
      } else {
         return (List<String>)(args.length == 2 && "debug".equals(args[0]) && sender.hasPermission("lpc.debug")
            ? this.getServer()
               .getOnlinePlayers()
               .stream()
               .<String>map(Player::getName)
               .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
               .collect(Collectors.toList())
            : new ArrayList<>());
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onChat(AsyncPlayerChatEvent event) {
      String message = event.getMessage();
      Player player = event.getPlayer();
      String format = this.buildFormat(player);
      String processedMessage = this.processMessage(player, message);
      event.setFormat(format.replace("{message}", processedMessage).replace("%", "%%"));
   }

   String buildFormat(Player player) {
      CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
      String group = metaData.getPrimaryGroup();
      String format = this.getConfig().getString(this.getConfig().getString("group-formats." + group) != null ? "group-formats." + group : "chat-format");
      if (format == null) {
         format = "{prefix}{name}&r: {message}";
      }

      format = format.replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
         .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
         .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> (String)metaData.getPrefixes().get(key)).collect(Collectors.joining()))
         .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> (String)metaData.getSuffixes().get(key)).collect(Collectors.joining()))
         .replace("{world}", player.getWorld().getName())
         .replace("{name}", player.getName())
         .replace("{displayname}", player.getDisplayName())
         .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
         .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");
      format = this.translateHexColorCodes(format);
      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         format = PlaceholderAPI.setPlaceholders(player, format);
      }

      return this.colorize(this.translateHexColorCodes(format));
   }

   String processMessage(Player player, String message) {
      if (player.hasPermission("lpc.colorcodes") && player.hasPermission("lpc.rgbcodes")) {
         return this.colorize(this.translateHexColorCodes(message));
      } else if (player.hasPermission("lpc.colorcodes")) {
         return this.colorize(this.stripHexCodes(message));
      } else {
         return player.hasPermission("lpc.rgbcodes")
            ? this.stripColorCodes(this.translateHexColorCodes(message))
            : this.stripColorCodes(this.stripHexCodes(message));
      }
   }

   String colorize(String message) {
      return ChatColor.translateAlternateColorCodes('&', message);
   }

   String translateHexColorCodes(String message) {
      char colorChar = 167;
      Matcher matcher = HEX_PATTERN.matcher(message);
      StringBuffer buffer = new StringBuffer(message.length() + 32);

      while (matcher.find()) {
         String group = matcher.group(1);
         matcher.appendReplacement(
            buffer,
            "§x§" + group.charAt(0) + '§' + group.charAt(1) + '§' + group.charAt(2) + '§' + group.charAt(3) + '§' + group.charAt(4) + '§' + group.charAt(5)
         );
      }

      String result = matcher.appendTail(buffer).toString();
      matcher = BUKKIT_HEX_PATTERN.matcher(result);
      buffer = new StringBuffer(result.length());

      while (matcher.find()) {
         matcher.appendReplacement(buffer, matcher.group().replace('&', '§'));
      }

      return matcher.appendTail(buffer).toString();
   }

   String stripColorCodes(String message) {
      return message.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
   }

   String stripHexCodes(String message) {
      String result = message.replaceAll("&#[0-9a-fA-F]{6}", "");
      return result.replaceAll("&x(&[0-9a-fA-F]){6}", "");
   }
}

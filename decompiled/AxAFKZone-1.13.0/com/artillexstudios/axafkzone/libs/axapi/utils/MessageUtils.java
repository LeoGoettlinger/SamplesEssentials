package com.artillexstudios.axafkzone.libs.axapi.utils;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.YamlDocument;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
   private final YamlDocument config;
   private final YamlDocument prefixConfig;
   private final String prefixRoute;

   public MessageUtils(YamlDocument config, String prefixRoute) {
      this.config = config;
      this.prefixConfig = config;
      this.prefixRoute = prefixRoute;
   }

   public MessageUtils(YamlDocument config, String prefixRoute, YamlDocument prefixConfig) {
      this.config = config;
      this.prefixConfig = prefixConfig;
      this.prefixRoute = prefixRoute;
   }

   public static void sendMessage(CommandSender sender, String message, TagResolver... resolvers) {
      sendMessage(sender, "", message, resolvers);
   }

   public static void sendMessage(CommandSender sender, String prefix, String message, TagResolver... resolvers) {
      if (message != null && !message.isEmpty()) {
         if (sender instanceof Player player) {
            ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
            wrapper.message(StringUtils.format(prefix + message, resolvers));
         } else {
            sender.sendMessage(StringUtils.formatToString(prefix + message, resolvers));
         }
      }
   }

   public void sendFormatted(CommandSender sender, String message, TagResolver... resolvers) {
      if (message != null && !message.isEmpty()) {
         if (sender instanceof Player player) {
            ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
            wrapper.message(StringUtils.format(message, resolvers));
         } else {
            sender.sendMessage(StringUtils.formatToString(message, resolvers));
         }
      }
   }

   public void sendLang(CommandSender commandSender, String message, TagResolver... resolvers) {
      String configString = this.config.getString(message);
      if (configString != null && !configString.isEmpty()) {
         this.sendFormatted(commandSender, this.prefixConfig.getString(this.prefixRoute) + configString, resolvers);
      }
   }

   public void sendFormatted(CommandSender sender, String message, Map<String, String> replacements) {
      AtomicReference<String> toFormat = new AtomicReference<>(message);
      replacements.forEach((pattern, replacement) -> toFormat.set(toFormat.get().replace(pattern, replacement)));
      this.sendFormatted(sender, toFormat.get());
   }

   public void sendLang(CommandSender sender, String message, Map<String, String> replacements) {
      String parsed = this.config.getString(message);
      if (parsed != null && !parsed.isEmpty()) {
         this.sendFormatted(sender, this.prefixConfig.getString(this.prefixRoute) + parsed, replacements);
      }
   }

   public static void send(CommandSender commandSender, Component component) {
      if (commandSender instanceof Player player) {
         ServerPlayerWrapper.wrap(player).message(component);
      } else if (commandSender instanceof ConsoleCommandSender) {
         commandSender.sendMessage(StringUtils.LEGACY_COMPONENT_SERIALIZER.serialize(component));
      }
   }
}

package me.usainsrht.ujobs.utils;

import java.util.List;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.yaml.YamlMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessageUtil {
   public static void send(Audience audience, YamlMessage message) {
      send(audience, message, UJobsPlugin.instance.getMiniMessage());
   }

   public static void send(Audience audience, YamlMessage message, TagResolver... placeholders) {
      send(audience, message, UJobsPlugin.instance.getMiniMessage(), placeholders);
   }

   public static void send(Audience audience, YamlMessage message, MiniMessage miniMessage, TagResolver... placeholders) {
      if (message.getChatMessages() != null && !message.getChatMessages().isEmpty()) {
         List<Component> components = message.getChatMessages().stream().map(line -> miniMessage.deserialize(line, placeholders)).toList();
         components.forEach(audience::sendMessage);
      }

      if (message.getActionBarMessage() != null) {
         audience.sendActionBar(miniMessage.deserialize(message.getActionBarMessage(), placeholders));
      }

      if (message.getTitle() != null) {
         audience.showTitle(message.getTitle().parse(miniMessage, placeholders));
      }

      if (message.getSounds() != null && !message.getSounds().isEmpty()) {
         message.getSounds().forEach(audience::playSound);
      }
   }
}

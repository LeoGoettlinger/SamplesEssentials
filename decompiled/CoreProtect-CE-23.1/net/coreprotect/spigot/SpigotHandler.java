package net.coreprotect.spigot;

import java.util.regex.Matcher;
import net.coreprotect.config.Config;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class SpigotHandler extends SpigotAdapter implements SpigotInterface {
   public static ChatColor DARK_AQUA = ChatColor.of("#31b0e8");

   public SpigotHandler() {
      Color.DARK_AQUA = DARK_AQUA.toString();
   }

   @Override
   public void addHoverComponent(Object message, String[] data) {
      try {
         if (Config.getGlobal().HOVER_EVENTS) {
            String tooltipText = data[1];
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(data[2]));
            if (tooltipText.contains("§k")) {
               tooltipText = tooltipText.replace("§k", "");
            }

            BaseComponent[] displayComponent = TextComponent.fromLegacyText(this.processComponent(tooltipText));
            component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(displayComponent)}));
            ((TextComponent)message).addExtra(component);
         } else {
            super.addHoverComponent(message, data);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   @Override
   public void setHoverEvent(Object component, String text) {
      if (Config.getGlobal().HOVER_EVENTS) {
         ((TextComponent)component).setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(TextComponent.fromLegacyText(text))}));
      }
   }

   @Override
   public void sendComponent(CommandSender sender, String string, String bypass) {
      TextComponent message = new TextComponent();
      StringBuilder builder = new StringBuilder();
      if (sender instanceof ConsoleCommandSender) {
         string = string.replace(DARK_AQUA.toString(), ChatColor.DARK_AQUA.toString());
      }

      Matcher matcher = Util.tagParser.matcher(string);

      while (matcher.find()) {
         String value = matcher.group(1);
         if (value != null) {
            if (builder.length() > 0) {
               addBuilder(message, builder);
            }

            String[] data = value.split("\\|", 3);
            if (data[0].equals("COMMAND")) {
               TextComponent component = new TextComponent(TextComponent.fromLegacyText(data[2]));
               component.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, data[1]));
               SpigotAdapter.ADAPTER.setHoverEvent(component, StringUtils.hoverCommandFilter(data[1]));
               message.addExtra(component);
            } else if (data[0].equals("POPUP")) {
               SpigotAdapter.ADAPTER.addHoverComponent(message, data);
            }
         } else {
            builder.append(matcher.group(2));
         }
      }

      if (builder.length() > 0) {
         addBuilder(message, builder);
      }

      if (bypass != null) {
         message.addExtra(bypass);
      }

      sender.spigot().sendMessage(message);
   }

   private static void addBuilder(TextComponent message, StringBuilder builder) {
      String[] splitBuilder = builder.toString().split(DARK_AQUA.toString());

      for (int i = 0; i < splitBuilder.length; i++) {
         if (i > 0) {
            TextComponent textComponent = new TextComponent(splitBuilder[i]);
            textComponent.setColor(DARK_AQUA);
            message.addExtra(textComponent);
         } else {
            message.addExtra(splitBuilder[i]);
         }
      }

      builder.setLength(0);
   }
}

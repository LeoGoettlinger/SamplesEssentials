package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.util.minimessage.MiniMessageParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class SpigotMessage {
   private SpigotMessage() {
   }

   public static BaseComponent[] fromMiniMessage(String message) {
      return MiniMessageParser.parse(message);
   }

   public static TextComponent simpleMessage(String message) {
      TextComponent wrapper = new TextComponent();

      for (BaseComponent component : MiniMessageParser.parse(message)) {
         wrapper.addExtra(component);
      }

      return wrapper;
   }

   public static TextComponent hoverMessage(String message, String hoverMessage) {
      TextComponent textComponent = simpleMessage(message);
      textComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Content[]{new Text(TextComponent.fromLegacyText(hoverMessage))}));
      return textComponent;
   }

   public static TextComponent commandHoverMessage(String message, String hoverMessage, String commandString) {
      TextComponent textComponent = hoverMessage(message, ChatColorConverter.convert(hoverMessage));
      if (!commandString.isEmpty()) {
         textComponent.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, commandString));
      }

      return textComponent;
   }

   public static TextComponent suggestHoverMessage(String message, String hoverMessage, String commandString) {
      TextComponent textComponent = hoverMessage(message, ChatColorConverter.convert(hoverMessage));
      if (!commandString.isEmpty()) {
         textComponent.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, commandString));
      }

      return textComponent;
   }

   public static TextComponent hoverLinkMessage(String message, String hoverMessage, String link) {
      TextComponent textComponent = hoverMessage(message, hoverMessage);
      textComponent.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, link));
      return textComponent;
   }
}

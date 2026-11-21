package me.chancesd.pvpmanager.sdutils.display.chat;

import java.util.ArrayList;
import java.util.List;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatLine {
   private final List<ChatLine.Component> components;

   public ChatLine(@NotNull String text, @Nullable ChatLine.ClickAction clickAction, @Nullable ChatLine.HoverAction hoverAction) {
      this.components = new ArrayList<>();
      this.components.add(new ChatLine.Component(text, clickAction, hoverAction));
   }

   public ChatLine(@NotNull String text) {
      this(text, null, null);
   }

   private ChatLine(@NotNull List<ChatLine.Component> components) {
      this.components = new ArrayList<>(components);
   }

   private ComponentBuilder toComponentBuilder() {
      ComponentBuilder mainComponent = new ComponentBuilder();

      for (ChatLine.Component comp : this.components) {
         TextComponent textComp = new TextComponent(ChatUtils.colorizeComponent(comp.text));
         if (comp.clickAction != null) {
            textComp.setClickEvent(new ClickEvent(comp.clickAction.type, comp.clickAction.value));
         }

         if (comp.hoverAction != null) {
            textComp.setHoverEvent(new HoverEvent(comp.hoverAction.type, new Content[]{new Text(ChatUtils.colorizeComponent(comp.hoverAction.value))}));
         }

         mainComponent.append(textComp);
      }

      return mainComponent;
   }

   public BaseComponent toComponent() {
      return this.toComponentBuilder().build();
   }

   public BaseComponent[] toComponentArray() {
      return this.toComponentBuilder().create();
   }

   public static ChatLine.Builder builder() {
      return new ChatLine.Builder();
   }

   public String getPlainText() {
      StringBuilder result = new StringBuilder();

      for (ChatLine.Component comp : this.components) {
         result.append(comp.text);
      }

      return result.toString();
   }

   public static class Builder {
      private final List<ChatLine.Component> components = new ArrayList<>();

      public ChatLine.Builder add(@NotNull String text, @Nullable ChatLine.ClickAction clickAction, @Nullable ChatLine.HoverAction hoverAction) {
         this.components.add(new ChatLine.Component(text, clickAction, hoverAction));
         return this;
      }

      public ChatLine.Builder add(@NotNull String text) {
         return this.add(text, null, null);
      }

      public ChatLine.Builder addClickable(@NotNull String text, @NotNull ChatLine.ClickAction clickAction, @Nullable ChatLine.HoverAction hoverAction) {
         return this.add(text, clickAction, hoverAction);
      }

      public ChatLine.Builder addClickable(@NotNull String text, @NotNull ChatLine.ClickAction clickAction, @NotNull String hoverText) {
         return this.add(text, clickAction, ChatLine.HoverAction.showText(hoverText));
      }

      public ChatLine build() {
         return new ChatLine(this.components);
      }
   }

   public static class ClickAction {
      private final Action type;
      private final String value;

      public ClickAction(Action type, String value) {
         this.type = type;
         this.value = value;
      }

      public static ChatLine.ClickAction runCommand(String command) {
         return new ChatLine.ClickAction(Action.RUN_COMMAND, command);
      }

      public static ChatLine.ClickAction suggestCommand(String command) {
         return new ChatLine.ClickAction(Action.SUGGEST_COMMAND, command);
      }

      public static ChatLine.ClickAction openUrl(String url) {
         return new ChatLine.ClickAction(Action.OPEN_URL, url);
      }
   }

   private static class Component {
      private final String text;
      private final ChatLine.ClickAction clickAction;
      private final ChatLine.HoverAction hoverAction;

      public Component(@NotNull String text, @Nullable ChatLine.ClickAction clickAction, @Nullable ChatLine.HoverAction hoverAction) {
         this.text = text;
         this.clickAction = clickAction;
         this.hoverAction = hoverAction;
      }
   }

   public static class HoverAction {
      private final net.md_5.bungee.api.chat.HoverEvent.Action type;
      private final String value;

      public HoverAction(net.md_5.bungee.api.chat.HoverEvent.Action type, String value) {
         this.type = type;
         this.value = value;
      }

      public static ChatLine.HoverAction showText(String text) {
         return new ChatLine.HoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, text);
      }
   }
}

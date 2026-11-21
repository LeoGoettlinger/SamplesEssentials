package me.clip.placeholderapi.replacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.ClickEvent.Payload;
import net.kyori.adventure.text.event.ClickEvent.Payload.Text;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.Style.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentReplacer {
   @NotNull
   public static Component replace(@NotNull Component component, @NotNull Function<String, String> replacer, @Nullable Function<String, Component> deserializer) {
      return rebuild(component, replacer, deserializer);
   }

   @NotNull
   private static Component rebuild(
      @NotNull Component component, @NotNull Function<String, String> replacer, @Nullable Function<String, Component> deserializer
   ) {
      Component rebuilt;
      if (component instanceof TextComponent) {
         TextComponent text = (TextComponent)component;
         String replaced = replacer.apply(text.content());
         rebuilt = (Component)(deserializer == null ? Component.text(replaced) : deserializer.apply(replaced));
      } else if (component instanceof TranslatableComponent) {
         TranslatableComponent translatable = (TranslatableComponent)component;
         List<Component> arguments = new ArrayList<>();

         for (ComponentLike arg : translatable.arguments()) {
            arguments.add(rebuild(arg.asComponent(), replacer, deserializer));
         }

         rebuilt = Component.translatable(translatable.key(), arguments);
      } else if (component instanceof KeybindComponent) {
         KeybindComponent keybind = (KeybindComponent)component;
         rebuilt = Component.keybind(keybind.keybind());
      } else if (component instanceof ScoreComponent) {
         ScoreComponent score = (ScoreComponent)component;
         rebuilt = Component.score(score.name(), score.objective());
      } else if (component instanceof SelectorComponent) {
         SelectorComponent selector = (SelectorComponent)component;
         rebuilt = Component.selector(selector.pattern());
      } else {
         rebuilt = Component.empty();
      }

      rebuilt = rebuilt.style(rebuildStyle(component.style(), replacer, deserializer));
      if (!component.children().isEmpty()) {
         List<Component> children = new ArrayList<>();

         for (Component child : component.children()) {
            children.add(rebuild(child, replacer, deserializer));
         }

         rebuilt = rebuilt.children(children);
      }

      return rebuilt;
   }

   @NotNull
   private static Style rebuildStyle(@NotNull Style style, @NotNull Function<String, String> replacer, @Nullable Function<String, Component> deserializer) {
      Builder builder = style.toBuilder();
      ClickEvent click = style.clickEvent();
      if (click != null) {
         builder.clickEvent(rebuildClickEvent(click, replacer));
      }

      HoverEvent<?> hover = style.hoverEvent();
      if (hover != null) {
         builder.hoverEvent(rebuildHoverEvent(hover, replacer, deserializer));
      }

      return builder.build();
   }

   @NotNull
   private static ClickEvent rebuildClickEvent(@NotNull ClickEvent click, @NotNull Function<String, String> replacer) {
      Payload payload = click.payload();
      if (!(payload instanceof Text)) {
         return click;
      } else {
         String original = ((Text)payload).value();
         String replaced = replacer.apply(original);
         Action action = click.action();
         switch (action) {
            case OPEN_URL:
               return ClickEvent.openUrl(replaced);
            case OPEN_FILE:
               return ClickEvent.openFile(replaced);
            case RUN_COMMAND:
               return ClickEvent.runCommand(replaced);
            case SUGGEST_COMMAND:
               return ClickEvent.suggestCommand(replaced);
            case COPY_TO_CLIPBOARD:
               return ClickEvent.copyToClipboard(replaced);
            default:
               return click;
         }
      }
   }

   @NotNull
   private static HoverEvent<?> rebuildHoverEvent(
      @NotNull HoverEvent<?> hover, @NotNull Function<String, String> replacer, @Nullable Function<String, Component> deserializer
   ) {
      Object value = hover.value();
      if (value instanceof Component) {
         Component rebuilt = rebuild((Component)value, replacer, deserializer);
         return HoverEvent.showText(rebuilt);
      } else if (value instanceof ShowItem) {
         return rebuildShowItem((ShowItem)value, replacer);
      } else if (value instanceof ShowEntity) {
         ShowEntity entity = (ShowEntity)value;
         Component rebuiltName = null;
         if (entity.name() != null) {
            rebuiltName = rebuild(entity.name(), replacer, deserializer);
         }

         return HoverEvent.showEntity(entity.type(), entity.id(), rebuiltName);
      } else {
         return hover;
      }
   }

   @NotNull
   private static HoverEvent<?> rebuildShowItem(@NotNull ShowItem item, @NotNull Function<String, String> replacer) {
      BinaryTagHolder nbt = item.nbt();
      if (nbt != null && !nbt.string().isEmpty()) {
         String replaced = replacer.apply(nbt.string());
         return HoverEvent.showItem(item.item(), item.count(), BinaryTagHolder.binaryTagHolder(replaced));
      } else {
         Map<Key, DataComponentValue> components = item.dataComponents();
         if (!components.isEmpty()) {
            Map<Key, DataComponentValue> rebuilt = new HashMap<>();

            for (Entry<Key, DataComponentValue> entry : components.entrySet()) {
               DataComponentValue value = entry.getValue();
               if (!(value instanceof BinaryTagHolder)) {
                  rebuilt.put(entry.getKey(), value);
               } else {
                  rebuilt.put(entry.getKey(), BinaryTagHolder.binaryTagHolder(replacer.apply(((BinaryTagHolder)value).string())));
               }
            }

            return HoverEvent.showItem(item.item(), item.count(), rebuilt);
         } else {
            return HoverEvent.showItem(item);
         }
      }
   }
}

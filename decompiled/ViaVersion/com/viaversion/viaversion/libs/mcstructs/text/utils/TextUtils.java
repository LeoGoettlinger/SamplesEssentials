package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.KeybindComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.HoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.EntityHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.impl.TextHoverEvent;
import com.viaversion.viaversion.libs.mcstructs.text.translation.Translator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class TextUtils {
   private static final String URL_PATTERN = "(?:https?://)?[\\w._-]+\\.\\w{2,}(?:/\\S*)?";

   public static TextComponent makeURLsClickable(TextComponent component) {
      return makeURLsClickable(component, true);
   }

   public static TextComponent makeURLsClickable(TextComponent component, boolean lenient) {
      return replace(component, "(?:https?://)?[\\w._-]+\\.\\w{2,}(?:/\\S*)?", comp -> {
         if (lenient) {
            comp.getStyle().setClickEvent(ClickEvent.openUrl(comp.asSingleString()));
         } else {
            try {
               comp.getStyle().setClickEvent(ClickEvent.openUrl(new URI(comp.asSingleString())));
            } catch (URISyntaxException var3) {
            }
         }

         return comp;
      });
   }

   public static TextComponent replace(TextComponent component, Function<TextComponent, TextComponent> replaceFunction) {
      TextComponent out = component.shallowCopy();
      out = replaceFunction.apply(out);
      if (out instanceof TranslationComponent) {
         Object[] args = ((TranslationComponent)out).getArgs();

         for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof TextComponent) {
               args[i] = replace((TextComponent)args[i], replaceFunction);
            }
         }
      }

      for (TextComponent sibling : component.getSiblings()) {
         out.append(replace(sibling, replaceFunction));
      }

      return out;
   }

   public static TextComponent replace(TextComponent component, String searchRegex, Function<TextComponent, TextComponent> replaceFunction) {
      Pattern pattern = Pattern.compile(searchRegex);
      TextComponent out;
      if (component instanceof StringComponent) {
         String text = component.asSingleString();
         Matcher matcher = pattern.matcher(text);
         List<TextComponent> parts = new ArrayList<>();

         int last;
         for (last = 0; matcher.find(); last = matcher.end()) {
            int start = matcher.start();
            String match = matcher.group();
            if (start > last) {
               parts.add(new StringComponent(text.substring(last, start)).setStyle(component.getStyle().copy()));
            }

            TextComponent replace = replaceFunction.apply(new StringComponent(match).setStyle(component.getStyle().copy()));
            if (replace != null) {
               parts.add(replace);
            }
         }

         if (last < text.length()) {
            parts.add(new StringComponent(text.substring(last)).setStyle(component.getStyle().copy()));
         }

         if (parts.size() > 1) {
            out = new StringComponent("");

            for (TextComponent part : parts) {
               out.append(part);
            }
         } else if (parts.size() == 1) {
            out = parts.get(0).shallowCopy();
         } else {
            out = component.shallowCopy();
         }
      } else {
         out = component.shallowCopy();
      }

      for (TextComponent sibling : component.getSiblings()) {
         TextComponent replace = replace(sibling, searchRegex, replaceFunction);
         out.append(replace);
      }

      return out;
   }

   public static TextComponent replaceRGBColors(TextComponent component) {
      TextComponent out = component.copy();
      out.forEach(comp -> {
         if (comp.getStyle().getColor() != null && comp.getStyle().getColor().isRGBColor()) {
            comp.getStyle().setFormatting(TextFormatting.getClosestFormattingColor(comp.getStyle().getColor().getRgbValue()));
         }
      });
      return out;
   }

   public static TextComponent join(TextComponent separator, TextComponent... components) {
      if (components.length == 0) {
         return new StringComponent("");
      } else if (components.length == 1) {
         return components[0].copy();
      } else {
         TextComponent out = null;

         for (TextComponent component : components) {
            if (out == null) {
               out = new StringComponent("").append(component.copy());
            } else {
               out.append(separator.copy()).append(component.copy());
            }
         }

         return out;
      }
   }

   public static void iterateAll(TextComponent component, Consumer<TextComponent> consumer) {
      consumer.accept(component);
      if (component instanceof TranslationComponent) {
         TranslationComponent translationComponent = (TranslationComponent)component;

         for (Object arg : translationComponent.getArgs()) {
            if (arg instanceof TextComponent) {
               iterateAll((TextComponent)arg, consumer);
            }
         }
      }

      if (component.getStyle().getHoverEvent() != null) {
         HoverEvent hoverEvent = component.getStyle().getHoverEvent();
         if (hoverEvent instanceof TextHoverEvent) {
            iterateAll(((TextHoverEvent)hoverEvent).getText(), consumer);
         } else if (hoverEvent instanceof EntityHoverEvent) {
            EntityHoverEvent entityHoverEvent = (EntityHoverEvent)hoverEvent;
            if (entityHoverEvent.isModern()) {
               TextComponent name = entityHoverEvent.asModern().getName();
               if (name != null) {
                  iterateAll(name, consumer);
               }
            }
         }
      }

      for (TextComponent sibling : component.getSiblings()) {
         iterateAll(sibling, consumer);
      }
   }

   public static void setTranslator(TextComponent component, @Nullable Translator translator) {
      setTranslator(component, translator, translator);
   }

   public static void setTranslator(TextComponent component, @Nullable Translator textTranslator, @Nullable Translator keyTranslator) {
      iterateAll(component, comp -> {
         if (comp instanceof TranslationComponent) {
            TranslationComponent translationComponent = (TranslationComponent)comp;
            translationComponent.setTranslator(textTranslator);
         } else if (comp instanceof KeybindComponent) {
            KeybindComponent keybindComponent = (KeybindComponent)comp;
            keybindComponent.setTranslator(keyTranslator);
         }
      });
   }

   public static TextComponent[] split(TextComponent component, String split, boolean resolveTranslations) {
      TextComponent rootCopy = component.copy();
      rootCopy.mergeSiblingParentStyle();
      List<TextComponent> components = new ArrayList<>();
      List<TextComponent> current = new ArrayList<>();
      Runnable addCurrent = () -> {
         boolean wasEmpty = current.isEmpty();
         current.removeIf(comp -> comp instanceof StringComponent && comp.asSingleString().isEmpty());
         if (current.size() == 1) {
            components.add(current.get(0));
         } else if (!wasEmpty) {
            TextComponent part = new StringComponent("");

            for (TextComponent textComponent : current) {
               part.append(textComponent);
            }

            components.add(part);
         }

         current.clear();
      };
      rootCopy.forEach(comp -> {
         if (comp instanceof StringComponent || comp instanceof TranslationComponent && resolveTranslations) {
            String text = comp.asSingleString();
            if (text.contains(split)) {
               String[] parts = text.split(split, -1);

               for (int i = 0; i < parts.length; i++) {
                  String part = parts[i];
                  TextComponent partComp = new StringComponent(part).setStyle(comp.getStyle().copy());
                  current.add(partComp);
                  if (i != parts.length - 1) {
                     addCurrent.run();
                  }
               }
            } else {
               current.add(comp.shallowCopy());
            }
         } else {
            current.add(comp.shallowCopy());
         }
      });
      addCurrent.run();
      return components.toArray(new TextComponent[0]);
   }
}

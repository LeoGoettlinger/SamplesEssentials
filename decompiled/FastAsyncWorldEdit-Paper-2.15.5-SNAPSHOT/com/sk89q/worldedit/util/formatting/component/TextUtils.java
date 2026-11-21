package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.Locale;

public class TextUtils {
   private TextUtils() {
   }

   public static Component join(List<Component> components, Component joiner) {
      TextComponent.Builder builder = TextComponent.builder();

      for (int i = 0; i < components.size(); i++) {
         builder.append(components.get(i));
         if (i < components.size() - 1) {
            builder.append(joiner);
         }
      }

      return builder.build();
   }

   public static Locale getLocaleByMinecraftTag(String locale) {
      return Locale.forLanguageTag(locale.replace('_', '-'));
   }
}

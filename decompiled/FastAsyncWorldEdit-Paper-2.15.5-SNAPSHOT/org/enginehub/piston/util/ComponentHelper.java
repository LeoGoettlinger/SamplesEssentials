package org.enginehub.piston.util;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Collector;
import javax.annotation.Nullable;
import org.enginehub.piston.config.ColorConfig;

public class ComponentHelper {
   public static Collector<Component, ?, Component> joiningWithBar() {
      return joiningTexts(TextComponent.empty(), ColorConfig.partWrapping().wrap("|"), TextComponent.empty());
   }

   public static Collector<Component, ?, Component> joiningTexts(Component prefix, Component delimiter, Component suffix) {
      return Collector.of(
         () -> new ComponentHelper.ComponentJoiner(prefix, delimiter, suffix),
         ComponentHelper.ComponentJoiner::add,
         ComponentHelper.ComponentJoiner::merge,
         ComponentHelper.ComponentJoiner::finish
      );
   }

   private ComponentHelper() {
   }

   private static final class ComponentJoiner {
      private final Component prefix;
      private final Component suffix;
      private final Component delimiter;
      @Nullable
      private TextComponent.Builder value;
      @Nullable
      private Component nullValue;

      private ComponentJoiner(Component prefix, Component delimiter, Component suffix) {
         this.prefix = prefix;
         this.suffix = suffix;
         this.delimiter = delimiter;
      }

      private TextComponent.Builder initBuilder() {
         if (this.value == null) {
            this.value = TextComponent.builder("");
         } else {
            this.value.append(this.delimiter);
         }

         return this.value;
      }

      public void add(Component component) {
         this.initBuilder().append(component);
      }

      public ComponentHelper.ComponentJoiner merge(ComponentHelper.ComponentJoiner other) {
         if (other.value != null) {
            this.initBuilder().append(other.value.build());
         }

         return this;
      }

      public Component finish() {
         if (this.value == null) {
            return (Component)(this.nullValue != null ? this.nullValue : this.initBuilder().append(this.prefix).append(this.suffix).build());
         } else {
            return TextComponent.builder("").append(this.prefix).append(this.value.build().children()).append(this.suffix).build();
         }
      }
   }
}

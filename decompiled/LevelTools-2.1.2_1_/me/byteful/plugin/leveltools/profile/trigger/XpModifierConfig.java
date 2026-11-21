package me.byteful.plugin.leveltools.profile.trigger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.NotNull;

public final class XpModifierConfig {
   private final XpModifierConfig.XpModifierRange defaultRange;
   private final Map<String, XpModifierConfig.XpModifierRange> customModifiers;

   public XpModifierConfig(double defaultMin, double defaultMax, @NotNull Map<String, XpModifierConfig.XpModifierRange> customModifiers) {
      this.defaultRange = new XpModifierConfig.XpModifierRange(defaultMin, defaultMax);
      this.customModifiers = Collections.unmodifiableMap(customModifiers);
   }

   public double getDefaultMin() {
      return this.defaultRange.getMin();
   }

   public double getDefaultMax() {
      return this.defaultRange.getMax();
   }

   @NotNull
   public Map<String, XpModifierConfig.XpModifierRange> getCustomModifiers() {
      return this.customModifiers;
   }

   public double calculateModifier(@NotNull String source) {
      XpModifierConfig.XpModifierRange range = this.customModifiers.get(source);
      return range != null ? range.random() : this.defaultRange.random();
   }

   public static final class XpModifierRange {
      private final double min;
      private final double max;

      public XpModifierRange(double min, double max) {
         this.min = min;
         this.max = max;
      }

      public double getMin() {
         return this.min;
      }

      public double getMax() {
         return this.max;
      }

      public double random() {
         return this.min == this.max ? this.min : ThreadLocalRandom.current().nextDouble(this.min, this.max);
      }
   }
}

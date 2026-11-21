package me.byteful.plugin.leveltools.profile.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerSettings {
   private final TriggerSettings.ClickMode clickMode;

   private TriggerSettings(@Nullable TriggerSettings.ClickMode clickMode) {
      this.clickMode = clickMode;
   }

   @NotNull
   public static TriggerSettings empty() {
      return new TriggerSettings(null);
   }

   @NotNull
   public static TriggerSettings forClick(@NotNull TriggerSettings.ClickMode mode) {
      return new TriggerSettings(mode);
   }

   @Nullable
   public TriggerSettings.ClickMode getClickMode() {
      return this.clickMode;
   }

   public static enum ClickMode {
      ANY,
      ON_BLOCK,
      ON_ENTITY,
      ON_AIR;

      @NotNull
      public static TriggerSettings.ClickMode fromString(@NotNull String value) {
         try {
            return valueOf(value.toUpperCase().replace("-", "_"));
         } catch (IllegalArgumentException var2) {
            return ANY;
         }
      }
   }
}

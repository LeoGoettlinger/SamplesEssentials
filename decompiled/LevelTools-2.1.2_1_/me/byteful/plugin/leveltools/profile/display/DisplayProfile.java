package me.byteful.plugin.leveltools.profile.display;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DisplayProfile {
   private final String id;
   private final DisplayProfile.NameDisplay nameDisplay;
   private final DisplayProfile.ActionBarDisplay actionBarDisplay;
   private final DisplayProfile.LoreDisplay loreDisplay;
   private final ProgressBarConfig progressBar;

   public DisplayProfile(
      @NotNull String id,
      @NotNull DisplayProfile.NameDisplay nameDisplay,
      @NotNull DisplayProfile.ActionBarDisplay actionBarDisplay,
      @NotNull DisplayProfile.LoreDisplay loreDisplay,
      @NotNull ProgressBarConfig progressBar
   ) {
      this.id = id;
      this.nameDisplay = nameDisplay;
      this.actionBarDisplay = actionBarDisplay;
      this.loreDisplay = loreDisplay;
      this.progressBar = progressBar;
   }

   public static DisplayProfile.Builder builder(@NotNull String id) {
      return new DisplayProfile.Builder(id);
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @NotNull
   public DisplayProfile.NameDisplay getNameDisplay() {
      return this.nameDisplay;
   }

   @NotNull
   public DisplayProfile.ActionBarDisplay getActionBarDisplay() {
      return this.actionBarDisplay;
   }

   @NotNull
   public DisplayProfile.LoreDisplay getLoreDisplay() {
      return this.loreDisplay;
   }

   @NotNull
   public ProgressBarConfig getProgressBar() {
      return this.progressBar;
   }

   public static final class ActionBarDisplay {
      private final boolean enabled;
      private final String text;

      public ActionBarDisplay(boolean enabled, @Nullable String text) {
         this.enabled = enabled;
         this.text = text != null ? text : "";
      }

      public static DisplayProfile.ActionBarDisplay disabled() {
         return new DisplayProfile.ActionBarDisplay(false, null);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      @NotNull
      public String getText() {
         return this.text;
      }
   }

   public static final class Builder {
      private final String id;
      private DisplayProfile.NameDisplay nameDisplay = DisplayProfile.NameDisplay.disabled();
      private DisplayProfile.ActionBarDisplay actionBarDisplay = DisplayProfile.ActionBarDisplay.disabled();
      private DisplayProfile.LoreDisplay loreDisplay = DisplayProfile.LoreDisplay.disabled();
      private ProgressBarConfig progressBar = ProgressBarConfig.defaultConfig();

      private Builder(@NotNull String id) {
         this.id = id;
      }

      public DisplayProfile.Builder nameDisplay(@NotNull DisplayProfile.NameDisplay nameDisplay) {
         this.nameDisplay = nameDisplay;
         return this;
      }

      public DisplayProfile.Builder actionBarDisplay(@NotNull DisplayProfile.ActionBarDisplay actionBarDisplay) {
         this.actionBarDisplay = actionBarDisplay;
         return this;
      }

      public DisplayProfile.Builder loreDisplay(@NotNull DisplayProfile.LoreDisplay loreDisplay) {
         this.loreDisplay = loreDisplay;
         return this;
      }

      public DisplayProfile.Builder progressBar(@NotNull ProgressBarConfig progressBar) {
         this.progressBar = progressBar;
         return this;
      }

      public DisplayProfile build() {
         return new DisplayProfile(this.id, this.nameDisplay, this.actionBarDisplay, this.loreDisplay, this.progressBar);
      }
   }

   public static final class LoreDisplay {
      private final boolean enabled;
      private final List<String> lines;

      public LoreDisplay(boolean enabled, @Nullable List<String> lines) {
         this.enabled = enabled;
         this.lines = lines != null ? Collections.unmodifiableList(lines) : Collections.emptyList();
      }

      public static DisplayProfile.LoreDisplay disabled() {
         return new DisplayProfile.LoreDisplay(false, null);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      @NotNull
      public List<String> getLines() {
         return this.lines;
      }
   }

   public static final class NameDisplay {
      private final boolean enabled;
      private final String text;

      public NameDisplay(boolean enabled, @Nullable String text) {
         this.enabled = enabled;
         this.text = text != null ? text : "";
      }

      public static DisplayProfile.NameDisplay disabled() {
         return new DisplayProfile.NameDisplay(false, null);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      @NotNull
      public String getText() {
         return this.text;
      }
   }
}

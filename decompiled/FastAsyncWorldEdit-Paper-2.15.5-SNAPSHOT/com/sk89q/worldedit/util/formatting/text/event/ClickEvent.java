package com.sk89q.worldedit.util.formatting.text.event;

import com.sk89q.worldedit.util.formatting.text.util.NameMap;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ClickEvent {
   private final ClickEvent.Action action;
   private final String value;

   @NonNull
   public static ClickEvent of(@NonNull final ClickEvent.Action action, @NonNull final String value) {
      return new ClickEvent(action, value);
   }

   @NonNull
   public static ClickEvent openUrl(@NonNull final String url) {
      return of(ClickEvent.Action.OPEN_URL, url);
   }

   @NonNull
   public static ClickEvent openFile(@NonNull final String file) {
      return of(ClickEvent.Action.OPEN_FILE, file);
   }

   @NonNull
   public static ClickEvent runCommand(@NonNull final String command) {
      return of(ClickEvent.Action.RUN_COMMAND, command);
   }

   @NonNull
   public static ClickEvent suggestCommand(@NonNull final String command) {
      return of(ClickEvent.Action.SUGGEST_COMMAND, command);
   }

   @NonNull
   public static ClickEvent changePage(@NonNull final String page) {
      return of(ClickEvent.Action.CHANGE_PAGE, page);
   }

   @NonNull
   public static ClickEvent changePage(final int page) {
      return changePage(String.valueOf(page));
   }

   @NonNull
   public static ClickEvent copyToClipboard(@NonNull final String text) {
      return of(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
   }

   private ClickEvent(@NonNull final ClickEvent.Action action, @NonNull final String value) {
      this.action = Objects.requireNonNull(action, "action");
      this.value = Objects.requireNonNull(value, "value");
   }

   @NonNull
   public ClickEvent.Action action() {
      return this.action;
   }

   @NonNull
   public String value() {
      return this.value;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         ClickEvent that = (ClickEvent)other;
         return this.action == that.action && Objects.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.action.hashCode();
      return 31 * result + this.value.hashCode();
   }

   @Override
   public String toString() {
      return ShadyPines.toString(this, map -> {
         map.put("action", this.action);
         map.put("value", this.value);
      });
   }

   public static enum Action {
      OPEN_URL("open_url", true),
      OPEN_FILE("open_file", false),
      RUN_COMMAND("run_command", true),
      SUGGEST_COMMAND("suggest_command", true),
      CHANGE_PAGE("change_page", true),
      COPY_TO_CLIPBOARD("copy_to_clipboard", true);

      public static final NameMap<ClickEvent.Action> NAMES = NameMap.create(ClickEvent.Action.class, constant -> constant.name);
      private final String name;
      private final boolean readable;

      private Action(@NonNull final String name, final boolean readable) {
         this.name = name;
         this.readable = readable;
      }

      public boolean readable() {
         return this.readable;
      }

      @NonNull
      @Override
      public String toString() {
         return this.name;
      }
   }
}

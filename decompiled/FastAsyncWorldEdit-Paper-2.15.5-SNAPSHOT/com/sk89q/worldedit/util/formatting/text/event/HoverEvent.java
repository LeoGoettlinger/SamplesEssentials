package com.sk89q.worldedit.util.formatting.text.event;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.util.NameMap;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class HoverEvent {
   private final HoverEvent.Action action;
   private final Component value;

   @NonNull
   public static HoverEvent of(@NonNull final HoverEvent.Action action, @NonNull final Component value) {
      return new HoverEvent(action, value);
   }

   @NonNull
   public static HoverEvent showText(@NonNull final Component text) {
      return of(HoverEvent.Action.SHOW_TEXT, text);
   }

   @NonNull
   public static HoverEvent showItem(@NonNull final Component item) {
      return of(HoverEvent.Action.SHOW_ITEM, item);
   }

   @NonNull
   public static HoverEvent showEntity(@NonNull final Component entity) {
      return of(HoverEvent.Action.SHOW_ENTITY, entity);
   }

   private HoverEvent(@NonNull final HoverEvent.Action action, @NonNull final Component value) {
      this.action = Objects.requireNonNull(action, "action");
      this.value = Objects.requireNonNull(value, "value");
   }

   @NonNull
   public HoverEvent.Action action() {
      return this.action;
   }

   @NonNull
   public Component value() {
      return this.value;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         HoverEvent that = (HoverEvent)other;
         return this.action == that.action && this.value.equals(that.value);
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
      SHOW_TEXT("show_text", true),
      SHOW_ITEM("show_item", true),
      SHOW_ENTITY("show_entity", true);

      public static final NameMap<HoverEvent.Action> NAMES = NameMap.create(HoverEvent.Action.class, constant -> constant.name);
      private final String name;
      private final boolean readable;

      private Action(final String name, final boolean readable) {
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

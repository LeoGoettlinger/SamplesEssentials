package com.sk89q.worldedit.util.formatting.text.format;

import com.sk89q.worldedit.util.formatting.text.util.NameMap;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum TextDecoration implements TextFormat {
   OBFUSCATED("obfuscated"),
   BOLD("bold"),
   STRIKETHROUGH("strikethrough"),
   UNDERLINED("underlined"),
   ITALIC("italic");

   public static final NameMap<TextDecoration> NAMES = NameMap.create(TextDecoration.class, constant -> constant.name);
   private final String name;

   private TextDecoration(final String name) {
      this.name = name;
   }

   @NonNull
   @Override
   public String toString() {
      return this.name;
   }

   @Deprecated
   @NonNull
   public static Set<TextDecoration> setOf(final TextDecoration... decorations) {
      return ShadyPines.enumSet(TextDecoration.class, decorations);
   }

   public static enum State {
      NOT_SET {
         @Override
         public String toString() {
            return "null";
         }
      },
      FALSE,
      TRUE;

      private State() {
      }

      @Override
      public String toString() {
         return this.name().toLowerCase();
      }

      @NonNull
      public static TextDecoration.State byBoolean(final boolean flag) {
         return flag ? TRUE : FALSE;
      }

      @NonNull
      public static TextDecoration.State byBoolean(@Nullable final Boolean flag) {
         return flag == null ? NOT_SET : byBoolean(flag.booleanValue());
      }
   }
}

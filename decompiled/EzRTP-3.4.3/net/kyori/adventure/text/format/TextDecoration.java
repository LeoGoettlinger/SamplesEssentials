package net.kyori.adventure.text.format;

import java.util.Objects;
import net.kyori.adventure.util.Index;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

public enum TextDecoration implements StyleBuilderApplicable, TextFormat {
   OBFUSCATED("obfuscated"),
   BOLD("bold"),
   STRIKETHROUGH("strikethrough"),
   UNDERLINED("underlined"),
   ITALIC("italic");

   public static final Index<String, TextDecoration> NAMES = Index.create(TextDecoration.class, constant -> constant.name);
   private final String name;

   private TextDecoration(final String name) {
      this.name = name;
   }

   public final TextDecorationAndState withState(final boolean state) {
      return new TextDecorationAndStateImpl(this, TextDecoration.State.byBoolean(state));
   }

   public final TextDecorationAndState withState(final TextDecoration.State state) {
      return new TextDecorationAndStateImpl(this, state);
   }

   public final TextDecorationAndState withState(final TriState state) {
      return new TextDecorationAndStateImpl(this, TextDecoration.State.byTriState(state));
   }

   @Override
   public void styleApply(final Style.Builder style) {
      style.decorate(this);
   }

   @Override
   public String toString() {
      return this.name;
   }

   public static enum State {
      NOT_SET("not_set"),
      FALSE("false"),
      TRUE("true");

      private final String name;

      private State(final String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public static TextDecoration.State byBoolean(final boolean flag) {
         return flag ? TRUE : FALSE;
      }

      public static TextDecoration.State byBoolean(@Nullable final Boolean flag) {
         return flag == null ? NOT_SET : byBoolean(flag.booleanValue());
      }

      public static TextDecoration.State byTriState(final TriState flag) {
         return switch ((TriState)Objects.requireNonNull(flag, "flag")) {
            case TRUE -> TRUE;
            case FALSE -> FALSE;
            case NOT_SET -> NOT_SET;
         };
      }
   }
}

package ac.grim.grimac.shaded.kyori.adventure.text.format;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.internal.Internals;
import java.util.Objects;

final class TextDecorationAndStateImpl implements TextDecorationAndState {
   private final TextDecoration decoration;
   private final TextDecoration.State state;

   TextDecorationAndStateImpl(final TextDecoration decoration, final TextDecoration.State state) {
      this.decoration = decoration;
      this.state = Objects.requireNonNull(state, "state");
   }

   @NotNull
   @Override
   public TextDecoration decoration() {
      return this.decoration;
   }

   @NotNull
   @Override
   public TextDecoration.State state() {
      return this.state;
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         TextDecorationAndStateImpl that = (TextDecorationAndStateImpl)other;
         return this.decoration == that.decoration && this.state == that.state;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.decoration.hashCode();
      return 31 * result + this.state.hashCode();
   }
}

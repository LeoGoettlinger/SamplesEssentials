package de.theredend2000.advancedhunt.lib.kyori.adventure.text.format;

import java.util.Objects;

record TextDecorationAndStateImpl(TextDecoration decoration, TextDecoration.State state) implements TextDecorationAndState {
   TextDecorationAndStateImpl(final TextDecoration decoration, final TextDecoration.State state) {
      this.decoration = decoration;
      this.state = Objects.requireNonNull(state, "state");
   }
}

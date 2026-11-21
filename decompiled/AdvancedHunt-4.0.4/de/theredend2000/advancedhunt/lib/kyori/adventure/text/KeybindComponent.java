package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import java.util.Objects;
import org.jetbrains.annotations.Contract;

public sealed interface KeybindComponent extends ScopedComponent<KeybindComponent> permits KeybindComponentImpl {
   String keybind();

   @Contract(
      pure = true
   )
   KeybindComponent keybind(final String keybind);

   @Contract(
      pure = true
   )
   default KeybindComponent keybind(final KeybindComponent.KeybindLike keybind) {
      return this.keybind(Objects.requireNonNull(keybind, "keybind").asKeybind());
   }

   KeybindComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<KeybindComponent, KeybindComponent.Builder> permits KeybindComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      KeybindComponent.Builder keybind(final String keybind);

      @Contract(
         pure = true
      )
      default KeybindComponent.Builder keybind(final KeybindComponent.KeybindLike keybind) {
         return this.keybind(Objects.requireNonNull(keybind, "keybind").asKeybind());
      }
   }

   public interface KeybindLike {
      String asKeybind();
   }
}

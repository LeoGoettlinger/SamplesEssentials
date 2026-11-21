package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.Nullable;

record KeybindComponentImpl(List<Component> children, Style style, String keybind) implements KeybindComponent {
   static KeybindComponent create(final List<? extends ComponentLike> children, final Style style, final String keybind) {
      return new KeybindComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY), Objects.requireNonNull(style, "style"), Objects.requireNonNull(keybind, "keybind")
      );
   }

   @Override
   public KeybindComponent keybind(final String keybind) {
      return (KeybindComponent)(Objects.equals(this.keybind, keybind) ? this : create(this.children, this.style, keybind));
   }

   public KeybindComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.keybind);
   }

   public KeybindComponent style(final Style style) {
      return create(this.children, style, this.keybind);
   }

   @Override
   public KeybindComponent.Builder toBuilder() {
      return new KeybindComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<KeybindComponent, KeybindComponent.Builder> implements KeybindComponent.Builder {
      @Nullable
      private String keybind;

      BuilderImpl() {
      }

      BuilderImpl(final KeybindComponent component) {
         super(component);
         this.keybind = component.keybind();
      }

      @Override
      public KeybindComponent.Builder keybind(final String keybind) {
         this.keybind = Objects.requireNonNull(keybind, "keybind");
         return this;
      }

      public KeybindComponent build() {
         if (this.keybind == null) {
            throw new IllegalStateException("keybind must be set");
         } else {
            return KeybindComponentImpl.create(this.children, this.buildStyle(), this.keybind);
         }
      }
   }
}

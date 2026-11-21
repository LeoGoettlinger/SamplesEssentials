package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class KeybindComponentImpl extends AbstractComponent implements KeybindComponent {
   private final String keybind;

   KeybindComponentImpl(@NonNull final List<Component> children, @NonNull final Style style, @NonNull final String keybind) {
      super(children, style);
      this.keybind = keybind;
   }

   @NonNull
   @Override
   public String keybind() {
      return this.keybind;
   }

   @NonNull
   @Override
   public KeybindComponent keybind(@NonNull final String keybind) {
      return Objects.equals(this.keybind, keybind) ? this : new KeybindComponentImpl(this.children, this.style, Objects.requireNonNull(keybind, "keybind"));
   }

   @NonNull
   public KeybindComponent children(@NonNull final List<Component> children) {
      return new KeybindComponentImpl(children, this.style, this.keybind);
   }

   @NonNull
   public KeybindComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new KeybindComponentImpl(this.children, style, this.keybind);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof KeybindComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         KeybindComponent that = (KeybindComponent)other;
         return Objects.equals(this.keybind, that.keybind());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.keybind.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("keybind", this.keybind);
   }

   @NonNull
   public KeybindComponent.Builder toBuilder() {
      return new KeybindComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<KeybindComponent, KeybindComponent.Builder> implements KeybindComponent.Builder {
      @Nullable
      private String keybind;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final KeybindComponent component) {
         super(component);
         this.keybind = component.keybind();
      }

      @NonNull
      @Override
      public KeybindComponent.Builder keybind(@NonNull final String keybind) {
         this.keybind = keybind;
         return this;
      }

      @NonNull
      public KeybindComponent build() {
         if (this.keybind == null) {
            throw new IllegalStateException("keybind must be set");
         } else {
            return new KeybindComponentImpl(this.children, this.buildStyle(), this.keybind);
         }
      }
   }
}

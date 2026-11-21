package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class VirtualComponentImpl<C> extends TextComponentImpl implements VirtualComponent {
   private final Class<C> contextType;
   private final VirtualComponentRenderer<C> renderer;

   static <C> VirtualComponent createVirtual(final Class<C> contextType, final VirtualComponentRenderer<C> renderer) {
      return createVirtual(contextType, renderer, List.of(), Style.empty());
   }

   static <C> VirtualComponent createVirtual(
      final Class<C> contextType, final VirtualComponentRenderer<C> renderer, final List<? extends ComponentLike> children, final Style style
   ) {
      List<Component> filteredChildren = ComponentLike.asComponents(children, IS_NOT_EMPTY);
      return new VirtualComponentImpl<>(filteredChildren, style, "", contextType, renderer);
   }

   private VirtualComponentImpl(
      final List<Component> children, final Style style, final String content, final Class<C> contextType, final VirtualComponentRenderer<C> renderer
   ) {
      super(children, style, content);
      this.contextType = contextType;
      this.renderer = renderer;
   }

   VirtualComponent create0(final List<? extends ComponentLike> children, final Style style, final String content) {
      return new VirtualComponentImpl<>(ComponentLike.asComponents(children, IS_NOT_EMPTY), style, content, this.contextType, this.renderer);
   }

   @Override
   public Class<C> contextType() {
      return this.contextType;
   }

   @Override
   public VirtualComponentRenderer<C> renderer() {
      return this.renderer;
   }

   @Override
   public String content() {
      return this.renderer.fallbackString();
   }

   @Override
   public TextComponent.Builder toBuilder() {
      return new VirtualComponentImpl.BuilderImpl<>(this);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other instanceof VirtualComponentImpl<?> that) {
         return !super.equals(other) ? false : Objects.equals(this.contextType, that.contextType) && Objects.equals(this.renderer, that.renderer);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.contextType.hashCode();
      return 31 * result + this.renderer.hashCode();
   }

   static final class BuilderImpl<C> extends TextComponentImpl.BuilderImpl {
      private final Class<C> contextType;
      private final VirtualComponentRenderer<C> renderer;

      BuilderImpl(final VirtualComponentImpl<C> other) {
         super(other);
         this.contextType = other.contextType();
         this.renderer = other.renderer();
      }

      @Override
      public TextComponent build() {
         return VirtualComponentImpl.createVirtual(this.contextType, this.renderer, this.children, this.buildStyle());
      }
   }
}

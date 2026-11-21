package me.clip.placeholderapi.libs.kyori.adventure.text;

import java.util.Collections;
import java.util.List;
import me.clip.placeholderapi.libs.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

final class VirtualComponentImpl<C> extends TextComponentImpl implements VirtualComponent {
   private final Class<C> contextType;
   private final VirtualComponentRenderer<C> renderer;

   static <C> VirtualComponent createVirtual(@NotNull final Class<C> contextType, @NotNull final VirtualComponentRenderer<C> renderer) {
      return createVirtual(contextType, renderer, Collections.emptyList(), Style.empty());
   }

   static <C> VirtualComponent createVirtual(
      @NotNull final Class<C> contextType, @NotNull final VirtualComponentRenderer<C> renderer, final List<? extends ComponentLike> children, final Style style
   ) {
      List<Component> filteredChildren = ComponentLike.asComponents(children, IS_NOT_EMPTY);
      return new VirtualComponentImpl<>(filteredChildren, style, "", contextType, renderer);
   }

   private VirtualComponentImpl(
      @NotNull final List<Component> children,
      @NotNull final Style style,
      @NotNull final String content,
      @NotNull final Class<C> contextType,
      @NotNull final VirtualComponentRenderer<C> renderer
   ) {
      super(children, style, content);
      this.contextType = contextType;
      this.renderer = renderer;
   }

   VirtualComponent create0(@NotNull final List<? extends ComponentLike> children, @NotNull final Style style, @NotNull final String content) {
      return new VirtualComponentImpl<>(ComponentLike.asComponents(children, IS_NOT_EMPTY), style, content, this.contextType, this.renderer);
   }

   @NotNull
   @Override
   public Class<C> contextType() {
      return this.contextType;
   }

   @NotNull
   @Override
   public VirtualComponentRenderer<C> renderer() {
      return this.renderer;
   }

   @NotNull
   @Override
   public String content() {
      return this.renderer.fallbackString();
   }

   @NotNull
   @Override
   public TextComponent.Builder toBuilder() {
      return new VirtualComponentImpl.BuilderImpl<>(this);
   }

   static final class BuilderImpl<C> extends TextComponentImpl.BuilderImpl {
      private final Class<C> contextType;
      private final VirtualComponentRenderer<C> renderer;

      BuilderImpl(final VirtualComponentImpl<C> other) {
         super(other);
         this.contextType = other.contextType();
         this.renderer = other.renderer();
      }

      @NotNull
      @Override
      public TextComponent build() {
         return VirtualComponentImpl.createVirtual(this.contextType, this.renderer, this.children, this.buildStyle());
      }
   }
}

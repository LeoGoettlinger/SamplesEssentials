package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.Nullable;

record ObjectComponentImpl(List<Component> children, Style style, ObjectContents contents, @Nullable Component fallback) implements ObjectComponent {
   static ObjectComponentImpl create(
      final List<? extends ComponentLike> children, final Style style, final ObjectContents objectContents, @Nullable final ComponentLike fallback
   ) {
      return new ObjectComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(objectContents, "contents"),
         ComponentLike.unbox(fallback)
      );
   }

   @Override
   public ObjectComponent contents(final ObjectContents contents) {
      return create(this.children, this.style, contents, this.fallback);
   }

   @Override
   public ObjectComponent fallback(@Nullable final ComponentLike fallback) {
      return create(this.children, this.style, this.contents, fallback);
   }

   @Override
   public ObjectComponent.Builder toBuilder() {
      return new ObjectComponentImpl.BuilderImpl(this);
   }

   public ObjectComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.contents, this.fallback);
   }

   public ObjectComponent style(final Style style) {
      return create(this.children, style, this.contents, this.fallback);
   }

   @Override
   public ObjectContents asObjectContents() {
      return this.contents;
   }

   static final class BuilderImpl extends AbstractComponentBuilder<ObjectComponent, ObjectComponent.Builder> implements ObjectComponent.Builder {
      @Nullable
      private ObjectContents objectContents;
      @Nullable
      private ComponentLike fallback;

      BuilderImpl() {
      }

      BuilderImpl(final ObjectComponent component) {
         super(component);
         this.objectContents = component.contents();
         this.fallback = component.fallback();
      }

      @Override
      public ObjectComponent.Builder contents(final ObjectContents objectContents) {
         this.objectContents = Objects.requireNonNull(objectContents, "contents");
         return this;
      }

      @Override
      public ObjectComponent.Builder fallback(@Nullable final ComponentLike fallback) {
         this.fallback = fallback;
         return this;
      }

      public ObjectComponent build() {
         if (this.objectContents == null) {
            throw new IllegalStateException("contents must be set");
         } else {
            return ObjectComponentImpl.create(this.children, this.buildStyle(), this.objectContents, this.fallback);
         }
      }
   }
}

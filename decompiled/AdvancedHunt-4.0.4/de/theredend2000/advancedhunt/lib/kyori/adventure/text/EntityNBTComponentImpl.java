package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

record EntityNBTComponentImpl(List<Component> children, Style style, String nbtPath, boolean interpret, Component separator, String selector, boolean plain)
   implements EntityNBTComponent {
   static EntityNBTComponent create(
      final List<? extends ComponentLike> children,
      final Style style,
      final String nbtPath,
      final boolean interpret,
      @Nullable final ComponentLike separator,
      final String selector,
      final boolean plain
   ) {
      return new EntityNBTComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(nbtPath, "nbtPath"),
         interpret,
         ComponentLike.unbox(separator),
         Objects.requireNonNull(selector, "selector"),
         plain
      );
   }

   public EntityNBTComponent nbtPath(final String nbtPath) {
      return (EntityNBTComponent)(Objects.equals(this.nbtPath, nbtPath)
         ? this
         : create(this.children, this.style, nbtPath, this.interpret, this.separator, this.selector, this.plain));
   }

   public EntityNBTComponent interpret(final boolean interpret) {
      if (this.interpret == interpret) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(interpret, this.plain);
         return create(this.children, this.style, this.nbtPath, interpret, this.separator, this.selector, this.plain);
      }
   }

   public EntityNBTComponent plain(final boolean plain) {
      if (this.plain == plain) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(this.interpret, plain);
         return create(this.children, this.style, this.nbtPath, this.interpret, this.separator, this.selector, plain);
      }
   }

   public EntityNBTComponent separator(@Nullable final ComponentLike separator) {
      return create(this.children, this.style, this.nbtPath, this.interpret, separator, this.selector, this.plain);
   }

   @Override
   public EntityNBTComponent selector(final String selector) {
      return (EntityNBTComponent)(Objects.equals(this.selector, selector)
         ? this
         : create(this.children, this.style, this.nbtPath, this.interpret, this.separator, selector, this.plain));
   }

   public EntityNBTComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.nbtPath, this.interpret, this.separator, this.selector, this.plain);
   }

   public EntityNBTComponent style(final Style style) {
      return create(this.children, style, this.nbtPath, this.interpret, this.separator, this.selector, this.plain);
   }

   @Override
   public EntityNBTComponent.Builder toBuilder() {
      return new EntityNBTComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractNBTComponentBuilder<EntityNBTComponent, EntityNBTComponent.Builder> implements EntityNBTComponent.Builder {
      @Nullable
      private String selector;

      BuilderImpl() {
      }

      BuilderImpl(final EntityNBTComponent component) {
         super(component);
         this.selector = component.selector();
      }

      @Override
      public EntityNBTComponent.Builder selector(final String selector) {
         this.selector = Objects.requireNonNull(selector, "selector");
         return this;
      }

      public EntityNBTComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.selector == null) {
            throw new IllegalStateException("selector must be set");
         } else {
            return EntityNBTComponentImpl.create(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.separator, this.selector, this.plain);
         }
      }
   }
}

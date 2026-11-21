package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

record SelectorComponentImpl(List<Component> children, Style style, String pattern, @Nullable Component separator) implements SelectorComponent {
   static SelectorComponent create(
      final List<? extends ComponentLike> children, final Style style, final String pattern, @Nullable final ComponentLike separator
   ) {
      return new SelectorComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(pattern, "pattern"),
         ComponentLike.unbox(separator)
      );
   }

   @Override
   public SelectorComponent pattern(final String pattern) {
      return (SelectorComponent)(Objects.equals(this.pattern, pattern) ? this : create(this.children, this.style, pattern, this.separator));
   }

   @Override
   public SelectorComponent separator(@Nullable final ComponentLike separator) {
      return create(this.children, this.style, this.pattern, separator);
   }

   public SelectorComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.pattern, this.separator);
   }

   public SelectorComponent style(final Style style) {
      return create(this.children, style, this.pattern, this.separator);
   }

   @Override
   public SelectorComponent.Builder toBuilder() {
      return new SelectorComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<SelectorComponent, SelectorComponent.Builder> implements SelectorComponent.Builder {
      @Nullable
      private String pattern;
      @Nullable
      private Component separator;

      BuilderImpl() {
      }

      BuilderImpl(final SelectorComponent component) {
         super(component);
         this.pattern = component.pattern();
         this.separator = component.separator();
      }

      @Override
      public SelectorComponent.Builder pattern(final String pattern) {
         this.pattern = Objects.requireNonNull(pattern, "pattern");
         return this;
      }

      @Override
      public SelectorComponent.Builder separator(@Nullable final ComponentLike separator) {
         this.separator = ComponentLike.unbox(separator);
         return this;
      }

      public SelectorComponent build() {
         if (this.pattern == null) {
            throw new IllegalStateException("pattern must be set");
         } else {
            return SelectorComponentImpl.create(this.children, this.buildStyle(), this.pattern, this.separator);
         }
      }
   }
}

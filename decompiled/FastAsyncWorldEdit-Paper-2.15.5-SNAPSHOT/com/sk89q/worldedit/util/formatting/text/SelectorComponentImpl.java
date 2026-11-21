package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SelectorComponentImpl extends AbstractComponent implements SelectorComponent {
   private final String pattern;

   SelectorComponentImpl(@NonNull final List<Component> children, @NonNull final Style style, @NonNull final String pattern) {
      super(children, style);
      this.pattern = pattern;
   }

   @NonNull
   @Override
   public String pattern() {
      return this.pattern;
   }

   @NonNull
   @Override
   public SelectorComponent pattern(@NonNull final String pattern) {
      return Objects.equals(this.pattern, pattern) ? this : new SelectorComponentImpl(this.children, this.style, Objects.requireNonNull(pattern, "pattern"));
   }

   @NonNull
   public SelectorComponent children(@NonNull final List<Component> children) {
      return new SelectorComponentImpl(children, this.style, this.pattern);
   }

   @NonNull
   public SelectorComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new SelectorComponentImpl(this.children, style, this.pattern);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof SelectorComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         SelectorComponent that = (SelectorComponent)other;
         return Objects.equals(this.pattern, that.pattern());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.pattern.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("pattern", this.pattern);
   }

   @NonNull
   public SelectorComponent.Builder toBuilder() {
      return new SelectorComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<SelectorComponent, SelectorComponent.Builder> implements SelectorComponent.Builder {
      @Nullable
      private String pattern;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final SelectorComponent component) {
         super(component);
         this.pattern = component.pattern();
      }

      @NonNull
      @Override
      public SelectorComponent.Builder pattern(@NonNull final String pattern) {
         this.pattern = pattern;
         return this;
      }

      @NonNull
      public SelectorComponent build() {
         if (this.pattern == null) {
            throw new IllegalStateException("pattern must be set");
         } else {
            return new SelectorComponentImpl(this.children, this.buildStyle(), this.pattern);
         }
      }
   }
}

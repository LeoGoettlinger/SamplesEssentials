package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class ScoreComponentImpl extends AbstractComponent implements ScoreComponent {
   private final String name;
   private final String objective;
   @Nullable
   private final String value;

   ScoreComponentImpl(
      @NonNull final List<Component> children,
      @NonNull final Style style,
      @NonNull final String name,
      @NonNull final String objective,
      @Nullable final String value
   ) {
      super(children, style);
      this.name = name;
      this.objective = objective;
      this.value = value;
   }

   @NonNull
   @Override
   public String name() {
      return this.name;
   }

   @NonNull
   @Override
   public ScoreComponent name(@NonNull final String name) {
      return Objects.equals(this.name, name)
         ? this
         : new ScoreComponentImpl(this.children, this.style, Objects.requireNonNull(name, "name"), this.objective, this.value);
   }

   @NonNull
   @Override
   public String objective() {
      return this.objective;
   }

   @NonNull
   @Override
   public ScoreComponent objective(@NonNull final String objective) {
      return Objects.equals(this.objective, objective)
         ? this
         : new ScoreComponentImpl(this.children, this.style, this.name, Objects.requireNonNull(objective, "objective"), this.value);
   }

   @Nullable
   @Override
   public String value() {
      return this.value;
   }

   @NonNull
   @Override
   public ScoreComponent value(@Nullable final String value) {
      return Objects.equals(this.value, value) ? this : new ScoreComponentImpl(this.children, this.style, this.name, this.objective, value);
   }

   @NonNull
   public ScoreComponent children(@NonNull final List<Component> children) {
      return new ScoreComponentImpl(children, this.style, this.name, this.objective, this.value);
   }

   @NonNull
   public ScoreComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new ScoreComponentImpl(this.children, style, this.name, this.objective, this.value);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof ScoreComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         ScoreComponent that = (ScoreComponent)other;
         return Objects.equals(this.name, that.name()) && Objects.equals(this.objective, that.objective()) && Objects.equals(this.value, that.value());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.name.hashCode();
      result = 31 * result + this.objective.hashCode();
      return 31 * result + Objects.hashCode(this.value);
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("name", this.name);
      builder.put("objective", this.objective);
      builder.put("value", this.value);
   }

   @NonNull
   public ScoreComponent.Builder toBuilder() {
      return new ScoreComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<ScoreComponent, ScoreComponent.Builder> implements ScoreComponent.Builder {
      @Nullable
      private String name;
      @Nullable
      private String objective;
      @Nullable
      private String value;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final ScoreComponent component) {
         super(component);
         this.name = component.name();
         this.objective = component.objective();
         this.value = component.value();
      }

      @NonNull
      @Override
      public ScoreComponent.Builder name(@NonNull final String name) {
         this.name = name;
         return this;
      }

      @NonNull
      @Override
      public ScoreComponent.Builder objective(@NonNull final String objective) {
         this.objective = objective;
         return this;
      }

      @NonNull
      @Override
      public ScoreComponent.Builder value(@Nullable final String value) {
         this.value = value;
         return this;
      }

      @NonNull
      public ScoreComponent build() {
         if (this.name == null) {
            throw new IllegalStateException("name must be set");
         } else if (this.objective == null) {
            throw new IllegalStateException("objective must be set");
         } else {
            return new ScoreComponentImpl(this.children, this.buildStyle(), this.name, this.objective, this.value);
         }
      }
   }
}

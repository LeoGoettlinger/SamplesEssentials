package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.Nullable;

record ScoreComponentImpl(List<Component> children, Style style, String name, String objective, @Nullable String value) implements ScoreComponent {
   static ScoreComponent create(
      final List<? extends ComponentLike> children, final Style style, final String name, final String objective, @Nullable final String value
   ) {
      return new ScoreComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(name, "name"),
         Objects.requireNonNull(objective, "objective"),
         value
      );
   }

   @Override
   public ScoreComponent name(final String name) {
      return (ScoreComponent)(Objects.equals(this.name, name) ? this : create(this.children, this.style, name, this.objective, this.value));
   }

   @Override
   public ScoreComponent objective(final String objective) {
      return (ScoreComponent)(Objects.equals(this.objective, objective) ? this : create(this.children, this.style, this.name, objective, this.value));
   }

   @Override
   public ScoreComponent value(@Nullable final String value) {
      return (ScoreComponent)(Objects.equals(this.value, value) ? this : create(this.children, this.style, this.name, this.objective, value));
   }

   public ScoreComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.name, this.objective, this.value);
   }

   public ScoreComponent style(final Style style) {
      return create(this.children, style, this.name, this.objective, this.value);
   }

   @Override
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

      BuilderImpl(final ScoreComponent component) {
         super(component);
         this.name = component.name();
         this.objective = component.objective();
      }

      @Override
      public ScoreComponent.Builder name(final String name) {
         this.name = Objects.requireNonNull(name, "name");
         return this;
      }

      @Override
      public ScoreComponent.Builder objective(final String objective) {
         this.objective = Objects.requireNonNull(objective, "objective");
         return this;
      }

      @Override
      public ScoreComponent.Builder value(@Nullable final String value) {
         this.value = value;
         return this;
      }

      public ScoreComponent build() {
         if (this.name == null) {
            throw new IllegalStateException("name must be set");
         } else if (this.objective == null) {
            throw new IllegalStateException("objective must be set");
         } else {
            return ScoreComponentImpl.create(this.children, this.buildStyle(), this.name, this.objective, this.value);
         }
      }
   }
}

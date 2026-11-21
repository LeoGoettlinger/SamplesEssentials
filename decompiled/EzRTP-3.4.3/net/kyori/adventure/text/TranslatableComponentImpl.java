package net.kyori.adventure.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.Nullable;

record TranslatableComponentImpl(List<Component> children, Style style, String key, @Nullable String fallback, List<TranslationArgument> args)
   implements TranslatableComponent {
   static TranslatableComponent create(
      final List<Component> children, final Style style, final String key, @Nullable final String fallback, final ComponentLike[] args
   ) {
      Objects.requireNonNull(args, "args");
      return create(children, style, key, fallback, List.of(args));
   }

   static TranslatableComponent create(
      final List<? extends ComponentLike> children,
      final Style style,
      final String key,
      @Nullable final String fallback,
      final List<? extends ComponentLike> args
   ) {
      return new TranslatableComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(key, "key"),
         fallback,
         asArguments(args)
      );
   }

   @Override
   public TranslatableComponent key(final String key) {
      return (TranslatableComponent)(Objects.equals(this.key, key) ? this : create(this.children, this.style, key, this.fallback, this.args));
   }

   @Override
   public List<TranslationArgument> arguments() {
      return this.args;
   }

   @Override
   public TranslatableComponent arguments(final ComponentLike... args) {
      return create(this.children, this.style, this.key, this.fallback, args);
   }

   @Override
   public TranslatableComponent arguments(final List<? extends ComponentLike> args) {
      return create(this.children, this.style, this.key, this.fallback, args);
   }

   @Override
   public TranslatableComponent fallback(@Nullable final String fallback) {
      return create(this.children, this.style, this.key, fallback, this.args);
   }

   public TranslatableComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.key, this.fallback, this.args);
   }

   public TranslatableComponent style(final Style style) {
      return create(this.children, style, this.key, this.fallback, this.args);
   }

   @Override
   public TranslatableComponent.Builder toBuilder() {
      return new TranslatableComponentImpl.BuilderImpl(this);
   }

   static List<TranslationArgument> asArguments(final List<? extends ComponentLike> likes) {
      if (likes.isEmpty()) {
         return List.of();
      } else {
         List<TranslationArgument> ret = new ArrayList<>(likes.size());

         for (int i = 0; i < likes.size(); i++) {
            ComponentLike like = likes.get(i);
            switch (like) {
               case null:
                  throw new NullPointerException("likes[" + i + "]");
               case TranslationArgument translationArgument:
                  ret.add(translationArgument);
                  break;
               case TranslationArgumentLike translationArgumentLike:
                  ret.add(Objects.requireNonNull(translationArgumentLike.asTranslationArgument(), "likes[" + i + "].asTranslationArgument()"));
                  break;
               default:
                  ret.add(TranslationArgument.component(like));
            }
         }

         return List.copyOf(ret);
      }
   }

   static final class BuilderImpl
      extends AbstractComponentBuilder<TranslatableComponent, TranslatableComponent.Builder>
      implements TranslatableComponent.Builder {
      @Nullable
      private String key;
      @Nullable
      private String fallback;
      private List<TranslationArgument> args = List.of();

      BuilderImpl() {
      }

      BuilderImpl(final TranslatableComponent component) {
         super(component);
         this.key = component.key();
         this.args = component.arguments();
         this.fallback = component.fallback();
      }

      @Override
      public TranslatableComponent.Builder key(final String key) {
         this.key = key;
         return this;
      }

      @Override
      public TranslatableComponent.Builder arguments(final ComponentLike... args) {
         Objects.requireNonNull(args, "args");
         return args.length == 0 ? this.arguments(List.of()) : this.arguments(List.of(args));
      }

      @Override
      public TranslatableComponent.Builder arguments(final List<? extends ComponentLike> args) {
         this.args = TranslatableComponentImpl.asArguments(Objects.requireNonNull(args, "args"));
         return this;
      }

      @Override
      public TranslatableComponent.Builder fallback(@Nullable final String fallback) {
         this.fallback = fallback;
         return this;
      }

      public TranslatableComponent build() {
         if (this.key == null) {
            throw new IllegalStateException("key must be set");
         } else {
            return TranslatableComponentImpl.create(this.children, this.buildStyle(), this.key, this.fallback, this.args);
         }
      }
   }
}

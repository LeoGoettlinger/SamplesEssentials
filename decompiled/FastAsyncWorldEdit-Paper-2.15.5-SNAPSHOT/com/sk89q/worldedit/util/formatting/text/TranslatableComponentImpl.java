package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class TranslatableComponentImpl extends AbstractComponent implements TranslatableComponent {
   private final String key;
   private final List<Component> args;

   TranslatableComponentImpl(@NonNull final List<Component> children, @NonNull final Style style, @NonNull final String key, @NonNull final Component[] args) {
      this(children, style, key, Arrays.asList(args));
   }

   TranslatableComponentImpl(
      @NonNull final List<Component> children, @NonNull final Style style, @NonNull final String key, @NonNull final List<? extends Component> args
   ) {
      super(children, style);
      this.key = key;
      this.args = unmodifiableCopy(args);
   }

   @NonNull
   @Override
   public String key() {
      return this.key;
   }

   @NonNull
   @Override
   public TranslatableComponent key(@NonNull final String key) {
      return Objects.equals(this.key, key) ? this : new TranslatableComponentImpl(this.children, this.style, Objects.requireNonNull(key, "key"), this.args);
   }

   @NonNull
   @Override
   public List<Component> args() {
      return this.args;
   }

   @NonNull
   @Override
   public TranslatableComponent args(@NonNull final Component... args) {
      return new TranslatableComponentImpl(this.children, this.style, this.key, args);
   }

   @NonNull
   @Override
   public TranslatableComponent args(@NonNull final List<? extends Component> args) {
      return new TranslatableComponentImpl(this.children, this.style, this.key, args);
   }

   @NonNull
   public TranslatableComponent children(@NonNull final List<Component> children) {
      return new TranslatableComponentImpl(children, this.style, this.key, this.args);
   }

   @NonNull
   public TranslatableComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new TranslatableComponentImpl(this.children, style, this.key, this.args);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof TranslatableComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         TranslatableComponent that = (TranslatableComponent)other;
         return Objects.equals(this.key, that.key()) && Objects.equals(this.args, that.args());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.key.hashCode();
      return 31 * result + this.args.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("key", this.key);
      builder.put("args", this.args);
   }

   @NonNull
   public TranslatableComponent.Builder toBuilder() {
      return new TranslatableComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl
      extends AbstractComponentBuilder<TranslatableComponent, TranslatableComponent.Builder>
      implements TranslatableComponent.Builder {
      @Nullable
      private String key;
      private List<? extends Component> args = AbstractComponent.EMPTY_COMPONENT_LIST;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final TranslatableComponent component) {
         super(component);
         this.key = component.key();
         this.args = component.args();
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder key(@NonNull final String key) {
         this.key = key;
         return this;
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder args(@NonNull final ComponentBuilder<?, ?> arg) {
         return this.args(Collections.singletonList(arg.build()));
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder args(final ComponentBuilder<?, ?>... args) {
         return args.length == 0
            ? this.args(AbstractComponent.EMPTY_COMPONENT_LIST)
            : this.args(Stream.of(args).map(ComponentBuilder::build).collect(Collectors.toList()));
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder args(@NonNull final Component arg) {
         return this.args(Collections.singletonList(arg));
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder args(final Component... args) {
         return args.length == 0 ? this.args(AbstractComponent.EMPTY_COMPONENT_LIST) : this.args(Arrays.asList(args));
      }

      @NonNull
      @Override
      public TranslatableComponent.Builder args(@NonNull final List<? extends Component> args) {
         this.args = args;
         return this;
      }

      @NonNull
      public TranslatableComponentImpl build() {
         if (this.key == null) {
            throw new IllegalStateException("key must be set");
         } else {
            return new TranslatableComponentImpl(this.children, this.buildStyle(), this.key, this.args);
         }
      }
   }
}

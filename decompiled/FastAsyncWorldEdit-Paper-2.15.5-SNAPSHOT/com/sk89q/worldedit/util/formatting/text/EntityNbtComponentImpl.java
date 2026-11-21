package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class EntityNbtComponentImpl extends NbtComponentImpl<EntityNbtComponent, EntityNbtComponent.Builder> implements EntityNbtComponent {
   private final String selector;

   EntityNbtComponentImpl(
      @NonNull final List<Component> children, @NonNull final Style style, final String nbtPath, final boolean interpret, final String selector
   ) {
      super(children, style, nbtPath, interpret);
      this.selector = selector;
   }

   @NonNull
   public EntityNbtComponent nbtPath(@NonNull final String nbtPath) {
      return Objects.equals(this.nbtPath, nbtPath) ? this : new EntityNbtComponentImpl(this.children, this.style, nbtPath, this.interpret, this.selector);
   }

   @NonNull
   public EntityNbtComponent interpret(final boolean interpret) {
      return this.interpret == interpret ? this : new EntityNbtComponentImpl(this.children, this.style, this.nbtPath, interpret, this.selector);
   }

   @NonNull
   @Override
   public String selector() {
      return this.selector;
   }

   @NonNull
   @Override
   public EntityNbtComponent selector(@NonNull final String selector) {
      return Objects.equals(this.selector, selector) ? this : new EntityNbtComponentImpl(this.children, this.style, this.nbtPath, this.interpret, selector);
   }

   @NonNull
   public EntityNbtComponent children(@NonNull final List<Component> children) {
      return new EntityNbtComponentImpl(children, this.style, this.nbtPath, this.interpret, this.selector);
   }

   @NonNull
   public EntityNbtComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new EntityNbtComponentImpl(this.children, style, this.nbtPath, this.interpret, this.selector);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof EntityNbtComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         EntityNbtComponentImpl that = (EntityNbtComponentImpl)other;
         return Objects.equals(this.selector, that.selector());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.selector.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      super.populateToString(builder);
      builder.put("selector", this.selector);
   }

   @NonNull
   public EntityNbtComponent.Builder toBuilder() {
      return new EntityNbtComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends NbtComponentImpl.BuilderImpl<EntityNbtComponent, EntityNbtComponent.Builder> implements EntityNbtComponent.Builder {
      @Nullable
      private String selector;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final EntityNbtComponent component) {
         super(component);
         this.selector = component.selector();
      }

      @NonNull
      @Override
      public EntityNbtComponent.Builder selector(@NonNull final String selector) {
         this.selector = selector;
         return this;
      }

      @NonNull
      public EntityNbtComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.selector == null) {
            throw new IllegalStateException("selector must be set");
         } else {
            return new EntityNbtComponentImpl(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.selector);
         }
      }
   }
}

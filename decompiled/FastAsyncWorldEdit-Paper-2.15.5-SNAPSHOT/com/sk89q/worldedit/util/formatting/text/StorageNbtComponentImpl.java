package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.kyori.Key;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class StorageNbtComponentImpl extends NbtComponentImpl<StorageNbtComponent, StorageNbtComponent.Builder> implements StorageNbtComponent {
   private final Key storage;

   protected StorageNbtComponentImpl(
      @NonNull final List<Component> children, @NonNull final Style style, final String nbtPath, final boolean interpret, final Key storage
   ) {
      super(children, style, nbtPath, interpret);
      this.storage = storage;
   }

   @NonNull
   public StorageNbtComponent nbtPath(@NonNull final String nbtPath) {
      return Objects.equals(this.nbtPath, nbtPath) ? this : new StorageNbtComponentImpl(this.children, this.style, nbtPath, this.interpret, this.storage);
   }

   @NonNull
   public StorageNbtComponent interpret(final boolean interpret) {
      return this.interpret == interpret ? this : new StorageNbtComponentImpl(this.children, this.style, this.nbtPath, interpret, this.storage);
   }

   @NonNull
   @Override
   public Key storage() {
      return this.storage;
   }

   @NonNull
   @Override
   public StorageNbtComponent storage(@NonNull final Key storage) {
      return Objects.equals(this.storage, storage) ? this : new StorageNbtComponentImpl(this.children, this.style, this.nbtPath, this.interpret, storage);
   }

   @NonNull
   public StorageNbtComponent children(@NonNull final List<Component> children) {
      return new StorageNbtComponentImpl(children, this.style, this.nbtPath, this.interpret, this.storage);
   }

   @NonNull
   public StorageNbtComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new StorageNbtComponentImpl(this.children, style, this.nbtPath, this.interpret, this.storage);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof StorageNbtComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         StorageNbtComponentImpl that = (StorageNbtComponentImpl)other;
         return Objects.equals(this.storage, that.storage());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.storage.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      super.populateToString(builder);
      builder.put("storage", this.storage);
   }

   @NonNull
   public StorageNbtComponent.Builder toBuilder() {
      return new StorageNbtComponentImpl.BuilderImpl(this);
   }

   static class BuilderImpl extends NbtComponentImpl.BuilderImpl<StorageNbtComponent, StorageNbtComponent.Builder> implements StorageNbtComponent.Builder {
      @MonotonicNonNull
      private Key storage;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final StorageNbtComponent component) {
         super(component);
         this.storage = component.storage();
      }

      @NonNull
      @Override
      public StorageNbtComponent.Builder storage(@NonNull final Key storage) {
         this.storage = storage;
         return this;
      }

      @NonNull
      public StorageNbtComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.storage == null) {
            throw new IllegalStateException("storage must be set");
         } else {
            return new StorageNbtComponentImpl(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.storage);
         }
      }
   }
}

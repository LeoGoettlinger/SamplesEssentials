package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.kyori.Key;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface StorageNbtComponent extends NbtComponent<StorageNbtComponent, StorageNbtComponent.Builder>, ScopedComponent<StorageNbtComponent> {
   @NonNull
   static StorageNbtComponent.Builder builder() {
      return new StorageNbtComponentImpl.BuilderImpl();
   }

   @NonNull
   static StorageNbtComponent of(@NonNull final String nbtPath, @NonNull final Key storage) {
      return builder().nbtPath(nbtPath).storage(storage).build();
   }

   @NonNull
   static StorageNbtComponent make(@NonNull final Consumer<? super StorageNbtComponent.Builder> consumer) {
      StorageNbtComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   Key storage();

   @NonNull
   StorageNbtComponent storage(@NonNull final Key storage);

   public interface Builder extends NbtComponentBuilder<StorageNbtComponent, StorageNbtComponent.Builder> {
      @NonNull
      StorageNbtComponent.Builder storage(@NonNull final Key storage);
   }
}

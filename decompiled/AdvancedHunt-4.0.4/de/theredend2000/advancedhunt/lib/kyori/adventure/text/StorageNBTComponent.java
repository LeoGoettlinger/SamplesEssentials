package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;

public sealed interface StorageNBTComponent extends NBTComponent<StorageNBTComponent>, ScopedComponent<StorageNBTComponent> permits StorageNBTComponentImpl {
   Key storage();

   @Contract(
      pure = true
   )
   StorageNBTComponent storage(final Key storage);

   StorageNBTComponent.Builder toBuilder();

   public sealed interface Builder extends NBTComponentBuilder<StorageNBTComponent, StorageNBTComponent.Builder> permits StorageNBTComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      StorageNBTComponent.Builder storage(final Key storage);
   }
}

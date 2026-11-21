package com.Sw3ys.bigenderchest.storage;

import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;

public interface ChestStore {
   void prepare();

   boolean exists(UUID var1);

   ChestStore.Stored read(UUID var1);

   void write(UUID var1, int var2, boolean var3, ItemStack[] var4);

   int getSavedSize(UUID var1);

   default boolean isAsync() {
      return false;
   }

   default boolean isAuthoritative() {
      return false;
   }

   default void loadAsync(UUID uuid, Consumer<Boolean> done) {
      done.accept(this.exists(uuid));
   }

   default void unload(UUID uuid) {
   }

   default boolean importWrite(UUID uuid, int size, ItemStack[] items) {
      this.write(uuid, size, false, items);
      return true;
   }

   default void close() {
   }

   public static final class Stored {
      public final int size;
      public final boolean pending;
      public final ItemStack[] items;

      public Stored(int size, boolean pending, ItemStack[] items) {
         this.size = size;
         this.pending = pending;
         this.items = items;
      }
   }
}

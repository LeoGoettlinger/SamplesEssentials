package com.sk89q.worldedit.registry;

import com.fastasyncworldedit.core.registry.RegistryItem;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public abstract class Category<T extends Keyed> implements RegistryItem, Keyed {
   private final Set<T> set = new HashSet<>();
   private final Supplier<Set<T>> supplier;
   protected final String id;
   private boolean empty = true;
   private int internalId;

   public Category(final String id) {
      this.id = id;
      this.supplier = null;
   }

   public Category(String id, Supplier<Set<T>> contentSupplier) {
      this.id = id;
      this.supplier = contentSupplier;
   }

   @Override
   public final String id() {
      return this.id;
   }

   public final Set<T> getAll() {
      if (this.empty) {
         if (this.supplier != null) {
            this.set.addAll(this.supplier.get());
         } else {
            this.set.addAll(this.load());
         }

         this.empty = false;
      }

      return this.set;
   }

   @Override
   public void setInternalId(int internalId) {
      this.internalId = internalId;
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   @Deprecated
   protected abstract Set<T> load();

   public boolean contains(final T object) {
      return this.getAll().contains(object);
   }

   public void invalidateCache() {
      this.set.clear();
      this.empty = true;
   }

   @Override
   public String toString() {
      return this.id();
   }
}

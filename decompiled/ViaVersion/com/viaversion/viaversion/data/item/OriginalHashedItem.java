package com.viaversion.viaversion.data.item;

import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.HashedStructuredItem;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;

public final class OriginalHashedItem extends HashedStructuredItem {
   private final String backupTagName;

   public OriginalHashedItem(HashedItem item, String backupTagName) {
      this(item.identifier(), item.amount(), item.dataHashesById(), item.removedDataIds(), backupTagName);
   }

   public OriginalHashedItem(int identifier, int amount, String backupTagName) {
      super(identifier, amount);
      this.backupTagName = backupTagName;
   }

   private OriginalHashedItem(int identifier, int amount, Int2IntMap dataHashes, IntSet removedData, String backupTagName) {
      super(identifier, amount, dataHashes, removedData);
      this.backupTagName = backupTagName;
   }

   public String backupTagName() {
      return this.backupTagName;
   }

   public HashedItem asRegularItem() {
      return new HashedStructuredItem(this.identifier(), this.amount(), this.dataHashesById(), this.removedDataIds());
   }

   public OriginalHashedItem copy() {
      return new OriginalHashedItem(
         this.identifier(), this.amount(), new Int2IntOpenHashMap(this.dataHashesById()), new IntOpenHashSet(this.removedDataIds()), this.backupTagName
      );
   }
}

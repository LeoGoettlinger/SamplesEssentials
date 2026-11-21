package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;

public class HashedStructuredItem implements HashedItem {
   private final Int2IntMap dataHashes;
   private final IntSet removedData;
   private int identifier;
   private int amount;

   public HashedStructuredItem(int identifier, int amount) {
      this(identifier, amount, new Int2IntOpenHashMap(0), new IntOpenHashSet(0));
   }

   public HashedStructuredItem(int identifier, int amount, Int2IntMap dataHashes, IntSet removedData) {
      this.identifier = identifier;
      this.amount = amount;
      this.dataHashes = dataHashes;
      this.removedData = removedData;
   }

   public static HashedStructuredItem empty() {
      return new HashedStructuredItem(0, 0);
   }

   @Override
   public int identifier() {
      return this.identifier;
   }

   @Override
   public void setIdentifier(int identifier) {
      this.identifier = identifier;
   }

   @Override
   public int amount() {
      return this.amount;
   }

   @Override
   public void setAmount(int amount) {
      this.amount = amount;
   }

   @Override
   public Int2IntMap dataHashesById() {
      return this.dataHashes;
   }

   @Override
   public IntSet removedDataIds() {
      return this.removedData;
   }

   @Override
   public HashedItem copy() {
      return new HashedStructuredItem(this.identifier, this.amount, new Int2IntOpenHashMap(this.dataHashes), new IntOpenHashSet(this.removedData));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         HashedStructuredItem that = (HashedStructuredItem)o;
         if (this.identifier != that.identifier) {
            return false;
         } else {
            return this.amount != that.amount ? false : this.dataHashes.equals(that.dataHashes) && this.removedData.equals(that.removedData);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.dataHashes.hashCode();
      result = 31 * result + this.removedData.hashCode();
      result = 31 * result + this.identifier;
      return 31 * result + this.amount;
   }

   @Override
   public String toString() {
      return "HashedStructuredItem{data=Hashes"
         + this.dataHashes
         + ", removedData="
         + this.removedData
         + ", identifier="
         + this.identifier
         + ", amount="
         + this.amount
         + "}";
   }
}

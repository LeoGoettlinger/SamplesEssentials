package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.libs.gson.annotations.SerializedName;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DataItem implements Item {
   @SerializedName(
      value = "identifier",
      alternate = {"id"}
   )
   private int identifier;
   private byte amount;
   private short data;
   private CompoundTag tag;

   public DataItem() {
   }

   public DataItem(int identifier, byte amount, @Nullable CompoundTag tag) {
      this(identifier, amount, (short)0, tag);
   }

   public DataItem(int identifier, byte amount, short data, @Nullable CompoundTag tag) {
      this.identifier = identifier;
      this.amount = amount;
      this.data = data;
      this.tag = tag;
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
      if (amount != (byte)amount) {
         throw new IllegalArgumentException("Invalid item amount: " + amount);
      } else {
         this.amount = (byte)amount;
      }
   }

   @Override
   public short data() {
      return this.data;
   }

   @Override
   public void setData(short data) {
      this.data = data;
   }

   @Nullable
   @Override
   public CompoundTag tag() {
      return this.tag;
   }

   @Override
   public void setTag(@Nullable CompoundTag tag) {
      this.tag = tag;
   }

   @Override
   public StructuredDataContainer dataContainer() {
      throw new UnsupportedOperationException();
   }

   public DataItem copy() {
      return new DataItem(this.identifier, this.amount, this.data, this.tag != null ? this.tag.copy() : null);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DataItem item = (DataItem)o;
         if (this.identifier != item.identifier) {
            return false;
         } else if (this.amount != item.amount) {
            return false;
         } else {
            return this.data != item.data ? false : Objects.equals(this.tag, item.tag);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.identifier;
      result = 31 * result + this.amount;
      result = 31 * result + this.data;
      return 31 * result + (this.tag != null ? this.tag.hashCode() : 0);
   }

   @Override
   public String toString() {
      return "DataItem{identifier=" + this.identifier + ", amount=" + this.amount + ", data=" + this.data + ", tag=" + this.tag + "}";
   }
}

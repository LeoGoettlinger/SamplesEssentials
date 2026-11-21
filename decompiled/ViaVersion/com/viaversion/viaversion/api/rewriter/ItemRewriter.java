package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.HashedItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ItemRewriter<T extends Protocol<?, ?, ?, ?>> extends Rewriter<T> {
   @Nullable
   Item handleItemToClient(UserConnection var1, @Nullable Item var2);

   @Nullable
   Item handleItemToServer(UserConnection var1, @Nullable Item var2);

   HashedItem handleHashedItem(UserConnection var1, HashedItem var2);

   @Nullable
   default Type<Item> itemType() {
      return null;
   }

   @Nullable
   default Type<Item[]> itemArrayType() {
      return null;
   }

   @Nullable
   default Type<Item> mappedItemType() {
      return this.itemType();
   }

   @Nullable
   default Type<Item[]> mappedItemArrayType() {
      return this.itemArrayType();
   }

   @Nullable
   default Type<Item> itemTemplateType() {
      return this.itemType();
   }

   @Nullable
   default Type<Item> mappedItemTemplateType() {
      return this.mappedItemType();
   }

   default String nbtTagName() {
      return "VV|" + this.protocol().getClass().getSimpleName();
   }

   default String nbtTagName(String nbt) {
      return this.nbtTagName() + "|" + nbt;
   }
}

package com.artillexstudios.axafkzone.libs.axapi.items;

import com.artillexstudios.axafkzone.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.Wrapper;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.WrapperRegistry;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;

public interface WrappedItemStack extends Wrapper<ItemStack> {
   static WrappedItemStack wrap(Object data) {
      return WrapperRegistry.ITEM_STACK.map(data);
   }

   static <T> T edit(ItemStack itemStack, Function<WrappedItemStack, T> function) {
      WrappedItemStack wrapped = wrap(itemStack);
      T result = function.apply(wrapped);
      wrapped.finishEdit();
      return result;
   }

   <T> void set(DataComponent<T> component, T value);

   default <T> T get(DataComponent<T> component) {
      return component == null ? null : component.getData(this);
   }

   void setAmount(int amount);

   int getAmount();

   ItemStack toBukkit();

   HashedStack toHashedStack(HashGenerator generator);

   String toSNBT();

   byte[] serialize();

   void finishEdit();

   WrappedItemStack copy();

   void editFrom(WrappedItemStack stack);

   public static enum CodecData {
      OPTIONAL_STREAM_CODEC,
      OPTIONAL_UNTRUSTED_STREAM_CODEC,
      STREAM_CODEC;
   }
}

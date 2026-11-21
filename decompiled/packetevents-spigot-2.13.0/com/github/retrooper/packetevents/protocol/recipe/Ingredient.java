package com.github.retrooper.packetevents.protocol.recipe;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public class Ingredient {
   private final ItemStack[] options;

   public Ingredient(ItemStack... options) {
      this.options = options;
   }

   public static Ingredient read(PacketWrapper<?> wrapper) {
      ItemStack[] options = wrapper.readArray(PacketWrapper::readItemStack, ItemStack.class);
      return new Ingredient(options);
   }

   public static void write(PacketWrapper<?> wrapper, Ingredient ingredient) {
      wrapper.writeArray(ingredient.options, PacketWrapper::writeItemStack);
   }

   public ItemStack[] getOptions() {
      return this.options;
   }
}

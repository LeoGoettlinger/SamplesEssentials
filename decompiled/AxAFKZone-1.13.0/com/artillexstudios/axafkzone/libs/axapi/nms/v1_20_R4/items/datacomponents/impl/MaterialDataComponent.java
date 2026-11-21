package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R4.items.datacomponents.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftMagicNumbers;

public record MaterialDataComponent() implements com.artillexstudios.axafkzone.libs.axapi.items.components.DataComponent<Material> {
   public void apply(WrappedItemStack stack, Material data) {
      ItemStack wrapped = ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R4.items.WrappedItemStack)stack).itemStack;
      wrapped.setItem(CraftMagicNumbers.getItem(data));
   }

   public Material getData(WrappedItemStack stack) {
      ItemStack wrapped = ((com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R4.items.WrappedItemStack)stack).itemStack;
      return CraftMagicNumbers.getMaterial(wrapped.g());
   }

   @Override
   public String getId() {
      return "material";
   }
}

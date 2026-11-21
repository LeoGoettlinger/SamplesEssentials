package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R1.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftMagicNumbers;

public record MaterialDataComponent() implements com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent<Material> {
   public void apply(WrappedItemStack stack, Material data) {
      ItemStack wrapped = ((com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R1.items.WrappedItemStack)stack).itemStack;
      wrapped.setItem(CraftMagicNumbers.getItem(data));
   }

   public Material getData(WrappedItemStack stack) {
      ItemStack wrapped = ((com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R1.items.WrappedItemStack)stack).itemStack;
      return CraftMagicNumbers.getMaterial(wrapped.g());
   }

   @Override
   public String getId() {
      return "material";
   }
}

package com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.entity;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.serializer.Accessors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemEntityMeta extends EntityMeta {
   public ItemEntityMeta(Metadata metadata) {
      super(metadata);
   }

   public void itemStack(WrappedItemStack itemStack) {
      this.metadata.set(Accessors.ITEM_SLOT, itemStack);
   }

   public WrappedItemStack itemStack() {
      return this.metadata.get(Accessors.ITEM_SLOT);
   }

   @Override
   protected void defineDefaults() {
      this.metadata.define(Accessors.ITEM_SLOT, WrappedItemStack.wrap(new ItemStack(Material.AIR)));
   }
}

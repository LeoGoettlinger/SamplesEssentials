package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.utils.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.item.ItemStack;

public final class NameDataComponent implements DataComponentHandler<Component> {
   public void setData(ItemStack from, Component data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      this.setDisplayTag(tag, "Name", NBTTagString.a(ComponentSerializer.INSTANCE.toGson(data)));
   }

   public Component getData(ItemStack stack) {
      NBTTagCompound tag = this.getDisplayTag(stack);
      if (tag == null) {
         return null;
      } else {
         return !tag.e("Name") ? null : ComponentSerializer.INSTANCE.fromGson(tag.l("Name"));
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getDisplayTag(itemStack);
      if (tag != null) {
         this.setDisplayTag(tag, "Name", null);
      }
   }
}

package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.items.components.data.ItemLore;
import com.artillexstudios.axtrade.libs.axapi.utils.ComponentSerializer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.item.ItemStack;

public final class LoreDataComponent implements DataComponentHandler<ItemLore> {
   public void setData(ItemStack from, ItemLore data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      NBTTagList listTag = new NBTTagList();
      List<String> jsonLore = ComponentSerializer.INSTANCE.toGsonList(data.lines());

      for (int i = 0; i < jsonLore.size(); i++) {
         listTag.add(NBTTagString.a(jsonLore.get(i)));
      }

      this.setDisplayTag(tag, "Lore", listTag);
   }

   public ItemLore getData(ItemStack stack) {
      NBTTagCompound tag = this.getDisplayTag(stack);
      if (tag == null) {
         return ItemLore.create(List.of());
      } else if (!tag.e("Lore")) {
         return ItemLore.create(List.of());
      } else {
         NBTTagList list = tag.c("Lore", 8);
         ArrayList<String> lore = new ArrayList<>(list.size());

         for (int index = 0; index < list.size(); index++) {
            String line = list.j(index);
            lore.add(line);
         }

         return ItemLore.create(ComponentSerializer.INSTANCE.fromGsonList(lore));
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getDisplayTag(itemStack);
      if (tag != null) {
         this.setDisplayTag(tag, "Lore", null);
      }
   }
}

package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R2.items.nbt;

import com.artillexstudios.axrankmenu.libs.axapi.items.nbt.Tag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ListTag implements com.artillexstudios.axrankmenu.libs.axapi.items.nbt.ListTag {
   private final NBTTagList parent;

   public ListTag(NBTTagList parent) {
      this.parent = parent;
   }

   @Override
   public void add(Tag tag) {
      this.parent.add(tag.getParent() instanceof NBTTagCompound compoundTag ? compoundTag : (NBTTagList)tag.getParent());
   }

   @Override
   public void remove(Tag tag) {
      this.parent.remove(tag.getParent() instanceof NBTTagCompound compoundTag ? compoundTag : tag.getParent());
   }

   @Override
   public boolean contains(Tag tag) {
      return this.parent.contains(tag.getParent() instanceof NBTTagCompound compoundTag ? compoundTag : tag.getParent());
   }

   @Override
   public Object getParent() {
      return this.parent;
   }
}

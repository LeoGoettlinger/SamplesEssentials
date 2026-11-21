package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axrankmenu.libs.axapi.utils.GameProfile;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ResolvableProfile;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.item.ItemStack;

public final class ProfileDataComponent implements DataComponentHandler<ResolvableProfile> {
   public void setData(ItemStack from, ResolvableProfile data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      NBTTagCompound skullOwner = new NBTTagCompound();
      skullOwner.a("Name", "skull");
      NBTTagCompound properties = new NBTTagCompound();
      NBTTagList textures = new NBTTagList();
      NBTTagCompound val = new NBTTagCompound();
      val.a("Value", ((ProfileProperties.Property)data.getPartialProfile().properties().properties().get("textures").stream().findFirst().get()).value());
      textures.add(val);
      properties.a("textures", textures);
      skullOwner.a("Properties", properties);
      tag.a("SkullOwner", skullOwner);
   }

   public ResolvableProfile getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      if (tag != null && tag.e("SkullOwner")) {
         ProfileProperties profileProperties = new ProfileProperties(UUID.randomUUID(), "skull");
         NBTTagCompound skullOwner = tag.p("SkullOwner");
         NBTTagCompound propertiesTag = skullOwner.p("Properties");
         NBTTagList listTag = propertiesTag.c("textures", 10);
         String textures = "";
         Iterator var8 = listTag.iterator();
         if (var8.hasNext()) {
            NBTBase tag1 = (NBTBase)var8.next();
            NBTTagCompound compoundTag = (NBTTagCompound)tag1;
            textures = compoundTag.l("Value");
         }

         profileProperties.put("textures", new ProfileProperties.Property("textures", textures, null));
         return new ResolvableProfile(new GameProfile(null, null, profileProperties), null);
      } else {
         return null;
      }
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null && tag.e("SkullOwner")) {
         tag.r("SkullOwner");
      }
   }
}

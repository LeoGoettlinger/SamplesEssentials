package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R1.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.items.components.data.PotionContents;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.potion.PotionData;

public final class PotionContentsDataComponent implements DataComponentHandler<PotionContents> {
   public void setData(ItemStack from, PotionContents data) {
      NBTTagCompound tag = this.getOrCreateTag(from);
      tag.a("Potion", CraftPotionUtil.fromBukkit(new PotionData(data.type().orElseThrow())));
   }

   public PotionContents getData(ItemStack stack) {
      NBTTagCompound tag = this.getTag(stack);
      return tag == null
         ? null
         : new PotionContents(Optional.ofNullable(CraftPotionUtil.toBukkit(tag.l("Potion")).getType()), Optional.empty(), List.of(), Optional.empty());
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null) {
         tag.r("Potion");
      }
   }
}

package com.artillexstudios.axafkzone.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.inventory.ItemFlag;

public final class EnchantmentGlintOverrideDataComponent implements DataComponentHandler<Boolean> {
   public void setData(ItemStack from, Boolean data) {
      Map<Enchantment, Integer> enchantments = Map.of(Enchantments.E, 1);
      EnchantmentManager.a(enchantments, from);
      addItemFlags(this.getOrCreateTag(from), ItemFlag.HIDE_ENCHANTS);
   }

   private static void addItemFlags(NBTTagCompound tag, ItemFlag... itemFlags) {
      byte flag = tag.b("HideFlags", 99) ? (byte)tag.h("HideFlags") : 0;

      for (ItemFlag itemFlag : itemFlags) {
         flag |= getBitModifier(itemFlag);
      }

      tag.a("HideFlags", flag);
   }

   private static byte getBitModifier(ItemFlag hideFlag) {
      return (byte)(1 << hideFlag.ordinal());
   }

   public Boolean getData(ItemStack stack) {
      return null;
   }

   @Override
   public void removeData(ItemStack itemStack) {
   }
}

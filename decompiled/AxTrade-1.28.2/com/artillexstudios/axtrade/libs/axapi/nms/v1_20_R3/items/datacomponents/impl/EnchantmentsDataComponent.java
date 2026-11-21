package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R3.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.items.components.data.ItemEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import org.bukkit.craftbukkit.v1_20_R3.enchantments.CraftEnchantment;

public final class EnchantmentsDataComponent implements DataComponentHandler<ItemEnchantments> {
   private final String tagKey;

   public EnchantmentsDataComponent(String tagKey) {
      this.tagKey = tagKey;
   }

   public void setData(ItemStack from, ItemEnchantments data) {
      HashMap<Enchantment, Integer> enchantments = new HashMap<>();
      ObjectIterator var4 = data.enchantments().object2IntEntrySet().iterator();

      while (var4.hasNext()) {
         Entry<org.bukkit.enchantments.Enchantment> entry = (Entry<org.bukkit.enchantments.Enchantment>)var4.next();
         enchantments.put(CraftEnchantment.bukkitToMinecraft((org.bukkit.enchantments.Enchantment)entry.getKey()), entry.getIntValue());
      }

      EnchantmentManager.a(enchantments, from);
   }

   public ItemEnchantments getData(ItemStack stack) {
      Map<Enchantment, Integer> enchants = EnchantmentManager.a(stack);
      Object2IntMap<org.bukkit.enchantments.Enchantment> enchantments = new Object2IntAVLTreeMap();

      for (java.util.Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
         enchantments.put(CraftEnchantment.minecraftToBukkit(entry.getKey()), entry.getValue());
      }

      return new ItemEnchantments(enchantments);
   }

   @Override
   public void removeData(ItemStack itemStack) {
      NBTTagCompound tag = this.getTag(itemStack);
      if (tag != null) {
         tag.r(this.tagKey);
      }
   }
}

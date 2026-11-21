package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.components.data.ItemEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.ItemEnchantments.a;
import org.bukkit.craftbukkit.v1_20_R4.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;

public final class EnchantmentsDataComponent implements DataComponentHandler<ItemEnchantments, net.minecraft.world.item.enchantment.ItemEnchantments> {
   public net.minecraft.world.item.enchantment.ItemEnchantments toNMS(ItemEnchantments from) {
      a enchantments = new a(net.minecraft.world.item.enchantment.ItemEnchantments.a);
      ObjectIterator var3 = from.enchantments().object2IntEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<Enchantment> entry = (Entry<Enchantment>)var3.next();
         enchantments.a(CraftEnchantment.bukkitToMinecraft((Enchantment)entry.getKey()), entry.getIntValue());
      }

      return enchantments.b();
   }

   public ItemEnchantments fromNMS(net.minecraft.world.item.enchantment.ItemEnchantments data) {
      Object2IntArrayMap<Enchantment> enchantments = new Object2IntArrayMap();

      for (Entry<Holder<net.minecraft.world.item.enchantment.Enchantment>> entry : data.b()) {
         enchantments.put(CraftEnchantment.minecraftHolderToBukkit((Holder)entry.getKey()), entry.getIntValue());
      }

      return new ItemEnchantments(enchantments);
   }
}

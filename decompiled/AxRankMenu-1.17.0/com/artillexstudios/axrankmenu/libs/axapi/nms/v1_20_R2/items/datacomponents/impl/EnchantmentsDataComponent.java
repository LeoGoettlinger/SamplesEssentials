package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R2.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.components.data.ItemEnchantments;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;

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
         enchantments.put(CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment)entry.getKey()), entry.getIntValue());
      }

      EnchantmentManager.a(enchantments, from);
   }

   public ItemEnchantments getData(ItemStack stack) {
      Map<Enchantment, Integer> enchants = EnchantmentManager.a(stack);
      Object2IntMap<org.bukkit.enchantments.Enchantment> enchantments = new Object2IntAVLTreeMap();

      for (java.util.Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
         enchantments.put(minecraftToBukkit(entry.getKey()), entry.getValue());
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

   private static org.bukkit.enchantments.Enchantment minecraftToBukkit(Enchantment minecraft) {
      Preconditions.checkArgument(minecraft != null);
      IRegistry<Enchantment> registry = BuiltInRegistries.g;
      org.bukkit.enchantments.Enchantment bukkit = (org.bukkit.enchantments.Enchantment)Registry.ENCHANTMENT
         .get(CraftNamespacedKey.fromMinecraft(((ResourceKey)registry.c(minecraft).orElseThrow()).a()));
      Preconditions.checkArgument(bukkit != null);
      return bukkit;
   }
}

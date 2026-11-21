package me.byteful.plugin.leveltools.api.item.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.libs.nbtapi.NBTCompoundList;
import me.byteful.plugin.leveltools.libs.nbtapi.NBTItem;
import me.byteful.plugin.leveltools.libs.nbtapi.NBTListCompound;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NBTLevelToolsItem implements LevelToolsItem {
   @NotNull
   public static final String LEVEL_KEY = "levelToolsLevel";
   @NotNull
   public static final String XP_KEY = "levelToolsXp";
   @NotNull
   public static final String LAST_REWARD_KEY = "levelToolsReward";
   @NotNull
   private NBTItem nbt;
   @NotNull
   private Map<Enchantment, Integer> enchantments;
   @NotNull
   private Map<String, Double> attributes;

   public NBTLevelToolsItem(@NotNull ItemStack stack) {
      this.nbt = new NBTItem(stack);
      this.enchantments = new HashMap<>();
      this.attributes = new HashMap<>();
   }

   @NotNull
   @Override
   public ItemStack getItemStack() {
      ItemStack stack = LevelToolsUtil.buildItemStack(this.nbt.getItem().clone(), this.enchantments, this.getLevel(), this.getXp(), this.getMaxXp());
      this.nbt = new NBTItem(stack);
      NBTCompoundList attr = this.nbt.getCompoundList("AttributeModifiers");
      this.attributes.forEach((attribute, modifier) -> {
         NBTListCompound list = attr.addCompound();
         list.setDouble("Amount", modifier);
         list.setString("AttributeName", attribute);
         list.setString("Name", attribute);
         list.setInteger("Operation", 0);
         list.setInteger("UUIDLeast", 59664);
         list.setInteger("UUIDMost", 31453);
      });
      return this.nbt.getItem();
   }

   @Override
   public int getLevel() {
      if (!this.nbt.hasKey("levelToolsLevel")) {
         this.setLevel(0);
      }

      return this.nbt.getInteger("levelToolsLevel");
   }

   @Override
   public void setLevel(int level) {
      if (level < 0) {
         this.setLevel0(0);
      } else {
         this.setLevel0(level);
      }
   }

   @Override
   public double getXp() {
      if (!this.nbt.hasTag("levelToolsXp")) {
         this.setXp(0.0);
      }

      return this.nbt.getDouble("levelToolsXp");
   }

   @Override
   public void setXp(double xp) {
      if (xp < 0.0) {
         this.setXp0(0.0);
      } else {
         this.setXp0(xp);
      }
   }

   @Override
   public int getLastHandledReward() {
      if (!this.nbt.hasTag("levelToolsReward")) {
         this.setLastHandledReward(-1);
      }

      return this.nbt.getInteger("levelToolsReward");
   }

   @Override
   public void setLastHandledReward(int rewardKey) {
      this.nbt.setInteger("levelToolsReward", rewardKey);
   }

   private void setLevel0(int level) {
      this.nbt.setInteger("levelToolsLevel", level);
   }

   private void setXp0(double xp) {
      this.nbt.setDouble("levelToolsXp", xp);
   }

   @Override
   public void enchant(Enchantment enchantment, int level) {
      this.enchantments.put(enchantment, level);
   }

   @Override
   public void modifyAttribute(String attribute, double modifier) {
      this.attributes.put(attribute, modifier);
   }

   @NotNull
   public NBTItem getNBT() {
      return this.nbt;
   }

   public void setNBT(@NotNull NBTItem nbt) {
      this.nbt = nbt;
   }

   @NotNull
   public Map<Enchantment, Integer> getEnchantments() {
      return this.enchantments;
   }

   public void setEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
      this.enchantments = enchantments;
   }

   @NotNull
   public Map<String, Double> getAttributes() {
      return this.attributes;
   }

   public void setAttributes(@NotNull Map<String, Double> attributes) {
      this.attributes = attributes;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NBTLevelToolsItem that = (NBTLevelToolsItem)o;
         return this.nbt.equals(that.nbt) && this.enchantments.equals(that.enchantments) && this.attributes.equals(that.attributes);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.nbt, this.enchantments, this.attributes);
   }

   @Override
   public String toString() {
      return "NBTLevelToolsItem{nbt=" + this.nbt + ", enchantments=" + this.enchantments + ", attributes=" + this.attributes + '}';
   }
}

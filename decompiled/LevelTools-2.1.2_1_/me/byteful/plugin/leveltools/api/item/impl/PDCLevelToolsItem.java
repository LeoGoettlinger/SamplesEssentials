package me.byteful.plugin.leveltools.api.item.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PDCLevelToolsItem implements LevelToolsItem {
   @NotNull
   public static final NamespacedKey LEVEL_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsLevel");
   @NotNull
   public static final NamespacedKey XP_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsXp");
   @NotNull
   public static final NamespacedKey LAST_REWARD_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsReward");
   @NotNull
   private ItemStack stack;
   @NotNull
   private Map<Enchantment, Integer> enchantments;
   @NotNull
   private Map<String, Double> attributes;

   public PDCLevelToolsItem(@NotNull ItemStack stack) {
      this.stack = stack;
      this.enchantments = new HashMap<>();
      this.attributes = new HashMap<>();
   }

   @NotNull
   @Override
   public ItemStack getItemStack() {
      ItemStack stack = LevelToolsUtil.buildItemStack(this.stack, this.enchantments, this.getLevel(), this.getXp(), this.getMaxXp());
      ItemMeta meta = stack.getItemMeta();

      assert meta != null : "ItemMeta is null! Should not happen.";

      this.attributes.forEach((attribute, modifier) -> {
         Attribute attr = Attribute.valueOf(attribute.replace(".", "_").toUpperCase(Locale.ROOT).trim());
         AttributeModifier mod = new AttributeModifier(attribute, modifier, Operation.ADD_NUMBER);
         meta.addAttributeModifier(attr, mod);
      });
      stack.setItemMeta(meta);
      return stack;
   }

   @Override
   public int getLevel() {
      PersistentDataContainer pdc = this.getItemPDC().getPersistentDataContainer();
      Integer value = (Integer)pdc.get(LEVEL_KEY, PersistentDataType.INTEGER);
      if (value == null) {
         this.setLevel(0);
         value = 0;
      }

      return value;
   }

   @Override
   public void setLevel(int level) {
      PersistentDataHolder holder = this.getItemPDC();
      PersistentDataContainer pdc = holder.getPersistentDataContainer();
      pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, Math.max(level, 0));
      this.stack.setItemMeta((ItemMeta)holder);
   }

   @Override
   public double getXp() {
      PersistentDataContainer pdc = this.getItemPDC().getPersistentDataContainer();
      Double value = (Double)pdc.get(XP_KEY, PersistentDataType.DOUBLE);
      if (value == null) {
         this.setXp(0.0);
         value = 0.0;
      }

      return value;
   }

   @Override
   public void setXp(double xp) {
      PersistentDataHolder holder = this.getItemPDC();
      PersistentDataContainer pdc = holder.getPersistentDataContainer();
      pdc.set(XP_KEY, PersistentDataType.DOUBLE, Math.max(xp, 0.0));
      this.stack.setItemMeta((ItemMeta)holder);
   }

   @Override
   public int getLastHandledReward() {
      PersistentDataContainer pdc = this.getItemPDC().getPersistentDataContainer();
      Integer value = (Integer)pdc.get(LAST_REWARD_KEY, PersistentDataType.INTEGER);
      if (value == null) {
         this.setLastHandledReward(-1);
         value = -1;
      }

      return value;
   }

   @Override
   public void setLastHandledReward(int rewardKey) {
      PersistentDataHolder holder = this.getItemPDC();
      PersistentDataContainer pdc = holder.getPersistentDataContainer();
      pdc.set(LAST_REWARD_KEY, PersistentDataType.INTEGER, rewardKey);
      this.stack.setItemMeta((ItemMeta)holder);
   }

   @Override
   public void enchant(Enchantment enchantment, int level) {
      this.enchantments.put(enchantment, level);
   }

   @Override
   public void modifyAttribute(String attribute, double modifier) {
      this.attributes.put(attribute, modifier);
   }

   private PersistentDataHolder getItemPDC() {
      return this.stack.getItemMeta();
   }

   @NotNull
   public ItemStack getStack() {
      return this.stack;
   }

   public void setStack(@NotNull ItemStack stack) {
      this.stack = stack;
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
         PDCLevelToolsItem that = (PDCLevelToolsItem)o;
         return this.stack.equals(that.stack) && this.enchantments.equals(that.enchantments) && this.attributes.equals(that.attributes);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.stack, this.enchantments, this.attributes);
   }

   @Override
   public String toString() {
      return "PDCLevelToolsItem{stack=" + this.stack + ", enchantments=" + this.enchantments + ", attributes=" + this.attributes + '}';
   }
}

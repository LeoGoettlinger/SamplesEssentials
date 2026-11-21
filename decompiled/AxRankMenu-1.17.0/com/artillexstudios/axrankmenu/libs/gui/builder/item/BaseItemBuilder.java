package com.artillexstudios.axrankmenu.libs.gui.builder.item;

import com.artillexstudios.axrankmenu.libs.gui.components.GuiAction;
import com.artillexstudios.axrankmenu.libs.gui.components.util.ItemNbt;
import com.artillexstudios.axrankmenu.libs.gui.components.util.VersionHelper;
import com.artillexstudios.axrankmenu.libs.gui.guis.GuiItem;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseItemBuilder<B extends BaseItemBuilder<B>> {
   private static final EnumSet<Material> LEATHER_ARMOR = EnumSet.of(
      Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
   );
   private ItemStack itemStack;
   private ItemMeta meta;
   private final NameLoreHandler nameLoreHandler;

   protected BaseItemBuilder(@NotNull ItemStack itemStack, @NotNull NameLoreHandler nameLoreHandler) {
      Preconditions.checkNotNull(itemStack, "Item can't be null!");
      this.itemStack = itemStack;
      this.nameLoreHandler = nameLoreHandler;
      this.meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
   }

   @NotNull
   @Contract("_ -> this")
   public B name(@NotNull Component name) {
      this.nameLoreHandler.name(this.meta, name);
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B amount(int amount) {
      this.itemStack.setAmount(amount);
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B lore(@Nullable @NotNull Component... lore) {
      return this.lore(Arrays.asList(lore));
   }

   @NotNull
   @Contract("_ -> this")
   public B lore(@NotNull List<Component> lore) {
      this.nameLoreHandler.lore(this.meta, lore);
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B lore(@NotNull Consumer<List<Component>> lore) {
      this.nameLoreHandler.lore(this.meta, lore);
      return (B)this;
   }

   @NotNull
   @Contract("_, _, _ -> this")
   public B enchant(@NotNull Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
      this.meta.addEnchant(enchantment, level, ignoreLevelRestriction);
      return (B)this;
   }

   @NotNull
   @Contract("_, _ -> this")
   public B enchant(@NotNull Enchantment enchantment, int level) {
      return this.enchant(enchantment, level, true);
   }

   @NotNull
   @Contract("_ -> this")
   public B enchant(@NotNull Enchantment enchantment) {
      return this.enchant(enchantment, 1, true);
   }

   @NotNull
   @Contract("_, _ -> this")
   public B enchant(@NotNull Map<Enchantment, Integer> enchantments, boolean ignoreLevelRestriction) {
      enchantments.forEach((enchantment, level) -> this.enchant(enchantment, level, ignoreLevelRestriction));
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B enchant(@NotNull Map<Enchantment, Integer> enchantments) {
      return this.enchant(enchantments, true);
   }

   @NotNull
   @Contract("_ -> this")
   public B disenchant(@NotNull Enchantment enchantment) {
      this.itemStack.removeEnchantment(enchantment);
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B flags(@NotNull ItemFlag... flags) {
      this.meta.addItemFlags(flags);
      return (B)this;
   }

   @NotNull
   @Contract(" -> this")
   public B unbreakable() {
      return this.unbreakable(true);
   }

   @NotNull
   @Contract("_ -> this")
   public B unbreakable(boolean unbreakable) {
      if (VersionHelper.IS_UNBREAKABLE_LEGACY) {
         return this.setNbt("Unbreakable", unbreakable);
      } else {
         this.meta.setUnbreakable(unbreakable);
         return (B)this;
      }
   }

   @NotNull
   @Contract(" -> this")
   public B glow() {
      return this.glow(true);
   }

   @NotNull
   @Contract("_ -> this")
   public B glow(boolean glow) {
      if (glow) {
         this.meta.addEnchant(Enchantment.LURE, 1, false);
         this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         return (B)this;
      } else {
         for (Enchantment enchantment : this.meta.getEnchants().keySet()) {
            this.meta.removeEnchant(enchantment);
         }

         return (B)this;
      }
   }

   @NotNull
   @Contract("_ -> this")
   public B pdc(@NotNull Consumer<PersistentDataContainer> consumer) {
      consumer.accept(this.meta.getPersistentDataContainer());
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B model(int modelData) {
      if (VersionHelper.IS_CUSTOM_MODEL_DATA) {
         this.meta.setCustomModelData(modelData);
      }

      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B color(@NotNull Color color) {
      if (LEATHER_ARMOR.contains(this.itemStack.getType())) {
         LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)this.getMeta();
         leatherArmorMeta.setColor(color);
         this.setMeta(leatherArmorMeta);
      }

      return (B)this;
   }

   @NotNull
   @Contract("_, _ -> this")
   public B setNbt(@NotNull String key, @NotNull String value) {
      this.itemStack.setItemMeta(this.meta);
      this.itemStack = ItemNbt.setString(this.itemStack, key, value);
      this.meta = this.itemStack.getItemMeta();
      return (B)this;
   }

   @NotNull
   @Contract("_, _ -> this")
   public B setNbt(@NotNull String key, boolean value) {
      this.itemStack.setItemMeta(this.meta);
      this.itemStack = ItemNbt.setBoolean(this.itemStack, key, value);
      this.meta = this.itemStack.getItemMeta();
      return (B)this;
   }

   @NotNull
   @Contract("_ -> this")
   public B removeNbt(@NotNull String key) {
      this.itemStack.setItemMeta(this.meta);
      this.itemStack = ItemNbt.removeTag(this.itemStack, key);
      this.meta = this.itemStack.getItemMeta();
      return (B)this;
   }

   @NotNull
   public ItemStack build() {
      this.itemStack.setItemMeta(this.meta);
      return this.itemStack;
   }

   @NotNull
   @Contract(" -> new")
   public GuiItem asGuiItem() {
      return new GuiItem(this.build());
   }

   @NotNull
   @Contract("_ -> new")
   public GuiItem asGuiItem(@NotNull GuiAction<InventoryClickEvent> action) {
      return new GuiItem(this.build(), action);
   }

   @NotNull
   protected ItemStack getItemStack() {
      return this.itemStack;
   }

   protected void setItemStack(@NotNull ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @NotNull
   protected ItemMeta getMeta() {
      return this.meta;
   }

   protected void setMeta(@NotNull ItemMeta meta) {
      this.meta = meta;
   }

   @Deprecated
   public B setName(@NotNull String name) {
      this.getMeta().setDisplayName(name);
      return (B)this;
   }

   @Deprecated
   public B setAmount(int amount) {
      this.getItemStack().setAmount(amount);
      return (B)this;
   }

   @Deprecated
   public B addLore(@NotNull String... lore) {
      return this.addLore(Arrays.asList(lore));
   }

   @Deprecated
   public B addLore(@NotNull List<String> lore) {
      List<String> newLore = (List<String>)(this.getMeta().hasLore() ? this.getMeta().getLore() : new ArrayList<>());
      newLore.addAll(lore);
      return this.setLore(newLore);
   }

   @Deprecated
   public B setLore(@NotNull String... lore) {
      return this.setLore(Arrays.asList(lore));
   }

   @Deprecated
   public B setLore(@NotNull List<String> lore) {
      this.getMeta().setLore(lore);
      return (B)this;
   }

   @Deprecated
   public B addEnchantment(@NotNull Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
      this.getMeta().addEnchant(enchantment, level, ignoreLevelRestriction);
      return (B)this;
   }

   @Deprecated
   public B addEnchantment(@NotNull Enchantment enchantment, int level) {
      return this.addEnchantment(enchantment, level, true);
   }

   @Deprecated
   public B addEnchantment(@NotNull Enchantment enchantment) {
      return this.addEnchantment(enchantment, 1, true);
   }

   @Deprecated
   public B removeEnchantment(@NotNull Enchantment enchantment) {
      this.getItemStack().removeEnchantment(enchantment);
      return (B)this;
   }

   @Deprecated
   public B addItemFlags(@NotNull ItemFlag... flags) {
      this.getMeta().addItemFlags(flags);
      return (B)this;
   }

   @Deprecated
   public B setUnbreakable(boolean unbreakable) {
      return this.unbreakable(unbreakable);
   }
}

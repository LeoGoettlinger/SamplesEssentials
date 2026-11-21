package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.registry.RegistryUtils;

@Deprecated
public class SimpleItemStack implements Cloneable {
   private static Material defaultMaterial;
   private Material material = null;
   private IntRange amount = new IntRange(1);
   private String displayName = null;
   private List<String> lore = null;
   private Map<Enchantment, Integer> enchantments = new HashMap<>();
   private Boolean enchantGlow = null;
   private int customModelData = 0;
   private String skullTexture = null;

   public SimpleItemStack() {
   }

   public SimpleItemStack(@Nullable Material material) {
      this.material = material;
   }

   public SimpleItemStack(@Nullable Material material, int amount) {
      this.material = material;
      this.amount = new IntRange(amount);
   }

   public SimpleItemStack(ItemStack itemStack) {
      this.material = itemStack.getType();
      this.amount = new IntRange(itemStack.getAmount(), itemStack.getAmount());
      this.enchantments = itemStack.getEnchantments();
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta != null) {
         if (itemMeta.hasDisplayName()) {
            this.displayName = itemMeta.getDisplayName();
         }

         if (itemMeta.hasLore()) {
            this.lore = itemMeta.getLore();
         }

         if (itemMeta.hasEnchants() && itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            this.enchantGlow = true;
         }

         if (itemMeta instanceof EnchantmentStorageMeta enchantmentMeta) {
            this.enchantments = enchantmentMeta.getEnchants();
         }

         if (itemMeta.hasCustomModelData()) {
            this.customModelData = itemMeta.getCustomModelData();
         }

         if (itemMeta instanceof SkullMeta) {
            this.skullTexture = SkullCreator.getB64(itemStack);
         }
      }
   }

   public SimpleItemStack(ConfigurationSection configurationSection) {
      if (configurationSection.contains("material")) {
         this.material = RegistryUtils.parseString(configurationSection.getString("material"), Registry.MATERIAL);
      }

      if (configurationSection.contains("amount")) {
         this.amount = IntRange.parseIntRange(configurationSection.getString("amount", "1"));
      }

      if (configurationSection.contains("display-name")) {
         this.displayName = configurationSection.getString("display-name");
      }

      if (configurationSection.contains("lore")) {
         this.lore = configurationSection.getStringList("lore");
      }

      if (configurationSection.contains("enchantments")) {
         Map<Enchantment, Integer> enchantments = new HashMap<>();
         configurationSection.getConfigurationSection("enchantments").getValues(false).forEach((enchantmentRaw, level) -> {
            Enchantment enchantment = RegistryUtils.parseString(enchantmentRaw, Registry.ENCHANTMENT);
            enchantments.put(enchantment, (Integer)level);
         });
         this.enchantments = enchantments;
      }

      if (configurationSection.contains("enchanted")) {
         this.enchantGlow = configurationSection.getBoolean("enchanted");
      }

      if (configurationSection.contains("custom-model-data")) {
         this.customModelData = configurationSection.getInt("custom-model-data");
      }

      if (configurationSection.contains("skull-texture")) {
         this.skullTexture = configurationSection.getString("skull-texture");
      }
   }

   public SimpleItemStack(@NotNull Map<?, ?> configurationMap) {
      try {
         if (configurationMap.containsKey("material")) {
            this.material = RegistryUtils.parseString((String)configurationMap.get("material"), Registry.MATERIAL);
         }

         if (configurationMap.containsKey("amount")) {
            this.amount = IntRange.valueOf(configurationMap.get("amount"));
         }

         if (configurationMap.containsKey("display-name")) {
            this.displayName = (String)configurationMap.get("display-name");
         }

         if (configurationMap.containsKey("lore")) {
            this.lore = (List<String>)configurationMap.get("lore");
         }

         if (configurationMap.containsKey("enchantments")) {
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            ((Map)configurationMap.get("enchantments")).forEach((enchantmentRaw, level) -> {
               Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentRaw));
               enchantments.put(enchantment, (Integer)level);
            });
            this.enchantments = enchantments;
         }

         if (configurationMap.containsKey("enchanted")) {
            this.enchantGlow = (Boolean)configurationMap.get("enchanted");
         }

         if (configurationMap.containsKey("custom-model-data")) {
            this.customModelData = (Integer)configurationMap.get("custom-model-data");
         }

         if (configurationMap.containsKey("skull-texture")) {
            this.skullTexture = (String)configurationMap.get("skull-texture");
         }
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException("Invalid format at '" + configurationMap + "', could not parse data", var3);
      }
   }

   @Nullable
   public Material getType() {
      return this.material;
   }

   public boolean hasType() {
      return this.material != null;
   }

   public void setType(@Nullable Material material) {
      this.material = material;
   }

   public IntRange getAmount() {
      return this.amount;
   }

   public void setAmount(int amount) {
      this.amount = new IntRange(amount);
   }

   public void setAmountRange(int minAmount, int maxAmount) {
      this.amount = new IntRange(minAmount, maxAmount);
   }

   public void setAmountRange(IntRange range) {
      this.amount = range;
   }

   @Nullable
   public String getDisplayName() {
      return this.displayName;
   }

   public boolean hasDisplayName() {
      return this.displayName != null;
   }

   public void setDisplayName(@Nullable String displayName) {
      this.displayName = displayName;
   }

   @Nullable
   public List<String> getLore() {
      return this.lore;
   }

   public boolean hasLore() {
      return this.lore != null;
   }

   public void setLore(@Nullable List<String> lore) {
      this.lore = lore;
   }

   public Map<Enchantment, Integer> getEnchantments() {
      return this.enchantments;
   }

   public boolean hasEnchantments() {
      return !this.enchantments.isEmpty();
   }

   public void setEnchantment(Enchantment enchantment, int level) {
      this.enchantments.put(enchantment, level);
   }

   public void setEnchantments(Map<Enchantment, Integer> enchantments) {
      this.enchantments = enchantments;
   }

   @Nullable
   public Boolean getEnchantGlow() {
      return this.enchantGlow;
   }

   public boolean hasEnchantGlow() {
      return this.enchantGlow != null;
   }

   public void setEnchantGlow(@Nullable Boolean enchantGlow) {
      this.enchantGlow = enchantGlow;
   }

   public int getCustomModelData() {
      return this.customModelData;
   }

   public boolean hasCustomModelData() {
      return this.customModelData != 0;
   }

   public void setCustomModelData(int customModelData) {
      this.customModelData = customModelData;
   }

   public String getSkullTexture() {
      return this.skullTexture;
   }

   public boolean hasSkullTexture() {
      return this.skullTexture != null;
   }

   public void setSkullTexture(@Nullable String texture) {
      this.skullTexture = texture;
   }

   public boolean isBlank() {
      return this.material == null
         && this.amount.getMin() == 1
         && this.amount.getMax() == 1
         && this.displayName == null
         && this.lore == null
         && this.enchantments.isEmpty()
         && this.enchantGlow == null
         && this.customModelData == 0
         && this.skullTexture == null;
   }

   public void parseColors(Player player) {
      if (this.hasDisplayName()) {
         this.displayName = ChatColorHandler.translate(this.displayName, player);
      }

      if (this.hasLore()) {
         this.lore = this.lore.stream().map(line -> ChatColorHandler.translate(line, player)).toList();
      }
   }

   public ItemStack asItemStack() {
      return this.asItemStack(null, true);
   }

   public ItemStack asItemStack(@Nullable Player player) {
      return this.asItemStack(player, true);
   }

   public ItemStack asItemStack(@Nullable Player player, boolean parseColors) {
      if (this.material == null && defaultMaterial != null) {
         LushLogger.logCurrentStackTrace(Level.WARNING, "Material cannot be null, defaulting to '" + defaultMaterial + "':");
         this.material = defaultMaterial;
      }

      ItemStack itemStack = new ItemStack(this.material, this.amount.next());
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (!(itemMeta instanceof EnchantmentStorageMeta) && !this.enchantments.isEmpty()) {
         this.enchantments.forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
      }

      if (itemMeta != null) {
         if (this.displayName != null) {
            itemMeta.setDisplayName(parseColors ? ChatColorHandler.translate(this.displayName, player) : this.displayName);
         }

         if (this.lore != null) {
            itemMeta.setLore(parseColors ? this.lore.stream().map(line -> ChatColorHandler.translate(line, player)).toList() : this.lore);
         }

         if (!this.enchantments.isEmpty()) {
            if (itemMeta instanceof EnchantmentStorageMeta enchantmentMeta) {
               this.enchantments.forEach((enchantment, level) -> enchantmentMeta.addStoredEnchant(enchantment, level, true));
            } else {
               this.enchantments.forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
            }
         } else if (this.enchantGlow != null && this.enchantGlow) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         if (this.customModelData != 0) {
            itemMeta.setCustomModelData(this.customModelData);
         }

         if (itemMeta instanceof SkullMeta skullMeta && this.skullTexture != null) {
            if (this.skullTexture.equals("mirror") && player != null) {
               String playerB64 = SkullCreator.getTexture(player);
               if (playerB64 != null) {
                  SkullCreator.mutateItemMeta(skullMeta, playerB64);
               }
            } else {
               SkullCreator.mutateItemMeta(skullMeta, this.skullTexture);
            }
         }

         itemStack.setItemMeta(itemMeta);
      }

      return itemStack;
   }

   public void save(ConfigurationSection configurationSection) {
      if (this.material != null) {
         configurationSection.set("material", this.material.name());
      }

      if (this.amount.getMin() != 1 && this.amount.getMax() != 1) {
         configurationSection.set("amount", this.amount);
      }

      if (this.displayName != null) {
         configurationSection.set("display-name", this.displayName);
      }

      if (this.lore != null) {
         configurationSection.set("lore", this.lore);
      }

      if (!this.enchantments.isEmpty()) {
         Map<String, Object> enchantmentMap = new HashMap<>();
         this.enchantments.forEach((enchantment, level) -> enchantmentMap.put(enchantment.getKey().toString().toLowerCase(), level));
         configurationSection.set("enchantments", enchantmentMap);
      }

      if (this.enchantGlow != null) {
         configurationSection.set("enchanted", this.enchantGlow);
      }

      if (this.customModelData != 0) {
         configurationSection.set("custom-model-data", this.customModelData);
      }

      if (this.skullTexture != null) {
         configurationSection.set("skull-texture", this.skullTexture);
      }
   }

   public Map<String, Object> asMap() {
      Map<String, Object> map = new HashMap<>();
      if (this.material != null) {
         map.put("material", this.material.name());
      }

      if (this.amount.getMin() != 1 && this.amount.getMax() != 1) {
         map.put("amount", this.amount);
      }

      if (this.displayName != null) {
         map.put("display-name", this.displayName);
      }

      if (this.lore != null) {
         map.put("lore", this.lore);
      }

      if (!this.enchantments.isEmpty()) {
         Map<String, Object> enchantmentMap = new HashMap<>();
         this.enchantments.forEach((enchantment, level) -> enchantmentMap.put(enchantment.getKey().toString().toLowerCase(), level));
         map.put("enchantments", enchantmentMap);
      }

      if (this.enchantGlow != null) {
         map.put("enchanted", this.enchantGlow);
      }

      if (this.customModelData != 0) {
         map.put("custom-model-data", this.customModelData);
      }

      if (this.skullTexture != null) {
         map.put("skull-texture", this.skullTexture);
      }

      return map;
   }

   public SimpleItemStack overwrite(@NotNull SimpleItemStack overwrite) {
      SimpleItemStack result = new SimpleItemStack();
      if (overwrite.hasType()) {
         result.setType(overwrite.getType());
         result.setCustomModelData(overwrite.getCustomModelData());
      } else {
         result.setType(this.material);
         result.setCustomModelData(this.customModelData);
      }

      result.setAmountRange(overwrite.getAmount().getMin() != 1 && overwrite.getAmount().getMax() != 1 ? overwrite.getAmount() : this.amount);
      result.setDisplayName(overwrite.hasDisplayName() ? overwrite.getDisplayName() : this.displayName);
      result.setLore(overwrite.hasLore() ? overwrite.getLore() : this.lore);
      result.setEnchantments(overwrite.hasEnchantments() ? overwrite.getEnchantments() : this.enchantments);
      result.setEnchantGlow(overwrite.hasEnchantGlow() ? overwrite.getEnchantGlow() : this.enchantGlow);
      result.setSkullTexture(overwrite.hasSkullTexture() ? overwrite.getSkullTexture() : this.skullTexture);
      return result;
   }

   public SimpleItemStack overwrite(@NotNull SimpleItemStack... overwrites) {
      SimpleItemStack result = this;

      for (SimpleItemStack overwrite : overwrites) {
         result = result.overwrite(overwrite);
      }

      return result;
   }

   public SimpleItemStack clone() {
      try {
         SimpleItemStack clone = (SimpleItemStack)super.clone();
         clone.setType(this.material);
         clone.setAmountRange(this.amount);
         clone.setDisplayName(this.displayName);
         clone.setLore(this.lore);
         clone.setEnchantments(this.enchantments);
         clone.setCustomModelData(this.customModelData);
         clone.setSkullTexture(this.skullTexture);
         return clone;
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError();
      }
   }

   public static void setDefaultMaterial(Material material) {
      defaultMaterial = material;
   }
}

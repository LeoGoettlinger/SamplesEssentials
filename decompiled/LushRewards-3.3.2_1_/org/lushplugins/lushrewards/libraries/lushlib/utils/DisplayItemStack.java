package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonAlias;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonAutoDetect;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonProperty;

@JsonAutoDetect(
   isGetterVisibility = JsonAutoDetect.Visibility.NONE,
   getterVisibility = JsonAutoDetect.Visibility.NONE,
   fieldVisibility = JsonAutoDetect.Visibility.ANY
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisplayItemStack {
   private static final DisplayItemStack EMPTY = builder().build();
   private final Material material;
   private final IntRange amount;
   private final String displayName;
   private final List<String> lore;
   private final Boolean enchantGlow;
   private final int customModelData;
   private final String skullTexture;

   @JsonCreator
   public DisplayItemStack(
      @JsonProperty("material") @Nullable Material material,
      @JsonProperty("amount") @NotNull IntRange amount,
      @JsonProperty("displayName") @JsonAlias({"display-name"}) @Nullable String displayName,
      @JsonProperty("lore") @Nullable List<String> lore,
      @JsonProperty("enchantGlow") @JsonAlias({"enchant-glow", "enchanted"}) @Nullable Boolean enchantGlow,
      @JsonProperty("customModelData") @JsonAlias({"custom-model-data"}) int customModelData,
      @JsonProperty("skullTexture") @JsonAlias({"skull-texture"}) @Nullable String skullTexture
   ) {
      this.material = material;
      this.amount = amount;
      this.displayName = displayName;
      this.lore = lore != null ? Collections.unmodifiableList(lore) : null;
      this.enchantGlow = enchantGlow;
      this.customModelData = customModelData;
      this.skullTexture = skullTexture;
   }

   @Nullable
   public Material getType() {
      return this.material;
   }

   public boolean hasType() {
      return this.material != null;
   }

   public IntRange getAmount() {
      return this.amount.clone();
   }

   @Nullable
   public String getDisplayName() {
      return this.displayName;
   }

   public boolean hasDisplayName() {
      return this.displayName != null;
   }

   @Nullable
   public List<String> getLore() {
      return this.lore;
   }

   public boolean hasLore() {
      return this.lore != null;
   }

   @Nullable
   public Boolean getEnchantGlow() {
      return this.enchantGlow;
   }

   public boolean hasEnchantGlow() {
      return this.enchantGlow != null;
   }

   public int getCustomModelData() {
      return this.customModelData;
   }

   public boolean hasCustomModelData() {
      return this.customModelData != 0;
   }

   public String getSkullTexture() {
      return this.skullTexture;
   }

   public boolean hasSkullTexture() {
      return this.skullTexture != null;
   }

   public boolean isBlank() {
      return this.material == null
         && this.amount.getMin() == 1
         && this.amount.getMax() == 1
         && this.displayName == null
         && this.lore == null
         && this.enchantGlow == null
         && this.customModelData == 0
         && this.skullTexture == null;
   }

   public boolean hasMeta() {
      return this.hasDisplayName() || this.hasLore() || this.hasEnchantGlow() || this.hasCustomModelData() || this.hasSkullTexture();
   }

   public boolean isSimilar(@NotNull ItemStack itemStack) {
      Material material = this.getType();
      if (material != null && itemStack.getType() != material) {
         return false;
      } else if ((this.amount.getMin() != 1 || this.amount.getMax() != 1) && !this.getAmount().contains(itemStack.getAmount())) {
         return false;
      } else {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            return !this.hasMeta();
         } else {
            String displayName = this.getDisplayName();
            if (displayName != null && !itemMeta.getDisplayName().equals(displayName)) {
               return false;
            } else {
               List<String> lore = this.getLore();
               if (lore == null || itemMeta.getLore() != null && itemMeta.getLore().equals(lore)) {
                  Boolean enchantGlow = this.getEnchantGlow();
                  if (enchantGlow != null && !itemMeta.getDisplayName().equals(displayName)) {
                     return false;
                  } else {
                     int customModelData = this.getCustomModelData();
                     return customModelData == 0 || itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == customModelData;
                  }
               } else {
                  return false;
               }
            }
         }
      }
   }

   public ItemStack asItemStack() {
      return this.asItemStack(null, true);
   }

   public ItemStack asItemStack(@Nullable Player player) {
      return this.asItemStack(player, true);
   }

   public ItemStack asItemStack(@Nullable Player player, boolean parseColors) {
      if (this.material == null) {
         throw new IllegalArgumentException("Material cannot be null");
      } else {
         ItemStack itemStack = new ItemStack(this.material, this.amount.next());
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta != null) {
            if (this.displayName != null) {
               itemMeta.setDisplayName(parseColors ? ChatColorHandler.translate(this.displayName, player) : this.displayName);
            }

            if (this.lore != null) {
               itemMeta.setLore(parseColors ? this.lore.stream().map(line -> ChatColorHandler.translate(line, player)).toList() : this.lore);
            }

            if (this.enchantGlow != null && this.enchantGlow) {
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
   }

   public static DisplayItemStack empty() {
      return EMPTY;
   }

   public static DisplayItemStack.Builder builder() {
      return builder((Material)null);
   }

   public static DisplayItemStack.Builder builder(Material material) {
      return new DisplayItemStack.Builder(material);
   }

   public static DisplayItemStack.Builder builder(ItemStack item) {
      DisplayItemStack.Builder itemBuilder = builder(item.getType()).setAmount(item.getAmount());
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null) {
         if (itemMeta.hasDisplayName()) {
            itemBuilder.setDisplayName(itemMeta.getDisplayName());
         }

         if (itemMeta.hasLore()) {
            itemBuilder.setLore(itemMeta.getLore());
         }

         if (itemMeta.hasEnchants()) {
            itemBuilder.setEnchantGlow(true);
         }

         if (itemMeta.hasCustomModelData()) {
            itemBuilder.setCustomModelData(itemMeta.getCustomModelData());
         }

         if (itemMeta instanceof SkullMeta) {
            itemBuilder.setSkullTexture(SkullCreator.getB64(item));
         }
      }

      return itemBuilder;
   }

   public static DisplayItemStack.Builder builder(DisplayItemStack item) {
      return builder()
         .setType(item.getType())
         .setAmountRange(item.getAmount())
         .setDisplayName(item.getDisplayName())
         .setLore(item.getLore())
         .setEnchantGlow(item.getEnchantGlow())
         .setCustomModelData(item.getCustomModelData())
         .setSkullTexture(item.getSkullTexture());
   }

   public static class Builder implements Cloneable {
      private Material material;
      private IntRange amount = new IntRange(1);
      private String displayName = null;
      private List<String> lore = null;
      private Boolean enchantGlow = null;
      private int customModelData = 0;
      private String skullTexture = null;

      @Deprecated(
         forRemoval = true
      )
      public Builder() {
         this(null);
      }

      @Deprecated(
         forRemoval = true
      )
      public Builder(@Nullable Material material) {
         this.material = material;
      }

      @Nullable
      public Material getType() {
         return this.material;
      }

      public boolean hasType() {
         return this.material != null;
      }

      public DisplayItemStack.Builder setType(@Nullable Material material) {
         this.material = material;
         return this;
      }

      public IntRange getAmount() {
         return this.amount.clone();
      }

      public DisplayItemStack.Builder setAmount(int amount) {
         this.amount = new IntRange(amount);
         return this;
      }

      public DisplayItemStack.Builder setAmountRange(int minAmount, int maxAmount) {
         this.amount = new IntRange(minAmount, maxAmount);
         return this;
      }

      public DisplayItemStack.Builder setAmountRange(IntRange range) {
         this.amount = range;
         return this;
      }

      @Nullable
      public String getDisplayName() {
         return this.displayName;
      }

      public boolean hasDisplayName() {
         return this.displayName != null;
      }

      public DisplayItemStack.Builder setDisplayName(@Nullable String displayName) {
         this.displayName = displayName;
         return this;
      }

      @Nullable
      public List<String> getLore() {
         return this.lore != null ? Collections.unmodifiableList(this.lore) : null;
      }

      public boolean hasLore() {
         return this.lore != null;
      }

      public DisplayItemStack.Builder setLore(@Nullable List<String> lore) {
         this.lore = lore;
         return this;
      }

      @Nullable
      public Boolean getEnchantGlow() {
         return this.enchantGlow;
      }

      public boolean hasEnchantGlow() {
         return this.enchantGlow != null;
      }

      public DisplayItemStack.Builder setEnchantGlow(@Nullable Boolean enchantGlow) {
         this.enchantGlow = enchantGlow;
         return this;
      }

      public int getCustomModelData() {
         return this.customModelData;
      }

      public boolean hasCustomModelData() {
         return this.customModelData != 0;
      }

      public DisplayItemStack.Builder setCustomModelData(int customModelData) {
         this.customModelData = customModelData;
         return this;
      }

      public String getSkullTexture() {
         return this.skullTexture;
      }

      public boolean hasSkullTexture() {
         return this.skullTexture != null;
      }

      public DisplayItemStack.Builder setSkullTexture(@Nullable String texture) {
         this.skullTexture = texture;
         return this;
      }

      public DisplayItemStack build() {
         return new DisplayItemStack(
            this.material,
            this.amount,
            this.displayName,
            this.lore != null ? new ArrayList<>(this.lore) : null,
            this.enchantGlow,
            this.customModelData,
            this.skullTexture
         );
      }

      public boolean isBlank() {
         return this.material == null
            && this.amount.getMin() == 1
            && this.amount.getMax() == 1
            && this.displayName == null
            && this.lore == null
            && this.enchantGlow == null
            && this.customModelData == 0
            && this.skullTexture == null;
      }

      public DisplayItemStack.Builder parseColors(Player player) {
         if (this.hasDisplayName()) {
            this.displayName = ChatColorHandler.translate(this.displayName, player);
         }

         if (this.hasLore()) {
            this.lore = this.lore.stream().map(line -> ChatColorHandler.translate(line, player)).toList();
         }

         return this;
      }

      public DisplayItemStack.Builder overwrite(@NotNull DisplayItemStack.Builder overwrite) {
         DisplayItemStack.Builder result = new DisplayItemStack.Builder();
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
         result.setEnchantGlow(overwrite.hasEnchantGlow() ? overwrite.getEnchantGlow() : this.enchantGlow);
         result.setSkullTexture(overwrite.hasSkullTexture() ? overwrite.getSkullTexture() : this.skullTexture);
         return result;
      }

      public DisplayItemStack.Builder overwrite(@NotNull DisplayItemStack.Builder... overwrites) {
         DisplayItemStack.Builder result = this;

         for (DisplayItemStack.Builder overwrite : overwrites) {
            result = result.overwrite(overwrite);
         }

         return result;
      }

      public DisplayItemStack.Builder clone() {
         try {
            DisplayItemStack.Builder clone = (DisplayItemStack.Builder)super.clone();
            clone.setAmountRange(this.amount.clone());
            clone.setLore(this.lore != null ? new ArrayList<>(this.lore) : null);
            return clone;
         } catch (CloneNotSupportedException var2) {
            throw new AssertionError();
         }
      }

      @Deprecated(
         forRemoval = true
      )
      public static DisplayItemStack.Builder of(DisplayItemStack item) {
         return DisplayItemStack.builder(item);
      }

      @Deprecated(
         forRemoval = true
      )
      public static DisplayItemStack.Builder of(ItemStack item) {
         return DisplayItemStack.builder(item);
      }
   }
}

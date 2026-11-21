package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemBuilder {
   private ItemStack item;
   private ItemMeta meta;
   private String customId;
   private String skullTexture;
   private String skullOwnerName;
   private UUID skullOwnerUuid;

   private ItemMeta ensureMeta() {
      if (this.meta != null) {
         return this.meta;
      } else {
         this.meta = this.item.getItemMeta();
         if (this.meta != null) {
            return this.meta;
         } else {
            try {
               this.meta = Bukkit.getItemFactory().getItemMeta(this.item.getType());
            } catch (Exception var2) {
            }

            return this.meta;
         }
      }
   }

   public ItemBuilder(Material material) {
      if (material == null) {
         material = Material.STONE;
      }

      this.item = new ItemStack(material);
      this.meta = this.item.getItemMeta();
      this.ensureMeta();
   }

   public ItemBuilder(ItemStack item) {
      this.item = item.clone();
      this.meta = this.item.getItemMeta();
      this.ensureMeta();

      try {
         NBT.get(this.item, nbt -> {
            if (nbt.hasTag("custom_id")) {
               this.customId = nbt.getString("custom_id");
            }
         });
      } catch (Exception var3) {
      }
   }

   public ItemBuilder(XMaterial material) {
      this.item = material.parseItem();
      if (this.item == null) {
         this.item = new ItemStack(XMaterial.STONE.get());
      }

      this.meta = this.item.getItemMeta();
      this.ensureMeta();
   }

   public ItemBuilder setAmount(int amount) {
      this.item.setAmount(amount);
      return this;
   }

   public ItemBuilder setDisplayName(String name) {
      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         meta.setDisplayName(name);
      }

      return this;
   }

   public ItemBuilder setLore(String... lore) {
      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         meta.setLore(Arrays.asList(lore));
      }

      return this;
   }

   public ItemBuilder setLore(List<String> lore) {
      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         meta.setLore(lore);
      }

      return this;
   }

   public ItemBuilder addLoreLine(String line) {
      ItemMeta meta = this.ensureMeta();
      if (meta == null) {
         return this;
      } else {
         List<String> lore = meta.getLore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         lore.add(line);
         meta.setLore(lore);
         return this;
      }
   }

   public ItemBuilder addEnchant(Enchantment enchantment, int level) {
      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         meta.addEnchant(enchantment, level, true);
      }

      return this;
   }

   public ItemBuilder addItemFlags(ItemFlag... flags) {
      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         meta.addItemFlags(flags);
      }

      return this;
   }

   public ItemBuilder hideTooltip(boolean tooltip) {
      ItemMeta meta = this.ensureMeta();
      meta.setDisplayName(" ");
      PlatformAccess.get().applyHideTooltip(meta, tooltip);
      return this;
   }

   public ItemBuilder setUnbreakable(boolean unbreakable) {
      ItemMeta meta = this.ensureMeta();
      PlatformAccess.get().applyUnbreakable(meta, unbreakable);
      return this;
   }

   public ItemBuilder setCustomModelData(int data) {
      ItemMeta meta = this.ensureMeta();
      PlatformAccess.get().applyCustomModelData(meta, data);
      return this;
   }

   public ItemBuilder setSkullOwner(UUID uuid) {
      this.skullOwnerUuid = uuid;
      return this;
   }

   public ItemBuilder setSkullOwner(String playerName) {
      if (playerName != null && !playerName.isEmpty()) {
         this.skullOwnerName = playerName;
      }

      return this;
   }

   public ItemBuilder setLeatherColor(Color color) {
      ItemMeta meta = this.ensureMeta();
      if (meta instanceof LeatherArmorMeta) {
         ((LeatherArmorMeta)meta).setColor(color);
      }

      return this;
   }

   public ItemBuilder setCustomId(String id) {
      this.customId = id;
      return this;
   }

   public boolean hasCustomId() {
      return this.customId != null;
   }

   public String getCustomId() {
      return this.customId;
   }

   public ItemBuilder setSkullTexture(String texture) {
      this.skullTexture = texture;
      return this;
   }

   public ItemStack build() {
      if (this.skullTexture != null || this.skullOwnerName != null || this.skullOwnerUuid != null) {
         this.item = PlatformAccess.get().ensurePlayerHeadItem(this.item);
      }

      ItemMeta meta = this.ensureMeta();
      if (meta != null) {
         if (this.skullOwnerUuid != null || this.skullOwnerName != null) {
            if (this.skullOwnerUuid != null) {
               PlatformAccess.get().applySkullOwner(meta, this.skullOwnerUuid);
            }

            if (this.skullOwnerName != null) {
               PlatformAccess.get().applySkullOwner(meta, this.skullOwnerName);
            }
         }

         this.item.setItemMeta(meta);
      }

      if (this.customId != null || this.skullTexture != null) {
         try {
            this.applyNbtData();
         } catch (Exception var3) {
            Bukkit.getLogger().warning("Failed to apply NBT data to item: " + var3.getMessage());
         }
      }

      return this.item;
   }

   private void applyNbtData() {
      String version = Bukkit.getBukkitVersion().split("-")[0];
      VersionComparator comparator = new VersionComparator();
      if (comparator.isGreaterThanOrEqual(version, "1.20.5")) {
         this.apply1_20_5PlusNbt();
      } else {
         this.applyLegacyNbt();
      }
   }

   private void apply1_20_5PlusNbt() {
      if (this.skullTexture != null) {
         NBT.modifyComponents(this.item, nbt -> {
            ReadWriteNBT profile = nbt.getOrCreateCompound("minecraft:profile");
            profile.setUUID("id", UUID.randomUUID());
            ReadWriteNBT properties = profile.getCompoundList("properties").addCompound();
            properties.setString("name", "textures");
            properties.setString("value", this.skullTexture);
         });
      }

      if (this.customId != null) {
         NBT.modify(this.item, nbt -> nbt.setString("custom_id", this.customId));
      }
   }

   private void applyLegacyNbt() {
      NBT.modify(this.item, nbt -> {
         if (this.customId != null) {
            nbt.setString("custom_id", this.customId);
         }

         if (this.skullTexture != null) {
            ReadWriteNBT skullOwner = nbt.getOrCreateCompound("SkullOwner");
            skullOwner.setUUID("Id", UUID.randomUUID());
            ReadWriteNBT properties = skullOwner.getOrCreateCompound("Properties");
            ReadWriteNBT textures = properties.getCompoundList("textures").addCompound();
            textures.setString("Value", this.skullTexture);
         }
      });
   }
}

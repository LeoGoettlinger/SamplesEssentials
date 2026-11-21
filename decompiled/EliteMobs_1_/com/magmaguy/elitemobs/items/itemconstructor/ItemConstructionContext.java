package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.ItemConstructionSnapshot;
import com.magmaguy.freeminecraftmodels.api.ItemConstructionSnapshot.ItemData;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemConstructionContext {
   private static final NamespacedKey FMM_ITEM_ID = Objects.requireNonNull(NamespacedKey.fromString("freeminecraftmodels:fmm_item_id"));
   private final EnchantmentItems.Construction enchantments;
   private final ItemConstructionSnapshot models;
   private final boolean modelsEnabled;

   private ItemConstructionContext(Collection<CustomItemsConfigFields> configs) {
      Set<String> ids = new HashSet<>(EliteEnchantmentCatalog.definitionIds());
      MagicEnchantmentGeneration.KEYS.forEach(key -> ids.add("freeminecraftmodels:" + key));

      for (CustomItemsConfigFields config : configs) {
         for (String entry : config.getEnchantments()) {
            String id = entry.split(",", 2)[0];
            if (!id.startsWith("minecraft:") && id.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) {
               ids.add(id);
            }
         }
      }

      this.enchantments = EnchantmentItems.prepareConstruction(ids);
      this.modelsEnabled = Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels");
      this.models = this.modelsEnabled ? ItemConstructionSnapshot.capture() : null;
   }

   public static ItemConstructionContext capture(Collection<CustomItemsConfigFields> configs) {
      return new ItemConstructionContext(configs);
   }

   public EnchantmentItems.Construction enchantments() {
      return this.enchantments;
   }

   public boolean modelsEnabled() {
      return this.modelsEnabled;
   }

   public ItemData model(String id) {
      if (this.models != null && id != null && !id.isBlank()) {
         ItemData weapon = this.models.weapon(id);
         return weapon == null ? this.models.item(id) : weapon;
      } else {
         return null;
      }
   }

   public ItemData scriptedItem(String id) {
      return this.models == null ? null : this.models.item(id);
   }

   public SkillType skill(ItemStack item) {
      if (item != null && !item.getType().isAir()) {
         ItemMeta meta = item.getItemMeta();
         String id = meta == null ? null : (String)meta.getPersistentDataContainer().get(FMM_ITEM_ID, PersistentDataType.STRING);
         if (id != null) {
            ItemData data = this.scriptedItem(id);
            if (data == null || data.authoredId() == null) {
               return null;
            }

            if (data.kind() != null) {
               return data.kind() == MagicWeaponKind.WAND ? SkillType.WANDS : SkillType.STAVES;
            }
         }

         return SkillType.fromMaterial(item.getType());
      } else {
         return null;
      }
   }

   public void requireCurrent() {
      this.enchantments.requireCurrent();
      if (this.modelsEnabled != Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         throw new ConcurrentModificationException("FMM availability changed during item construction");
      } else {
         if (this.models != null) {
            this.models.requireCurrent();
         }
      }
   }
}

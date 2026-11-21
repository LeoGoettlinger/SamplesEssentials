package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemConsumables {
   private ItemConsumables() {
   }

   public static ItemConsumables.Definition resolve(ItemStack item) {
      if (item != null && !item.getType().isAir() && item.getAmount() >= 1) {
         String id = ItemTagger.getCustomItemId(item);
         if (id == null) {
            return null;
         } else {
            CustomItem authored = CustomItem.getCustomItem(id);
            if (authored == null) {
               return null;
            } else {
               CustomItemsConfigFields fields = authored.getCustomItemsConfigFields();
               return fields.isEnabled() && fields.getMaterial() == item.getType() ? fields.getConsumable() : null;
            }
         }
      } else {
         return null;
      }
   }

   public static boolean is(ItemStack item, ItemConsumables.Type type) {
      ItemConsumables.Definition definition = resolve(item);
      return definition != null && definition.type() == type;
   }

   public static int repairTier(ItemStack item) {
      ItemConsumables.Definition definition = resolve(item);
      return definition != null && definition.type() == ItemConsumables.Type.REPAIR_SCRAP ? definition.tier() : 0;
   }

   public static ItemStack generateScrap(ItemStack source, Player player, EliteEntity elite) {
      int level = EliteItemManager.getRoundedItemLevel(source);
      int tier = level < 50 ? 1 : (level < 100 ? 2 : (level < 150 ? 3 : (level < 200 ? 4 : 5)));

      String name = switch (tier) {
         case 1 -> "tiny";
         case 2 -> "small";
         case 3 -> "medium";
         case 4 -> "large";
         default -> "huge";
      };
      CustomItem scrap = CustomItem.getCustomItem("elite_scrap_" + name + ".yml");
      if (scrap != null && new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, tier).equals(scrap.getCustomItemsConfigFields().getConsumable())) {
         return scrap.generateItemStack(tier, player, elite);
      } else {
         Logger.warn("Cannot generate elite scrap: the enabled default item must declare repair_scrap tier " + tier);
         return null;
      }
   }

   public static ItemStack unbind(ItemStack source) {
      ItemStack result = source.clone();
      ItemMeta meta = result.getItemMeta();
      if (meta == null) {
         throw new IllegalArgumentException("Cannot unbind an item without metadata");
      } else {
         meta.getPersistentDataContainer().remove(SoulbindEnchantment.SOULBIND_KEY);
         meta.getPersistentDataContainer().remove(SoulbindEnchantment.PRESTIGE_KEY);
         result.setItemMeta(meta);
         new EliteItemLore(result, false);
         return result;
      }
   }

   public record Definition(ItemConsumables.Type type, int tier) {
      public Definition(ItemConsumables.Type type, int tier) {
         Objects.requireNonNull(type, "consumable type");
         if (type == ItemConsumables.Type.REPAIR_SCRAP ? tier >= 1 && tier <= 5 : tier == 0) {
            this.type = type;
            this.tier = tier;
         } else {
            throw new IllegalArgumentException("Repair scrap requires tier 1-5; other consumables have no tier");
         }
      }
   }

   public static enum Type {
      LUCKY_TICKET,
      REPAIR_SCRAP,
      UNBIND;
   }
}

package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;

public abstract class AdventurerRewardItemConfig extends CustomItemsConfigFields {
   protected AdventurerRewardItemConfig(String item, Material material, List<String> enchantments) {
      super(
         "ag_adventurer_" + item.toLowerCase(Locale.ROOT),
         material != null,
         material,
         "&aAdventurer's " + item,
         List.of("&7Awarded by Casus for completing your first class trial.")
      );
      this.setLevel(1);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setEnchantments(enchantments);
   }
}

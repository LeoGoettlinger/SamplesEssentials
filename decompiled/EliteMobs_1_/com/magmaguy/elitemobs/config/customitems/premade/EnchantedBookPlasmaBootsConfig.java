package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class EnchantedBookPlasmaBootsConfig extends CustomItemsConfigFields {
   public EnchantedBookPlasmaBootsConfig() {
      super(
         "enchanted_book_plasma_boots",
         true,
         Material.ENCHANTED_BOOK,
         "&5Elite Plasma Boots Enchanted Book",
         new ArrayList<>(List.of("&2Used to enchant items at the enchanter!"))
      );
      this.setEnchantments(new ArrayList<>(List.of("elitemobs:plasma_boots,1")));
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

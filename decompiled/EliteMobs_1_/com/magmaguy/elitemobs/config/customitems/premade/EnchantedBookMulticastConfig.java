package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public final class EnchantedBookMulticastConfig extends CustomItemsConfigFields {
   public EnchantedBookMulticastConfig() {
      super(
         "enchanted_book_multicast",
         true,
         Material.ENCHANTED_BOOK,
         "&5Elite Multicast Enchanted Book",
         new ArrayList<>(List.of("&2Used at the enchanter.", "&7Wands only. Adds extra magic bolts."))
      );
      this.setEnchantments(new ArrayList<>(List.of("freeminecraftmodels:multicast,1")));
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

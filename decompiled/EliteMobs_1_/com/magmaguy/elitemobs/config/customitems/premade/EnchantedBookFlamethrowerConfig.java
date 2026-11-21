package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class EnchantedBookFlamethrowerConfig extends CustomItemsConfigFields {
   public EnchantedBookFlamethrowerConfig() {
      super(
         "enchanted_book_flamethrower",
         true,
         Material.ENCHANTED_BOOK,
         "&5Elite Flamethrower Enchanted Book",
         new ArrayList<>(List.of("&2Used to enchant items at the enchanter!"))
      );
      this.setEnchantments(new ArrayList<>(List.of("elitemobs:flamethrower,1")));
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

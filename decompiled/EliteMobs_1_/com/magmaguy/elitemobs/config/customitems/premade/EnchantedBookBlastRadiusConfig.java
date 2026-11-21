package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public final class EnchantedBookBlastRadiusConfig extends CustomItemsConfigFields {
   public EnchantedBookBlastRadiusConfig() {
      super(
         "enchanted_book_blast_radius",
         true,
         Material.ENCHANTED_BOOK,
         "&5Elite Blast Radius Enchanted Book",
         new ArrayList<>(List.of("&2Used at the enchanter.", "&7Staves only. Increases explosion radius."))
      );
      this.setEnchantments(new ArrayList<>(List.of("freeminecraftmodels:blast_radius,1")));
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

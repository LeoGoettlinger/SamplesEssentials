package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ScrapEliteHugeConfig extends CustomItemsConfigFields {
   public ScrapEliteHugeConfig() {
      super(
         "elite_scrap_huge",
         true,
         Material.PURPLE_DYE,
         "&5Huge Elite Scrap",
         new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fFully repairs an item!"))
      );
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, 5));
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

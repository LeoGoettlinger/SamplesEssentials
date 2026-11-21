package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ScrapEliteTinyConfig extends CustomItemsConfigFields {
   public ScrapEliteTinyConfig() {
      super(
         "elite_scrap_tiny",
         true,
         Material.WHITE_DYE,
         "&fTiny Elite Scrap",
         new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fRepairs a small amount!"))
      );
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.REPAIR_SCRAP, 1));
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

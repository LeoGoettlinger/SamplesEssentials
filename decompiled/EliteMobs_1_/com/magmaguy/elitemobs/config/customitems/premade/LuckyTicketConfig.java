package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class LuckyTicketConfig extends CustomItemsConfigFields {
   public LuckyTicketConfig() {
      super(
         "elite_lucky_ticket",
         true,
         Material.PAPER,
         "&6Elite Lucky Ticket",
         new ArrayList<>(List.of("&2Doubles the chance of successfully", "&2enchanting an item!"))
      );
      this.setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.LUCKY_TICKET, 0));
      this.setItemType(CustomItem.ItemType.UNIQUE);
      this.setScalability(CustomItem.Scalability.FIXED);
      this.setSoulbound(false);
   }
}

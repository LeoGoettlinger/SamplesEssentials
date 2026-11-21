package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class SummonWolfScrollConfig extends CustomItemsConfigFields {
   public SummonWolfScrollConfig() {
      super(
         "summon_wolf_scroll",
         true,
         Material.PAPER,
         "&6Summon Wolf Scroll",
         new ArrayList<>(List.of("&aSummons a good boy", "&ato help you bite Elites!", "&aMake sure you pet it!"))
      );
      this.setEnchantments(List.of("elitemobs:summon_wolf,1"));
      this.setItemType(CustomItem.ItemType.UNIQUE);
   }
}

package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class UnbindScrollConfig extends CustomItemsConfigFields {
   public UnbindScrollConfig() {
      super(
         "unbind_scroll",
         true,
         Material.PAPER,
         "&5Unbind Scroll",
         new ArrayList<>(List.of("&5Allows users to unbind one", "&5soulbound item at the Unbinder!", "&5Use wisely!"))
      );
      this.setConsumable(new ItemConsumables.Definition(ItemConsumables.Type.UNBIND, 0));
      this.setItemType(CustomItem.ItemType.UNIQUE);
   }
}

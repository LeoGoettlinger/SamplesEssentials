package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class MeteorShowerScrollConfig extends CustomItemsConfigFields {
   public MeteorShowerScrollConfig() {
      super("meteor_shower_scroll", true, Material.PAPER, "&7Meteor Shower Scroll", new ArrayList<>(List.of("&4Call forth destruction.", "&4Single-use.")));
      this.setEnchantments(List.of("elitemobs:meteor_shower,1"));
      this.setItemType(CustomItem.ItemType.UNIQUE);
   }
}

package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class TheStingerConfig extends CustomItemsConfigFields {
   public TheStingerConfig() {
      super(
         "the_stinger",
         true,
         Material.GOLDEN_SWORD,
         "<g:#FFD700:#FFA500>The Stinger</g>",
         new ArrayList<>(List.of("&aFloat like a butterfly,", "&asting like a bee!"))
      );
      this.setEnchantments(new ArrayList<>(List.of("SHARPNESS,1", "UNBREAKING,1", "VANISHING_CURSE,1")));
      this.setPotionEffects(List.of("POISON,0,target,onHit"));
      this.setItemType(CustomItem.ItemType.UNIQUE);
   }
}

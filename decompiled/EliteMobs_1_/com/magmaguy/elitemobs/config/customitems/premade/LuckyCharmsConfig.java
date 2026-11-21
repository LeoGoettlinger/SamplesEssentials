package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class LuckyCharmsConfig extends CustomItemsConfigFields {
   public LuckyCharmsConfig() {
      super(
         "lucky_charms",
         true,
         Material.COOKIE,
         "<g:#FFD700:#32CD32>Lucky Charms</g>",
         new ArrayList<>(List.of("&aPart of a complete breakfast!", "&cNote: Absolutely not a part of", "&ca complete breakfast."))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(List.of("LUCK,0,self,continuous"));
      this.setDropWeight("1");
   }
}

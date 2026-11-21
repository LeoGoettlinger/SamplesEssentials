package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class BerserkerCharmConfig extends CustomItemsConfigFields {
   public BerserkerCharmConfig() {
      super(
         "berserker_charm",
         true,
         Material.TOTEM_OF_UNDYING,
         "&bBerserker Charm",
         new ArrayList<>(List.of("&aLose yourself in the face of", "&aoverwhelming adversity in more", "&aways than one..."))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(new ArrayList<>(List.of("BLINDNESS,0,self,onHit", "FAST_DIGGING,0,self,onHit")));
      this.setDropWeight("1");
   }
}

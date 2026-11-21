package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ChameleonCharmConfig extends CustomItemsConfigFields {
   public ChameleonCharmConfig() {
      super(
         "chameleon_charm",
         true,
         Material.LEATHER,
         "&bChameleon Charm",
         new ArrayList<>(List.of("&aThe colorful Chameleon can", "&ablend in just about anywhere,", "&aslowly creeping around..."))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(new ArrayList<>(List.of("INVISIBILITY,0,self,onHit", "SLOW,0,self,onHit", "BLINDNESS,0,self,onHit")));
      this.setDropWeight("1");
   }
}

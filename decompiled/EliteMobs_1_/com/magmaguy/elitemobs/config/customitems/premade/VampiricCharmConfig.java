package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class VampiricCharmConfig extends CustomItemsConfigFields {
   public VampiricCharmConfig() {
      super(
         "vampiric_charm",
         true,
         Material.STICK,
         "&bVampiric Charm",
         new ArrayList<>(List.of("&aNo one can remember where this", "&acame from, and no one can,", "&aforget how dangerous it is..."))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(new ArrayList<>(List.of("HEAL,0,self,onHit", "BLINDNESS,0,self,onHit", "SLOW,0,self,onHit")));
      this.setDropWeight("1");
   }
}

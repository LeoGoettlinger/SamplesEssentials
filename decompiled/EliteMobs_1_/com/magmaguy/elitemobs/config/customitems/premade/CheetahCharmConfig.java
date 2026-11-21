package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class CheetahCharmConfig extends CustomItemsConfigFields {
   public CheetahCharmConfig() {
      super(
         "cheetah_charm",
         true,
         Material.GOLDEN_BOOTS,
         "&bCheetah Charm",
         new ArrayList<>(List.of("&aCheetahs can reach speeds of", "&a120 km/h... for about 60", "&aseconds. Make them count!"))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(new ArrayList<>(List.of("SPEED,2,self,continuous", "HUNGER,0,self,continuous")));
      this.setDropWeight("1");
   }
}

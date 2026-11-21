package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class FireflyCharmConfig extends CustomItemsConfigFields {
   public FireflyCharmConfig() {
      super("firefly_charm", true, Material.GLASS_BOTTLE, "&bFirefly Charm", List.of("&aLight up the night!"));
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(new ArrayList<>(List.of("NIGHT_VISION,0,self,continuous", "GLOWING,0,target,onHit")));
      this.setDropWeight("1");
   }
}

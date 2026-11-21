package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class FishyCharmConfig extends CustomItemsConfigFields {
   public FishyCharmConfig() {
      super("fishy_charm", true, Material.COD, "&bFishy Charm", new ArrayList<>(List.of("&aThere's just something not", "&aquite right with this one...")));
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(List.of("WATER_BREATHING,0,self,continuous"));
      this.setDropWeight("1");
   }
}

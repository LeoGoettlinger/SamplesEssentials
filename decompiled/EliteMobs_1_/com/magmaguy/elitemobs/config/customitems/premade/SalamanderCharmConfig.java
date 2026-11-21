package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class SalamanderCharmConfig extends CustomItemsConfigFields {
   public SalamanderCharmConfig() {
      super("salamander_charm", true, Material.LEATHER, "&bSalamander Charm", new ArrayList<>(List.of("&aAlmost as hot as MagmaGuy's", "&anew mixtape!")));
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(List.of("FIRE_RESISTANCE,0,self,continuous"));
      this.setDropWeight("1");
   }
}

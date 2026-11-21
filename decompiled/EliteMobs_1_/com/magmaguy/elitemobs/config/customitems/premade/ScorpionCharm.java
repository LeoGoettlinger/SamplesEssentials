package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ScorpionCharm extends CustomItemsConfigFields {
   public ScorpionCharm() {
      super(
         "scorpion_charm",
         true,
         Material.POISONOUS_POTATO,
         "&bScorpion Charm",
         new ArrayList<>(List.of("&aFloat like a butterfly,", "&asting like a bee,", "&apoison like a potato!"))
      );
      this.setEnchantments(List.of("VANISHING_CURSE,1"));
      this.setPotionEffects(List.of("POISON,0,target,onHit"));
      this.setDropWeight("1");
   }
}

package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteZoglinConfig extends MobPropertiesConfigFields {
   public EliteZoglinConfig() {
      super(
         "elite_zoglin",
         EntityType.ZOGLIN,
         true,
         "&fLvl &2$level &fElite &dZoglin",
         new ArrayList<>(List.of("$player &cmessed with the $entity &cand got the horns!", "$entity &cgot $player's &cbacon!")),
         12.0,
         40.0
      );
   }
}

package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteSpiderConfig extends MobPropertiesConfigFields {
   public EliteSpiderConfig() {
      super(
         "elite_spider",
         EntityType.SPIDER,
         true,
         "&fLvl &2$level &fElite &7Spider",
         new ArrayList<>(List.of("$player &cbecame entangled in $entity's &cweb!", "$entity &chas devoured $player&c!")),
         3.0,
         16.0
      );
   }
}

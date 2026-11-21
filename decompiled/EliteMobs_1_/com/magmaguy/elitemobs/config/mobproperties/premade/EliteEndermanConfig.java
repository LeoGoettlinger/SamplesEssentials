package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteEndermanConfig extends MobPropertiesConfigFields {
   public EliteEndermanConfig() {
      super(
         "elite_enderman",
         EntityType.ENDERMAN,
         true,
         "&fLvl &2$level &fElite &5Enderman",
         new ArrayList<>(
            List.of("$entity &csent $player &cinto the void!", "$player &clooked at $entity &cwrong!", "$player &cand $entity &cbecame best friends!")
         ),
         10.0,
         40.0
      );
   }
}

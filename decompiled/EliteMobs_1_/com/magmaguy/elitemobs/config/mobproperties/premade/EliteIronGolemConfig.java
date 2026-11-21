package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteIronGolemConfig extends MobPropertiesConfigFields {
   public EliteIronGolemConfig() {
      super(
         "elite_iron_golem",
         EntityType.IRON_GOLEM,
         true,
         "&fLvl &2$level &fElite &fIron Golem",
         new ArrayList<>(List.of("$player &cmessed with the wrong $entity&c!", "$player &chas been taught the way of fist by $entity&c!")),
         15.0,
         100.0
      );
   }
}

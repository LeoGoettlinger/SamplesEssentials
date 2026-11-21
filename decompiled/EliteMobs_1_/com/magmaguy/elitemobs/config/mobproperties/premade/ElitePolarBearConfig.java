package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class ElitePolarBearConfig extends MobPropertiesConfigFields {
   public ElitePolarBearConfig() {
      super(
         "elite_polar_bear",
         EntityType.POLAR_BEAR,
         true,
         "&fLvl &2$level &fElite &fPolar Bear",
         new ArrayList<>(List.of("$player &cwas clawed to death by $entity&c!", "$player &cwas mauled to death by $entity&c!")),
         9.0,
         30.0
      );
   }
}

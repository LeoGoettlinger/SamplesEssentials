package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class ElitePiglinConfig extends MobPropertiesConfigFields {
   public ElitePiglinConfig() {
      super(
         "elite_piglin",
         EntityType.PIGLIN,
         true,
         "&fLvl &2$level &fElite &ePiglin",
         new ArrayList<>(List.of("$entity &cwill fetch a good price for $player's remains!", "$entity &ctaught $player &cthe value of gold!")),
         13.0,
         16.0
      );
   }
}

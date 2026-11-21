package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteElderGuardianConfig extends MobPropertiesConfigFields {
   public EliteElderGuardianConfig() {
      super(
         "elite_elder_guardian",
         EntityType.ELDER_GUARDIAN,
         true,
         "&fLvl &2$level &fElite &3Elder Guardian",
         List.of("$entity &cprevented $player &cfrom exploring the depths!"),
         12.0,
         80.0
      );
   }
}

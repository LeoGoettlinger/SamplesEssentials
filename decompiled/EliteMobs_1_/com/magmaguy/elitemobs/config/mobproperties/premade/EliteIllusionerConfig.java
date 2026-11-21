package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteIllusionerConfig extends MobPropertiesConfigFields {
   public EliteIllusionerConfig() {
      super(
         "elite_illusioner",
         EntityType.ILLUSIONER,
         true,
         "&fLvl &2$level &fElite &8Illusioner",
         List.of("$player&c fell for $entity's &cillusions!"),
         5.0,
         32.0
      );
   }
}

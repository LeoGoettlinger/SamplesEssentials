package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class ElitePillagerConfig extends MobPropertiesConfigFields {
   public ElitePillagerConfig() {
      super(
         "elite_pillager",
         EntityType.PILLAGER,
         true,
         "&fLvl &2$level &fElite &8Pillager",
         new ArrayList<>(List.of("$entity &cplundered $player&c!", "$entity &cpillaged $player&c!")),
         5.0,
         24.0
      );
   }
}

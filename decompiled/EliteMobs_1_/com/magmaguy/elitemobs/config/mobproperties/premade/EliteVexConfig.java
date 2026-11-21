package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteVexConfig extends MobPropertiesConfigFields {
   public EliteVexConfig() {
      super("elite_vex", EntityType.VEX, true, "&fLvl &2$level &fElite &bVex", List.of("$entity &chas vexed $player&c!"), 13.0, 14.0);
   }
}

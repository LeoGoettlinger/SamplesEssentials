package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteWardenConfig extends MobPropertiesConfigFields {
   public EliteWardenConfig() {
      super("elite_warden", EntityType.WARDEN, true, "&fLvl &2$level &fElite &3Warden", List.of("$entity &csensed $player &cwas nearby!"), 45.0, 500.0);
   }
}

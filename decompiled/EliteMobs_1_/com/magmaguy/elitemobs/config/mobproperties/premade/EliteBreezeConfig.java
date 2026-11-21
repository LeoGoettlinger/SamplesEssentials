package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteBreezeConfig extends MobPropertiesConfigFields {
   public EliteBreezeConfig() {
      super("elite_breeze", EntityType.BREEZE, true, "&2Lvl &2$level &2Elite &eBreeze", List.of("$player &cwas blown away by $entity!"), 1.5, 30.0);
   }
}

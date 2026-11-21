package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteBoggedConfig extends MobPropertiesConfigFields {
   public EliteBoggedConfig() {
      super("elite_bogged", EntityType.BOGGED, true, "&2Lvl &2$level &2Elite &eBogged", List.of("$player &cwas poisoned by $entity!"), 5.0, 16.0);
   }
}

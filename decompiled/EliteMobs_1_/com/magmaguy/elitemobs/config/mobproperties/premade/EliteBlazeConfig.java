package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteBlazeConfig extends MobPropertiesConfigFields {
   public EliteBlazeConfig() {
      super("elite_blaze", EntityType.BLAZE, true, "&fLvl &2$level &fElite &eBlaze", List.of("$player &cwas lit ablaze by $entity!"), 9.0, 20.0);
   }
}

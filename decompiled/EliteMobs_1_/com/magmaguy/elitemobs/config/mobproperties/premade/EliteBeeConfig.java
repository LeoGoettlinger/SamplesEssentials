package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteBeeConfig extends MobPropertiesConfigFields {
   public EliteBeeConfig() {
      super("elite_bee", EntityType.BEE, true, "&fLvl &2$level &fElite &eBee", List.of("$player &cwas stung by $entity!"), 3.0, 10.0);
   }
}

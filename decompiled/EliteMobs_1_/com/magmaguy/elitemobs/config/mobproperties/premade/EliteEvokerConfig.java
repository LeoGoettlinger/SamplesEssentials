package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteEvokerConfig extends MobPropertiesConfigFields {
   public EliteEvokerConfig() {
      super("elite_evoker", EntityType.EVOKER, true, "&fLvl &2$level &fElite &5Evoker", List.of("$player &cwas enchanted by $entity&c!"), 9.0, 24.0);
   }
}

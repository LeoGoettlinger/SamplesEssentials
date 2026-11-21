package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteSlimeConfig extends MobPropertiesConfigFields {
   public EliteSlimeConfig() {
      super("elite_slime", EntityType.SLIME, true, "&2Lvl &2$level &fElite &2Slime", List.of("$player was squished by $entity&f!"), 6.0, 16.0);
   }
}

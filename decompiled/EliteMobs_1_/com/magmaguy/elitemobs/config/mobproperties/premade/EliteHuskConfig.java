package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteHuskConfig extends MobPropertiesConfigFields {
   public EliteHuskConfig() {
      super("elite_husk", EntityType.HUSK, true, "&fLvl &2$level &fElite &7Husk", List.of("$player &cwas hollowed out by $entity&c!"), 5.0, 20.0);
   }
}

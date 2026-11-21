package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteShulkerConfig extends MobPropertiesConfigFields {
   public EliteShulkerConfig() {
      super("elite_shulker", EntityType.SHULKER, true, "&fLvl &2$level &fElite &5Shulker", List.of("$entity &cshowed $player &cnew heights!"), 4.0, 30.0);
   }
}

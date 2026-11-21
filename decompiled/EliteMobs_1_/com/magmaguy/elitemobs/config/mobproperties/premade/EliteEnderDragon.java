package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteEnderDragon extends MobPropertiesConfigFields {
   public EliteEnderDragon() {
      super("elite_ender_dragon", EntityType.ENDER_DRAGON, false, "&fLvl &2$level &fElite &5Ender Dragon", List.of("$entity &chas ended $player!"), 7.0, 200.0);
   }
}

package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteSilverfishConfig extends MobPropertiesConfigFields {
   public EliteSilverfishConfig() {
      super(
         "elite_silverfish",
         EntityType.SILVERFISH,
         true,
         "&fLvl &2$level &fElite &7Silverfish",
         List.of("$player &cmistook $entity &cfor a stone block!"),
         1.0,
         8.0
      );
   }
}

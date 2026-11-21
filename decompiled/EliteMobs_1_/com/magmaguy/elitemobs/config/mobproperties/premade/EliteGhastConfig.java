package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteGhastConfig extends MobPropertiesConfigFields {
   public EliteGhastConfig() {
      super(
         "elite_ghast",
         EntityType.GHAST,
         true,
         "&fLvl &2$level &fElite &fGhast",
         new ArrayList<>(List.of("$player &cdidn't dodge $entity&c &cfireballs!", "$player &cwas blown to bits $entity&c!", "$entity &cgot $player's &cbacon!")),
         22.5,
         10.0
      );
   }
}

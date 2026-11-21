package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteWolfConfig extends MobPropertiesConfigFields {
   public EliteWolfConfig() {
      super(
         "elite_wolf",
         EntityType.WOLF,
         true,
         "[$level] Elite Wolf",
         new ArrayList<>(List.of("$entity tore $player apart!", "$player was torn to shreds by $entity!")),
         6.0,
         8.0
      );
   }
}

package com.magmaguy.elitemobs.config.mobproperties.premade;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

public class EliteWitherSkeletonConfig extends MobPropertiesConfigFields {
   public EliteWitherSkeletonConfig() {
      super(
         "elite_wither_skeleton",
         EntityType.WITHER_SKELETON,
         true,
         "&fLvl &2$level &fElite &8Wither Skeleton",
         new ArrayList<>(List.of("$entity's &carrows withered away $player&c!", "$entity's &chas withered $player &caway!")),
         12.0,
         20.0
      );
   }
}

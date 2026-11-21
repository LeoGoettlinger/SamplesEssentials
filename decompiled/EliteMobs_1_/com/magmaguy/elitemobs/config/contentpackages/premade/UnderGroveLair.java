package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class UnderGroveLair extends ContentPackagesConfigFields {
   public UnderGroveLair() {
      super(
         "under_grove_lair",
         true,
         "&2[Dynamic] &6The Under Grove",
         List.of("&6Stop the dryad's ritual!"),
         "https://nightbreak.io/plugin/elitemobs/#the-under-grove",
         ContentPackagesConfigFields.DungeonSizeCategory.LAIR,
         "em_under_grove",
         Environment.NORMAL,
         true,
         "em_under_grove,-2.5,-38.8,35.5,90,0",
         "em_under_grove,-9.5,-38.8,35.5,90,0",
         1,
         "&aDynamic Lair\n$bossCount boss encounters\n&6Stop the dryad's\n&6sinister ritual!",
         "&8[EM] &aYou are now trespassing the sacred grove!",
         "&8[EM] &aYou have left the sacred grove!",
         List.of("filename=the_under_grove_rotroot_dryad_p1.yml"),
         "the_under_grove",
         -1,
         false
      );
      this.contentType = ContentPackagesConfigFields.ContentType.DYNAMIC_DUNGEON;
      this.setDifficulties(
         List.of(
            Map.of("name", "normal", "levelSync", "+5", "id", 0),
            Map.of("name", "hard", "levelSync", "+0", "id", 1),
            Map.of("name", "mythic", "levelSync", "-5", "id", 2)
         )
      );
      this.setSetupMenuDescription(List.of("&2A Dynamic Lair where you choose the level!"));
      this.setNightbreakSlug("the-under-grove");
   }
}

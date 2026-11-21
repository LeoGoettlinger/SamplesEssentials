package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class RuinsLair extends ContentPackagesConfigFields {
   public RuinsLair() {
      super(
         "the_ruins",
         true,
         "&2[Dynamic] &6The Ruins",
         List.of("&fA fight against a myth from", "&fNorse mythology, be prepared for a smiting!", "&6Credits: 69OzCanOfBepis, MagmaGuy, Dali, Frost"),
         "https://nightbreak.io/plugin/elitemobs/#the-ruins",
         ContentPackagesConfigFields.DungeonSizeCategory.LAIR,
         "em_the_ruins",
         Environment.NORMAL,
         true,
         "em_the_ruins,-63.5,190.0,111.5,-132,0",
         "em_the_ruins,-60.5,190.0,109.5,-132,0",
         0,
         "Difficulty: &cHard\n$bossCount level $lowestTier Big Boss!\n&cA tough fight against a Norse god!",
         "&8[EM] &3Those who challenge the myths must be prepared for their downfall!",
         "&8[EM] &3You now know what it takes to make a legend!",
         List.of("filename=ruins_boss_p1.yml"),
         "the_ruins",
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
      this.setSetupMenuDescription(List.of("&2A dynamic Lair!"));
      this.setNightbreakSlug("the-ruins");
   }
}

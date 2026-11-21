package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class CatacombsLair extends ContentPackagesConfigFields {
   public CatacombsLair() {
      super(
         "catacombs_lair",
         true,
         "&2[Dynamic] &8The Catacombs",
         List.of("&fA great starter lair for players!", "&6Credits: Realm of Lotheridon, MagmaGuy, Dali, Frost"),
         "https://nightbreak.io/plugin/elitemobs/#the-catacombs",
         ContentPackagesConfigFields.DungeonSizeCategory.LAIR,
         "em_the_catacombs",
         Environment.NORMAL,
         true,
         "em_the_catacombs,41.5,65.0,82.5,176,0",
         "em_the_catacombs,42.5,66.0,75.5,155,0",
         0,
         "Difficulty: &cHard\n$bossCount bosses, from tier $lowestTier to $highestTier\n&2A great Lair challenge for groups of beginners!",
         "&8[EM] &8Now entering the Catacombs. Be careful with what dwells below...",
         "&8[EM] &8You have left the Catacombs. Was it worth it?",
         List.of("filename=catacombs_tier_15_boss.yml"),
         "the_catacombs",
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
      this.setNightbreakSlug("the-catacombs");
   }
}

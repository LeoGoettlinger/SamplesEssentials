package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class KnightCastleLair extends ContentPackagesConfigFields {
   public KnightCastleLair() {
      super(
         "knights_castle_lair",
         true,
         "&2[Dynamic] &fThe Knight's Castle",
         List.of("&fChallenge the knights of the castle!"),
         "https://nightbreak.io/plugin/elitemobs/#the-knights-castle",
         ContentPackagesConfigFields.DungeonSizeCategory.LAIR,
         "em_knight_castle",
         Environment.NORMAL,
         true,
         "em_knight_castle,-33.5,3.5,48.5,-17,0",
         "em_knight_castle,-20.5,6.2,55.5,-120,0",
         0,
         "Difficulty: &chard\n$bossCount level $lowestTier Big Boss!\n&6Face an honorable duel!",
         "&fAssault the castle!",
         "&fNow leaving the castle!",
         List.of("filename=the_castle_charlemagne_p1.yml"),
         "the_knight_castle",
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
      this.setNightbreakSlug("the-knights-castle");
   }
}

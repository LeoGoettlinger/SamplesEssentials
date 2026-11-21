package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class TheNetherBellSanctum extends ContentPackagesConfigFields {
   public TheNetherBellSanctum() {
      super(
         "the_nether_bell_sanctum",
         true,
         "&2[Dynamic] &3The Nether Bell Sanctum",
         new ArrayList<>(List.of("&fVenture into the deepest part of the Nether!", "&6Credits: Dali_, MagmaGuy, Frostcone")),
         "https://discord.gg/eSxvPbWYy4",
         ContentPackagesConfigFields.DungeonSizeCategory.SANCTUM,
         "em_id_the_nether_bell",
         Environment.NORMAL,
         true,
         "em_id_the_nether_bell,231.5,87,-219.5,44,0",
         "em_id_the_nether_bell,184.5,69,-192.5,43,0",
         0,
         "&aDynamic Sanctum\n$bossCount boss encounters\n&cVenture into the deepest\n&cpart of the Nether!",
         "&bNo being should be this deep in the Nether...",
         "&bYou have left The Nether Bell!",
         List.of("filename=em_id_the_nether_bell_boss_void_bell_p1.yml"),
         "em_id_the_nether_bell",
         -1,
         false
      );
      this.setContentType(ContentPackagesConfigFields.ContentType.DYNAMIC_DUNGEON);
      this.setDifficulties(
         List.of(
            Map.of("name", "normal", "levelSync", "+5", "id", 0),
            Map.of("name", "hard", "levelSync", "+0", "id", 1),
            Map.of("name", "mythic", "levelSync", "-5", "id", 2)
         )
      );
      this.setNightbreakSlug("story-mode-dungeons");
   }
}

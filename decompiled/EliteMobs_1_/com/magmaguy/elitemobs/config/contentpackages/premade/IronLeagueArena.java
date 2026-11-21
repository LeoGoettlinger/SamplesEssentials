package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import org.bukkit.World.Environment;

public class IronLeagueArena extends ContentPackagesConfigFields {
   public IronLeagueArena() {
      super(
         "iron_league_arena",
         true,
         "&2[lvl 050-100] &fThe Iron League Arena",
         List.of("&fFace the Iron League Arena!"),
         "https://nightbreak.io/plugin/elitemobs/#iron-league-arena",
         ContentPackagesConfigFields.DungeonSizeCategory.ARENA,
         "em_iron_league_arena",
         Environment.NORMAL,
         true,
         "em_iron_league_arena,46,-41,38,-110,-24",
         0,
         "Difficulty: &chard\nFace 50 waves of the arena, from\nlevel 50 to level 100!",
         "&fAre you prepared to face the arena?",
         "&fNow leaving the iron league arena!",
         "iron_league_arena",
         false
      );
      this.setSetupMenuDescription(List.of("&2An arena for players between levels 50-100!"));
      this.setNightbreakSlug("iron-league-arena");
   }
}

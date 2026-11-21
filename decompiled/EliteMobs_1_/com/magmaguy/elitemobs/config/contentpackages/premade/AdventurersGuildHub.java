package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import org.bukkit.World.Environment;

public class AdventurersGuildHub extends ContentPackagesConfigFields {
   public AdventurersGuildHub() {
      super(
         "adventurers_guild_hub",
         true,
         "&2Adventurers Guild Hub!",
         List.of("&aThe EliteMobs hub with shops and teleports!"),
         "https://nightbreak.io/plugin/elitemobs/#the-adventurers-guild",
         "em_adventurers_guild",
         Environment.NORMAL,
         true,
         "em_adventurers_guild,208.5,88,236.5,-80,0",
         24,
         "Difficulty: &cHard\n$bossCount level $lowestTier Big Boss!\n&6Face fierce beasts at their home turf!",
         "&8[EM] &aYou have entered the Beast Sanctuary! Beware of what prowls here!",
         "&8[EM] &aYou've left the Beast Sanctuary! Did you take trophies?",
         "em_adventurers_guild",
         false
      );
      this.setSetupMenuDescription(
         List.of(
            "&2The highly recommended Elitemobs Hub!",
            "&2It does not replace your worlds, it just",
            "&2adds an isolated world with all the NPCs",
            "&2and portals for EliteMobs!"
         )
      );
      this.setNightbreakSlug("the-adventurers-guild");
   }
}

package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;
import java.util.Map;
import org.bukkit.World.Environment;

public class EnchantmentChallenge16 extends ContentPackagesConfigFields {
   public EnchantmentChallenge16() {
      super(
         "enchantment_challenge_16_sanctum",
         true,
         "&2[lvl 000-200] &6Enchantment Challenge 16",
         List.of("&fAn enchantment challenge dungeon!"),
         "https://discord.gg/eSxvPbWYy4",
         ContentPackagesConfigFields.DungeonSizeCategory.SANCTUM,
         "em_id_enchantment_challenge_16",
         Environment.THE_END,
         true,
         "em_id_enchantment_challenge_16,-15.5,94,15.5,-135,0.0",
         "em_id_enchantment_challenge_16,14.5,65,-12.5,45,0",
         0,
         "Difficulty: &4solo hard content!",
         "&bChallenge time!",
         "&bYou have left the enchantment challenge!",
         List.of("filename=enchantment_boss_shadow_heart.yml"),
         "em_id_enchantment_challenge_16",
         1,
         false
      );
      this.setDifficulties(List.of(Map.of("name", "normal", "id", 0)));
      this.setEnchantmentChallenge(true);
      this.setMaxPlayerCount(1);
      this.setNightbreakSlug("premium-enchantment-sanctums");
   }
}

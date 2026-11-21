package com.magmaguy.elitemobs.config.skillbonuses.premade;

import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;

public class MacesDivineShieldConfig extends SkillBonusConfigFields {
   private String procMessage = "&6&lDivine Shield &esaved you from a fatal blow!";

   public MacesDivineShieldConfig() {
      super(
         "maces_divine_shield.yml",
         true,
         "&eDivine Shield",
         List.of("&7When taking fatal damage,", "&7become invulnerable briefly. 120s CD."),
         SkillType.MACES,
         2,
         2.0,
         0.02,
         0.0,
         120.0,
         Material.GOLDEN_CHESTPLATE,
         true
      );
      this.loreTemplates = List.of("&7Prevents death once", "&7Invulnerable for 2 seconds", "&7Cooldown: &f$cooldowns");
      this.formattedBonusTemplate = "Prevent Death (CD: $cooldowns)";
   }

   @Override
   public void processAdditionalFields() {
      this.procMessage = this.translatable(this.filename, "procMessage", this.processString("procMessage", this.procMessage, this.procMessage, true));
   }

   @Generated
   public String getProcMessage() {
      return this.procMessage;
   }
}

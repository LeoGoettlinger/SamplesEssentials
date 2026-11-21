package com.magmaguy.elitemobs.config.luapowers;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import java.util.List;
import lombok.Generated;

public abstract class LuaPowersConfigFields {
   private final String filename;
   private final String legacyFilename;
   private final String effect;
   private final PowersConfigFields.PowerType powerType;

   protected LuaPowersConfigFields(String baseFileName, String effect, PowersConfigFields.PowerType powerType) {
      this.filename = baseFileName + ".lua";
      this.legacyFilename = baseFileName + ".yml";
      this.effect = effect;
      this.powerType = powerType;
   }

   public abstract String getSource();

   protected static String playerDamagedPotion(String potionEffectType, int duration, int amplifier, int localCooldown, int globalCooldown) {
      String cooldownGuard = "";
      String cooldownSet = "";
      if (localCooldown > 0 || globalCooldown > 0) {
         cooldownGuard = "                if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then\n                  return\n                end\n\n";
         StringBuilder builder = new StringBuilder();
         if (localCooldown > 0) {
            builder.append("    context.cooldowns.set_local(").append(localCooldown).append(")\n");
         }

         if (globalCooldown > 0) {
            builder.append("    context.cooldowns.set_global(").append(globalCooldown).append(")\n");
         }

         cooldownSet = builder.toString();
      }

      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil then\n      return\n    end\n%s    context.player:apply_potion_effect(\"%s\", %d, %d)\n%s  end\n}\n"
         .formatted(cooldownGuard, potionEffectType, duration, amplifier, cooldownSet);
   }

   protected static String spawnSelfPotion(String potionEffectType, int duration, int amplifier) {
      return "return {\n  api_version = 1,\n  on_spawn = function(context)\n    context.boss:apply_potion_effect(\"%s\", %d, %d)\n  end\n}\n"
         .formatted(potionEffectType, duration, amplifier);
   }

   protected static String luaQuote(String value) {
      return value == null ? "\"\"" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
   }

   protected static String luaList(List<String> values) {
      StringBuilder builder = new StringBuilder("{\n");

      for (String value : values) {
         builder.append("    ").append(luaQuote(value)).append(",\n");
      }

      builder.append("  }");
      return builder.toString();
   }

   @Generated
   public String getFilename() {
      return this.filename;
   }

   @Generated
   public String getLegacyFilename() {
      return this.legacyFilename;
   }

   @Generated
   public String getEffect() {
      return this.effect;
   }

   @Generated
   public PowersConfigFields.PowerType getPowerType() {
      return this.powerType;
   }
}

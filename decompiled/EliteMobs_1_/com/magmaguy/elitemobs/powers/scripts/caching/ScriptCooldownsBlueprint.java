package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;

public class ScriptCooldownsBlueprint {
   private ScriptInteger localCooldown = new ScriptInteger(0);
   private ScriptInteger globalCooldown = new ScriptInteger(0);

   public ScriptCooldownsBlueprint(Map<?, ?> values, String scriptName, String filename) {
      if (values != null) {
         for (Entry<?, ?> entry : values.entrySet()) {
            String key = entry.getKey().toString();
            String var7 = key.toLowerCase(Locale.ROOT);
            switch (var7) {
               case "local":
                  this.localCooldown = MapListInterpreter.parseScriptInteger(key, entry.getValue(), scriptName);
                  break;
               case "global":
                  this.globalCooldown = MapListInterpreter.parseScriptInteger(key, entry.getValue(), scriptName);
                  break;
               default:
                  Logger.warn("Failed to parse cooldown entry for script name " + scriptName + " in config file " + filename);
            }
         }
      }
   }

   @Generated
   public ScriptInteger getLocalCooldown() {
      return this.localCooldown;
   }

   @Generated
   public ScriptInteger getGlobalCooldown() {
      return this.globalCooldown;
   }
}

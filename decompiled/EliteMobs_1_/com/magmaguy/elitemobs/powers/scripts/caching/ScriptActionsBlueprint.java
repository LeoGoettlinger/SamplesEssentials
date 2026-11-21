package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Generated;

public class ScriptActionsBlueprint {
   private final List<ScriptActionBlueprint> scriptActionsBlueprintList = new ArrayList<>();

   public ScriptActionsBlueprint(Map<String, Object> configurationValues, String scriptName, String filename) {
      if (configurationValues.get("Actions") instanceof List<?> values && !values.isEmpty()) {
         for (Object value : values) {
            if (value instanceof Map<?, ?> entry) {
               this.scriptActionsBlueprintList.add(new ScriptActionBlueprint(entry, scriptName, filename));
            } else {
               Logger.warn("Script " + scriptName + " in file " + filename + " has an action entry that is not a valid map.");
            }
         }
      } else {
         if (!Boolean.TRUE.equals(configurationValues.get("__lua_synthetic"))) {
            Logger.warn("Script " + scriptName + " in file " + filename + " does not have any actions! You should probably fix this.");
         }
      }
   }

   @Generated
   public List<ScriptActionBlueprint> getScriptActionsBlueprintList() {
      return this.scriptActionsBlueprintList;
   }
}

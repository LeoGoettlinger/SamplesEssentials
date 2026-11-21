package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.MemorySection;

public class ScriptRelativeVectorBlueprint {
   private final String scriptName;
   private final String scriptFilename;
   private ScriptTargetsBlueprint sourceTarget = null;
   private ScriptTargetsBlueprint destinationTarget = null;
   private boolean normalize = false;
   private ScriptFloat multiplier = new ScriptFloat(1.0F);
   private ScriptVector offset = new ScriptVector(new ScriptFloat(0.0F), new ScriptFloat(0.0F), new ScriptFloat(0.0F));

   public ScriptRelativeVectorBlueprint(String scriptName, String scriptFilename, Map<String, ?> configurationValues) {
      this.scriptName = scriptName;
      this.scriptFilename = scriptFilename;
      configurationValues.entrySet().forEach(entry -> this.processKeyAndValue(entry.getKey(), entry.getValue()));
   }

   public ScriptRelativeVectorBlueprint(String scriptName, String scriptFilename, LinkedHashMap<String, ?> configurationValues) {
      this.scriptName = scriptName;
      this.scriptFilename = scriptFilename;
      configurationValues.entrySet().forEach(entry -> this.processKeyAndValue(entry.getKey(), entry.getValue()));
   }

   protected void processKeyAndValue(String key, Object value) {
      String var3 = key.toLowerCase(Locale.ROOT);
      switch (var3) {
         case "sourcetarget":
            if (value instanceof MemorySection) {
               this.sourceTarget = new ScriptTargetsBlueprint(((MemorySection)value).getValues(false), this.scriptName, this.scriptFilename);
            } else {
               this.sourceTarget = new ScriptTargetsBlueprint((Map<?, ?>)value, this.scriptName, this.scriptFilename);
            }
            break;
         case "destinationtarget":
            if (value instanceof MemorySection) {
               this.destinationTarget = new ScriptTargetsBlueprint(((MemorySection)value).getValues(false), this.scriptName, this.scriptFilename);
            } else {
               this.destinationTarget = new ScriptTargetsBlueprint((Map<?, ?>)value, this.scriptName, this.scriptFilename);
            }
            break;
         case "multiplier":
            this.multiplier = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "normalize":
            this.normalize = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "offset":
            this.offset = MapListInterpreter.parseScriptVector(key, value, this.scriptName);
            break;
         default:
            Logger.warn("Failed to read key " + key + " for script " + this.scriptName + " in " + this.scriptFilename);
      }
   }

   @Generated
   public ScriptTargetsBlueprint getSourceTarget() {
      return this.sourceTarget;
   }

   @Generated
   public ScriptTargetsBlueprint getDestinationTarget() {
      return this.destinationTarget;
   }

   @Generated
   public boolean isNormalize() {
      return this.normalize;
   }

   @Generated
   public ScriptFloat getMultiplier() {
      return this.multiplier;
   }

   @Generated
   public ScriptVector getOffset() {
      return this.offset;
   }
}

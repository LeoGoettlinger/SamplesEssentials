package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.configuration.MemorySection;

public class ScriptTargetsBlueprint {
   private final String filename;
   private final String scriptName;
   protected TargetType targetType = TargetType.SELF;
   private List<String> locations;
   private String location;
   private ScriptVector offset = new ScriptVector(new ScriptFloat(0.0F), new ScriptFloat(0.0F), new ScriptFloat(0.0F));
   private ScriptFloat range = new ScriptFloat(20.0F);
   private boolean track = true;
   private ScriptFloat coverage = new ScriptFloat(1.0F);
   private boolean isCustomCoverage = false;
   private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint = null;

   public ScriptTargetsBlueprint(Map<?, ?> entry, String scriptName, String filename) {
      this.scriptName = scriptName;
      this.filename = filename;
      this.processMapList(entry);
      if (!this.isZoneTarget() && this.coverage.getValue().floatValue() < 1.0) {
         Logger.warn(
            "Coverage for script "
               + scriptName
               + " in file "
               + filename
               + " was less than 1.0 but the targetType is neither ZONE_FULL nor ZONE_BORDER! Coverage should only be used for ZONE_FULL or ZONE_BORDER"
         );
         this.coverage = new ScriptFloat(1.0F);
      }
   }

   public void setCoverage(float coverage) {
      this.coverage = new ScriptFloat(coverage);
   }

   public boolean isZoneTarget() {
      return this.targetType == TargetType.ZONE_FULL
         || this.targetType == TargetType.ZONE_BORDER
         || this.targetType == TargetType.INHERIT_SCRIPT_ZONE_FULL
         || this.targetType == TargetType.INHERIT_SCRIPT_ZONE_BORDER;
   }

   private void processMapList(Map<?, ?> entry) {
      for (Entry entrySet : entry.entrySet()) {
         String key = (String)entrySet.getKey();
         this.processKeyAndValue(key, entrySet.getValue());
      }
   }

   protected void processKeyAndValue(String key, Object value) {
      String var3 = key.toLowerCase(Locale.ROOT);
      switch (var3) {
         case "location":
            this.location = MapListInterpreter.parseString(key, value, this.scriptName);
            break;
         case "locations":
            this.locations = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "targettype":
            this.targetType = MapListInterpreter.parseEnum(key, value, TargetType.class, this.scriptName);
            break;
         case "range":
            this.range = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "offset":
            this.offset = MapListInterpreter.parseScriptVector(key, value, this.scriptName);
            break;
         case "track":
            this.track = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "coverage":
            this.isCustomCoverage = true;
            this.coverage = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "relativeoffset":
            if (value instanceof MemorySection) {
               this.scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(this.scriptName, this.filename, ((MemorySection)value).getValues(false));
            } else if (value instanceof LinkedHashMap) {
               this.scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(this.scriptName, this.filename, (LinkedHashMap<String, ?>)value);
            } else {
               Logger.warn("Failed to get valid format for relative offset in " + this.scriptName + " for file " + this.filename);
            }
      }
   }

   @Generated
   public String getScriptName() {
      return this.scriptName;
   }

   @Generated
   public TargetType getTargetType() {
      return this.targetType;
   }

   @Generated
   public List<String> getLocations() {
      return this.locations;
   }

   @Generated
   public String getLocation() {
      return this.location;
   }

   @Generated
   public ScriptVector getOffset() {
      return this.offset;
   }

   @Generated
   public ScriptFloat getRange() {
      return this.range;
   }

   @Generated
   public boolean isTrack() {
      return this.track;
   }

   @Generated
   public void setTrack(boolean track) {
      this.track = track;
   }

   @Generated
   public ScriptFloat getCoverage() {
      return this.coverage;
   }

   @Generated
   public boolean isCustomCoverage() {
      return this.isCustomCoverage;
   }

   @Generated
   public ScriptRelativeVectorBlueprint getScriptRelativeVectorBlueprint() {
      return this.scriptRelativeVectorBlueprint;
   }
}

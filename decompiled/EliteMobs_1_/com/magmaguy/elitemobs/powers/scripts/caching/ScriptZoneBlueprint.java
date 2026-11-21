package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.enums.Filter;
import com.magmaguy.elitemobs.powers.scripts.enums.ShapeType;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.configuration.MemorySection;

public class ScriptZoneBlueprint {
   private final String scriptName;
   private final String filename;
   private ScriptTargetsBlueprint target = null;
   private ScriptTargetsBlueprint finalTarget = null;
   private ScriptTargetsBlueprint target2 = null;
   private ScriptTargetsBlueprint finalTarget2 = null;
   private ShapeType shapeTypeEnum = ShapeType.CYLINDER;
   private ScriptFloat height = new ScriptFloat(1.0F);
   private Filter filter = Filter.PLAYER;
   private ScriptFloat radius = new ScriptFloat(5.0F);
   private ScriptFloat borderRadius = new ScriptFloat(1.0F);
   private ScriptFloat pointRadius = new ScriptFloat(0.5F);
   private ScriptFloat pitchRotation = new ScriptFloat(0.0F);
   private ScriptFloat yawRotation = new ScriptFloat(0.0F);
   private ScriptFloat pitchPreRotation = new ScriptFloat(0.0F);
   private ScriptFloat yawPreRotation = new ScriptFloat(0.0F);
   private ScriptInteger animationDuration = new ScriptInteger(0);
   private ScriptFloat x = new ScriptFloat(0.0F);
   private ScriptFloat y = new ScriptFloat(0.0F);
   private ScriptFloat z = new ScriptFloat(0.0F);
   private ScriptFloat xBorder = new ScriptFloat(0.0F);
   private ScriptFloat yBorder = new ScriptFloat(0.0F);
   private ScriptFloat zBorder = new ScriptFloat(0.0F);
   private boolean ignoresSolidBlocks = true;

   public ScriptZoneBlueprint(Map<String, Object> configurationValues, String scriptName, String filename) {
      this.scriptName = scriptName;
      this.filename = filename;
      if (configurationValues.get("Zone") instanceof Map<?, ?> values) {
         this.processMapList(values);
         if (this.target == null) {
            this.target = new ScriptTargetsBlueprint(new HashMap(), scriptName, filename);
         }
      }
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
         case "shape":
            this.shapeTypeEnum = MapListInterpreter.parseEnum(key, value, ShapeType.class, this.scriptName);
            break;
         case "filter":
            this.filter = MapListInterpreter.parseEnum(key, value, Filter.class, this.scriptName);
            break;
         case "height":
            this.height = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "radius":
            this.radius = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "borderradius":
            this.borderRadius = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "pointradius":
            this.pointRadius = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "pitchrotation":
            this.pitchRotation = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "pitchprerotation":
            this.pitchPreRotation = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "yawrotation":
            this.yawRotation = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "yawprerotation":
            this.yawPreRotation = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "target":
            this.target = this.createTargetsBlueprint(value);
            break;
         case "finaltarget":
            this.finalTarget = this.createTargetsBlueprint(value);
            break;
         case "target2":
            this.target2 = this.createTargetsBlueprint(value);
            break;
         case "finaltarget2":
            this.finalTarget2 = this.createTargetsBlueprint(value);
            break;
         case "animationduration":
            this.animationDuration = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "ignoressolidblocks":
            this.ignoresSolidBlocks = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "x":
            this.x = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "y":
            this.y = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "z":
            this.z = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "xborder":
            this.xBorder = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "yborder":
            this.yBorder = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "zborder":
            this.zBorder = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         default:
            Logger.warn("Failed to read key " + key + " for script " + this.scriptName + " in file " + this.filename);
      }
   }

   private ScriptTargetsBlueprint createTargetsBlueprint(Object value) {
      if (value instanceof MemorySection memorySection) {
         return new ScriptTargetsBlueprint(memorySection.getValues(false), this.scriptName, this.filename);
      } else if (value instanceof Map<?, ?> map) {
         return new ScriptTargetsBlueprint(map, this.scriptName, this.filename);
      } else {
         Logger.warn("Failed to read target section for script " + this.scriptName + " in file " + this.filename);
         return null;
      }
   }

   @Generated
   public ScriptTargetsBlueprint getTarget() {
      return this.target;
   }

   @Generated
   public ScriptTargetsBlueprint getFinalTarget() {
      return this.finalTarget;
   }

   @Generated
   public ScriptTargetsBlueprint getTarget2() {
      return this.target2;
   }

   @Generated
   public ScriptTargetsBlueprint getFinalTarget2() {
      return this.finalTarget2;
   }

   @Generated
   public ShapeType getShapeTypeEnum() {
      return this.shapeTypeEnum;
   }

   @Generated
   public ScriptFloat getHeight() {
      return this.height;
   }

   @Generated
   public Filter getFilter() {
      return this.filter;
   }

   @Generated
   public ScriptFloat getRadius() {
      return this.radius;
   }

   @Generated
   public ScriptFloat getBorderRadius() {
      return this.borderRadius;
   }

   @Generated
   public ScriptFloat getPointRadius() {
      return this.pointRadius;
   }

   @Generated
   public ScriptFloat getPitchRotation() {
      return this.pitchRotation;
   }

   @Generated
   public ScriptFloat getYawRotation() {
      return this.yawRotation;
   }

   @Generated
   public ScriptFloat getPitchPreRotation() {
      return this.pitchPreRotation;
   }

   @Generated
   public ScriptFloat getYawPreRotation() {
      return this.yawPreRotation;
   }

   @Generated
   public ScriptInteger getAnimationDuration() {
      return this.animationDuration;
   }

   @Generated
   public ScriptFloat getX() {
      return this.x;
   }

   @Generated
   public ScriptFloat getY() {
      return this.y;
   }

   @Generated
   public ScriptFloat getZ() {
      return this.z;
   }

   @Generated
   public ScriptFloat getXBorder() {
      return this.xBorder;
   }

   @Generated
   public ScriptFloat getYBorder() {
      return this.yBorder;
   }

   @Generated
   public ScriptFloat getZBorder() {
      return this.zBorder;
   }

   @Generated
   public boolean isIgnoresSolidBlocks() {
      return this.ignoresSolidBlocks;
   }
}

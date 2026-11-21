package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.powers.scripts.enums.WeatherType;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.PotionEffectTypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.MemorySection;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ScriptActionBlueprint {
   private final String scriptName;
   private final String scriptFilename;
   private ActionType actionType = null;
   private ScriptInteger duration = new ScriptInteger(0);
   private ScriptInteger wait = new ScriptInteger(0);
   private PotionEffectType potionEffectType;
   private ScriptInteger amplifier;
   private List<String> scripts = new ArrayList<>();
   private List<String> landingScripts = new ArrayList<>();
   private ScriptConditionsBlueprint conditionsBlueprint;
   private ScriptInteger times = new ScriptInteger(-1);
   private ScriptInteger repeatEvery = new ScriptInteger(0);
   private ScriptParticlesBlueprint scriptParticlesBlueprint = new ScriptParticlesBlueprint();
   private ScriptFloat multiplier = new ScriptFloat(1.0F);
   private Material material = Material.TARGET;
   private ScriptFloat amount = new ScriptFloat(1.0F);
   private Boolean bValue = null;
   private String sValue = "";
   private Vector vValue = null;
   private ScriptTargetsBlueprint scriptTargets;
   private ScriptTargetsBlueprint finalTarget = null;
   private String title = "";
   private String subtitle = "";
   private BarColor barColor = BarColor.WHITE;
   private BarStyle barStyle = BarStyle.SOLID;
   private ScriptInteger fadeIn = new ScriptInteger(1);
   private ScriptInteger fadeOut = new ScriptInteger(1);
   private boolean flicker = true;
   private boolean withTrail = true;
   private Type fireworkEffectType = Type.BALL_LARGE;
   private List<Type> fireworkEffectTypes = null;
   private List<List<ScriptActionBlueprint.FireworkColor>> fireworkEffects = new ArrayList<>();
   private ScriptInteger power = new ScriptInteger(10);
   private boolean invulnerable = true;
   private ScriptVector offset = new ScriptVector(new ScriptFloat(0.0F), new ScriptFloat(0.0F), new ScriptFloat(0.0F));
   private List<String> tags;
   private ScriptInteger time = new ScriptInteger(0);
   private WeatherType weatherType = WeatherType.CLEAR;
   private boolean onlyRunOneScript = false;
   private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint = null;
   private ScriptFloat volume = new ScriptFloat(1.0F);
   private ScriptFloat pitch = new ScriptFloat(1.0F);
   private ScriptFloat velocity = new ScriptFloat(1.0F);
   private ScriptFloat scale = new ScriptFloat(1.0F);
   private boolean debug = false;

   public ScriptActionBlueprint(Map<?, ?> entry, String scriptName, String scriptFilename) {
      this.scriptName = scriptName;
      this.scriptFilename = scriptFilename;
      this.processMapList(entry);
      if (this.conditionsBlueprint == null) {
         this.conditionsBlueprint = new ScriptConditionsBlueprint((Map<?, ?>)null, scriptName, scriptFilename);
      }

      if (this.scriptTargets == null) {
         this.scriptTargets = new ScriptTargetsBlueprint(entry, scriptName, scriptFilename);
      }

      if (this.actionType == ActionType.SPAWN_PARTICLE
         && this.scriptTargets.isZoneTarget()
         && this.scriptTargets.getCoverage().getValue().floatValue() == 1.0
         && !this.scriptTargets.isCustomCoverage()) {
         this.scriptTargets.setCoverage(0.3F);
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
         case "duration":
            this.duration = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "wait":
            this.wait = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "amplifier":
            this.amplifier = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "action":
            this.actionType = MapListInterpreter.parseEnum(key, value, ActionType.class, this.scriptName);
            break;
         case "potioneffecttype":
            try {
               this.potionEffectType = PotionEffectTypeUtil.getByKey(((String)value).toLowerCase(Locale.ROOT));
            } catch (Exception var6) {
               Logger.warn("Invalid potion effect type " + value + " in file " + this.scriptFilename + " for script " + this.scriptName + " !");
            }
            break;
         case "scripts":
            this.scripts = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "landingscripts":
            this.landingScripts = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "conditions":
            if (this.conditionsBlueprint != null) {
               throw new IllegalArgumentException("Duplicate conditions section in script " + this.scriptName + " in " + this.scriptFilename);
            }

            this.conditionsBlueprint = new ScriptConditionsBlueprint((Map<?, ?>)value, this.scriptName, this.scriptFilename);
            break;
         case "times":
            this.times = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "repeatevery":
            this.repeatEvery = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "particles":
            this.scriptParticlesBlueprint = new ScriptParticlesBlueprint((List<Map<?, ?>>)value, this.scriptName, this.scriptFilename);
            break;
         case "multiplier":
            this.multiplier = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "material":
            this.material = MapListInterpreter.parseEnum(key, value, Material.class, this.scriptName);
            break;
         case "amount":
            this.amount = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "bvalue":
            this.bValue = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "svalue":
            this.sValue = MapListInterpreter.parseString(key, value, this.scriptName);
            break;
         case "vvalue":
            this.vValue = MapListInterpreter.parseVector(key, value, this.scriptName);
            break;
         case "title":
            this.title = MapListInterpreter.parseString(key, value, this.scriptName);
            break;
         case "subtitle":
            this.subtitle = MapListInterpreter.parseString(key, value, this.scriptName);
            break;
         case "barcolor":
            this.barColor = MapListInterpreter.parseEnum(key, value, BarColor.class, this.scriptName);
            break;
         case "barstyle":
            this.barStyle = MapListInterpreter.parseEnum(key, value, BarStyle.class, this.scriptName);
            break;
         case "fadein":
            this.fadeIn = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "fadeout":
            this.fadeOut = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "flicker":
            this.flicker = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "withtrail":
            this.withTrail = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "fireworkeffecttype":
            this.fireworkEffectType = MapListInterpreter.parseEnum(key, value, Type.class, this.scriptName);
            break;
         case "fireworkeffecttypes":
            this.fireworkEffectTypes = MapListInterpreter.parseEnumList(key, value, Type.class, this.scriptName);
            break;
         case "fireworkeffects":
            this.fireworkEffects = MapListInterpreter.parseEnumListList(key, value, ScriptActionBlueprint.FireworkColor.class, this.scriptName);
            break;
         case "power":
            this.power = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "invulnerable":
            this.invulnerable = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "offset":
            this.offset = MapListInterpreter.parseScriptVector(key, value, this.scriptName);
            break;
         case "tags":
            this.tags = MapListInterpreter.parseStringList(key, value, this.scriptName);
            break;
         case "time":
            this.time = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
            break;
         case "weather":
            this.weatherType = MapListInterpreter.parseEnum(key, value, WeatherType.class, this.scriptName);
            break;
         case "target":
            if (value instanceof MemorySection) {
               this.scriptTargets = new ScriptTargetsBlueprint(((MemorySection)value).getValues(false), this.scriptName, this.scriptFilename);
            } else if (value instanceof String stringValue) {
               this.scriptTargets = new ScriptTargetsBlueprint(Map.of("targetType", stringValue), this.scriptName, this.scriptFilename);
            } else {
               this.scriptTargets = new ScriptTargetsBlueprint((Map<?, ?>)value, this.scriptName, this.scriptFilename);
            }
            break;
         case "finaltarget":
            if (value instanceof MemorySection) {
               this.finalTarget = new ScriptTargetsBlueprint(((MemorySection)value).getValues(false), this.scriptName, this.scriptFilename);
            } else if (value instanceof String stringValue) {
               this.finalTarget = new ScriptTargetsBlueprint(Map.of("targetType", stringValue), this.scriptName, this.scriptFilename);
            } else {
               this.finalTarget = new ScriptTargetsBlueprint((Map<?, ?>)value, this.scriptName, this.scriptFilename);
            }
            break;
         case "onlyrunonescript":
            this.onlyRunOneScript = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         case "relativevector":
            this.scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(this.scriptName, this.scriptFilename, (Map<String, ?>)value);
            break;
         case "pitch":
            this.pitch = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "volume":
            this.volume = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "velocity":
            this.velocity = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "scale":
            this.scale = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
            break;
         case "debug":
            this.debug = MapListInterpreter.parseBoolean(key, value, this.scriptName);
            break;
         default:
            Logger.warn("Failed to read key " + key + " for script " + this.scriptName + " in " + this.scriptFilename);
      }
   }

   @Generated
   public String getScriptName() {
      return this.scriptName;
   }

   @Generated
   public String getScriptFilename() {
      return this.scriptFilename;
   }

   @Generated
   public ActionType getActionType() {
      return this.actionType;
   }

   @Generated
   public ScriptInteger getDuration() {
      return this.duration;
   }

   @Generated
   public ScriptInteger getWait() {
      return this.wait;
   }

   @Generated
   public PotionEffectType getPotionEffectType() {
      return this.potionEffectType;
   }

   @Generated
   public ScriptInteger getAmplifier() {
      return this.amplifier;
   }

   @Generated
   public List<String> getScripts() {
      return this.scripts;
   }

   @Generated
   public List<String> getLandingScripts() {
      return this.landingScripts;
   }

   @Generated
   public ScriptConditionsBlueprint getConditionsBlueprint() {
      return this.conditionsBlueprint;
   }

   @Generated
   public ScriptInteger getTimes() {
      return this.times;
   }

   @Generated
   public ScriptInteger getRepeatEvery() {
      return this.repeatEvery;
   }

   @Generated
   public ScriptParticlesBlueprint getScriptParticlesBlueprint() {
      return this.scriptParticlesBlueprint;
   }

   @Generated
   public ScriptFloat getMultiplier() {
      return this.multiplier;
   }

   @Generated
   public Material getMaterial() {
      return this.material;
   }

   @Generated
   public ScriptFloat getAmount() {
      return this.amount;
   }

   @Generated
   public Boolean getBValue() {
      return this.bValue;
   }

   @Generated
   public String getSValue() {
      return this.sValue;
   }

   @Generated
   public Vector getVValue() {
      return this.vValue;
   }

   @Generated
   public ScriptTargetsBlueprint getScriptTargets() {
      return this.scriptTargets;
   }

   @Generated
   public ScriptTargetsBlueprint getFinalTarget() {
      return this.finalTarget;
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public String getSubtitle() {
      return this.subtitle;
   }

   @Generated
   public BarColor getBarColor() {
      return this.barColor;
   }

   @Generated
   public BarStyle getBarStyle() {
      return this.barStyle;
   }

   @Generated
   public ScriptInteger getFadeIn() {
      return this.fadeIn;
   }

   @Generated
   public ScriptInteger getFadeOut() {
      return this.fadeOut;
   }

   @Generated
   public boolean isFlicker() {
      return this.flicker;
   }

   @Generated
   public boolean isWithTrail() {
      return this.withTrail;
   }

   @Generated
   public Type getFireworkEffectType() {
      return this.fireworkEffectType;
   }

   @Generated
   public List<Type> getFireworkEffectTypes() {
      return this.fireworkEffectTypes;
   }

   @Generated
   public List<List<ScriptActionBlueprint.FireworkColor>> getFireworkEffects() {
      return this.fireworkEffects;
   }

   @Generated
   public ScriptInteger getPower() {
      return this.power;
   }

   @Generated
   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   @Generated
   public ScriptVector getOffset() {
      return this.offset;
   }

   @Generated
   public List<String> getTags() {
      return this.tags;
   }

   @Generated
   public ScriptInteger getTime() {
      return this.time;
   }

   @Generated
   public WeatherType getWeatherType() {
      return this.weatherType;
   }

   @Generated
   public boolean isOnlyRunOneScript() {
      return this.onlyRunOneScript;
   }

   @Generated
   public ScriptRelativeVectorBlueprint getScriptRelativeVectorBlueprint() {
      return this.scriptRelativeVectorBlueprint;
   }

   @Generated
   public ScriptFloat getVolume() {
      return this.volume;
   }

   @Generated
   public ScriptFloat getPitch() {
      return this.pitch;
   }

   @Generated
   public ScriptFloat getVelocity() {
      return this.velocity;
   }

   @Generated
   public ScriptFloat getScale() {
      return this.scale;
   }

   @Generated
   public boolean isDebug() {
      return this.debug;
   }

   public static enum FireworkColor {
      WHITE(Color.WHITE),
      SILVER(Color.SILVER),
      GRAY(Color.GRAY),
      BLACK(Color.BLACK),
      RED(Color.RED),
      MAROON(Color.MAROON),
      YELLOW(Color.YELLOW),
      OLIVE(Color.OLIVE),
      LIME(Color.LIME),
      GREEN(Color.GREEN),
      AQUA(Color.AQUA),
      TEAL(Color.TEAL),
      BLUE(Color.BLUE),
      NAVY(Color.NAVY),
      FUCHSIA(Color.FUCHSIA),
      PURPLE(Color.PURPLE),
      ORANGE(Color.ORANGE);

      private final Color color;

      private FireworkColor(Color color) {
         this.color = color;
      }

      @Generated
      public Color getColor() {
         return this.color;
      }
   }
}

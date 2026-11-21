package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class ScriptParticlesBlueprint {
   private final List<ScriptParticlesBlueprint.ScriptParticleBlueprint> particleBlueprints = new ArrayList<>();

   public ScriptParticlesBlueprint(List<Map<?, ?>> entries, String scriptName, String filename) {
      for (Map<?, ?> entry : entries) {
         this.particleBlueprints.add(new ScriptParticlesBlueprint.ScriptParticleBlueprint(entry, scriptName, filename));
      }
   }

   public ScriptParticlesBlueprint() {
   }

   @Generated
   public List<ScriptParticlesBlueprint.ScriptParticleBlueprint> getParticleBlueprints() {
      return this.particleBlueprints;
   }

   public class ScriptParticleBlueprint {
      private final String scriptName;
      private final String filename;
      private ScriptFloat x = new ScriptFloat(0.01F);
      private ScriptFloat y = new ScriptFloat(0.01F);
      private ScriptFloat z = new ScriptFloat(0.01F);
      private ScriptInteger amount = new ScriptInteger(1);
      private ScriptInteger red = new ScriptInteger(255);
      private ScriptInteger green = new ScriptInteger(255);
      private ScriptInteger blue = new ScriptInteger(255);
      private ScriptInteger toRed = new ScriptInteger(255);
      private ScriptInteger toGreen = new ScriptInteger(255);
      private ScriptInteger toBlue = new ScriptInteger(255);
      private Particle particle = Particle.FLAME;
      private Material material = Material.STONE;
      private ScriptFloat speed = new ScriptFloat(0.01F);
      private Boolean moveToTarget = null;
      private ScriptRelativeVectorBlueprint relativeVectorBlueprint = null;
      private transient Class<?> cachedDataType = null;
      private transient BlockData cachedBlockData = null;
      private transient ItemStack cachedItemStack = null;

      public ScriptParticleBlueprint(Map<?, ?> entry, String scriptName, String filename) {
         this.filename = filename;
         this.scriptName = scriptName;
         this.processMapList(entry);
      }

      public Class<?> getDataType() {
         if (this.cachedDataType == null) {
            this.cachedDataType = this.particle.getDataType();
         }

         return this.cachedDataType;
      }

      public BlockData getBlockData() {
         if (this.cachedBlockData == null) {
            Material blockMaterial = this.material;
            if (blockMaterial == null || !blockMaterial.isBlock()) {
               blockMaterial = Material.STONE;
            }

            this.cachedBlockData = blockMaterial.createBlockData();
         }

         return this.cachedBlockData;
      }

      public ItemStack getItemStack() {
         if (this.cachedItemStack == null) {
            Material itemMaterial = this.material;
            if (itemMaterial == null || !itemMaterial.isItem() || itemMaterial.isAir()) {
               itemMaterial = Material.STONE;
            }

            this.cachedItemStack = new ItemStack(itemMaterial);
         }

         return this.cachedItemStack;
      }

      private void processMapList(Map<?, ?> entry) {
         for (Entry entrySet : entry.entrySet()) {
            String key = (String)entrySet.getKey();
            this.processKeyAndValue(key, entrySet.getValue());
         }
      }

      private void processKeyAndValue(String key, Object value) {
         String var3 = key.toLowerCase(Locale.ROOT);
         switch (var3) {
            case "x":
               this.x = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
               break;
            case "y":
               this.y = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
               break;
            case "z":
               this.z = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
               break;
            case "amount":
               this.amount = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "particle":
               this.particle = MapListInterpreter.parseEnum(key, value, Particle.class, this.scriptName);
               break;
            case "material":
               this.material = MapListInterpreter.parseEnum(key, value, Material.class, this.scriptName);
               break;
            case "speed":
               this.speed = MapListInterpreter.parseScriptFloat(key, value, this.scriptName);
               break;
            case "red":
               this.red = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "green":
               this.green = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "blue":
               this.blue = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "tored":
               this.toRed = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "togreen":
               this.toGreen = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "toblue":
               this.toBlue = MapListInterpreter.parseScriptInteger(key, value, this.scriptName);
               break;
            case "movetotarget":
               this.moveToTarget = MapListInterpreter.parseBoolean(key, value, this.scriptName);
               break;
            case "relativevector":
               this.relativeVectorBlueprint = new ScriptRelativeVectorBlueprint(this.scriptName, this.filename, (Map<String, ?>)value);
               break;
            default:
               Logger.warn("Key " + key + " in script " + this.scriptName + " in file " + this.filename + " for script particles is not a valid key!");
         }
      }

      @Generated
      public String getScriptName() {
         return this.scriptName;
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
      public ScriptInteger getAmount() {
         return this.amount;
      }

      @Generated
      public ScriptInteger getRed() {
         return this.red;
      }

      @Generated
      public ScriptInteger getGreen() {
         return this.green;
      }

      @Generated
      public ScriptInteger getBlue() {
         return this.blue;
      }

      @Generated
      public ScriptInteger getToRed() {
         return this.toRed;
      }

      @Generated
      public ScriptInteger getToGreen() {
         return this.toGreen;
      }

      @Generated
      public ScriptInteger getToBlue() {
         return this.toBlue;
      }

      @Generated
      public Particle getParticle() {
         return this.particle;
      }

      @Generated
      public Material getMaterial() {
         return this.material;
      }

      @Generated
      public ScriptFloat getSpeed() {
         return this.speed;
      }

      @Generated
      public Boolean getMoveToTarget() {
         return this.moveToTarget;
      }

      @Generated
      public ScriptRelativeVectorBlueprint getRelativeVectorBlueprint() {
         return this.relativeVectorBlueprint;
      }
   }
}

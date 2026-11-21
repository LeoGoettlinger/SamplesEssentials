package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptConditionsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ConditionType;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScriptConditions {
   private final ScriptConditionsBlueprint conditionsBlueprint;
   private final ScriptRuntimeOwner runtimeOwner;
   private final ScriptTargets scriptTargets;

   public ScriptConditions(ScriptConditionsBlueprint scriptConditionsBlueprint, ScriptRuntimeOwner runtimeOwner, boolean actionCondition) {
      this.conditionsBlueprint = scriptConditionsBlueprint;
      this.runtimeOwner = runtimeOwner;
      if (this.conditionsBlueprint.getScriptTargets() != null) {
         this.scriptTargets = new ScriptTargets(this.conditionsBlueprint.getScriptTargets(), runtimeOwner);
      } else {
         this.scriptTargets = null;
      }

      if (this.conditionsBlueprint.getConditionType() == null) {
         if (actionCondition) {
            this.conditionsBlueprint.setConditionType(ConditionType.FILTERING);
         } else {
            this.conditionsBlueprint.setConditionType(ConditionType.BLOCKING);
         }
      }
   }

   private boolean eval(boolean actual, Boolean expected, Boolean runIf) {
      if (expected == null) {
         return true;
      } else {
         boolean matches = actual == expected;
         return runIf == null ? matches : matches == runIf;
      }
   }

   private boolean isAliveCheck(LivingEntity livingEntity) {
      Boolean expected = this.conditionsBlueprint.getIsAlive();
      if (expected == null) {
         return true;
      } else if (livingEntity == null) {
         return false;
      } else {
         boolean actual = livingEntity.isValid();
         return this.eval(actual, expected, this.conditionsBlueprint.getRunIfIsAliveIs());
      }
   }

   private boolean hasTagsCheck(LivingEntity target) {
      List<String> tags = this.conditionsBlueprint.getHasTags();
      if (tags == null) {
         return true;
      } else {
         boolean actual = this.tagsUninverted(target, tags);
         return this.eval(actual, Boolean.TRUE, this.conditionsBlueprint.getRunIfHasTagIs());
      }
   }

   private boolean doesNotHaveTags(LivingEntity target) {
      List<String> tags = this.conditionsBlueprint.getDoesNotHaveTags();
      if (tags == null) {
         return true;
      } else {
         boolean actual = this.tagsUninverted(target, tags);
         return this.eval(actual, Boolean.FALSE, this.conditionsBlueprint.getRunIfDoesNotHaveTagIs());
      }
   }

   private boolean tagsUninverted(LivingEntity target, List<String> blueprintTags) {
      List<String> entityTags = null;

      try {
         if (target instanceof Player player) {
            ElitePlayerInventory inv = ElitePlayerInventory.getPlayer(player);
            if (inv != null) {
               entityTags = new ArrayList<>(inv.getTags());
            }
         } else {
            EliteEntity mob = EntityTracker.getEliteMobEntity(target);
            if (mob != null) {
               entityTags = new ArrayList<>(mob.getTags());
            }
         }
      } catch (Exception var6) {
         Logger.warn("Failed to retrieve tags for entity '" + target.getName() + "': " + var6.getMessage());
         return false;
      }

      return entityTags == null ? false : entityTags.containsAll(blueprintTags);
   }

   private boolean targetCountLowerThan(int targetCount) {
      Integer threshold = this.conditionsBlueprint.getTargetCountLowerThan();
      if (threshold == null) {
         return true;
      } else {
         boolean actual = targetCount < threshold;
         return this.eval(actual, Boolean.TRUE, this.conditionsBlueprint.getRunIfTargetCountLowerThanIs());
      }
   }

   private boolean targetCountGreaterThan(int targetCount) {
      Integer threshold = this.conditionsBlueprint.getTargetCountGreaterThan();
      if (threshold == null) {
         return true;
      } else {
         boolean actual = targetCount > threshold;
         return this.eval(actual, Boolean.TRUE, this.conditionsBlueprint.getRunIfTargetCountGreaterThanIs());
      }
   }

   private boolean checkRandomizer() {
      Double chance = this.conditionsBlueprint.getRandomChance();
      if (chance == null) {
         return true;
      } else {
         boolean actual = ThreadLocalRandom.current().nextDouble() < chance;
         return this.eval(actual, Boolean.TRUE, this.conditionsBlueprint.getRunIfRandomChanceIs());
      }
   }

   private boolean isAirCheck(Location targetLocation) {
      Boolean expected = this.conditionsBlueprint.getLocationIsAir();
      if (expected == null) {
         return true;
      } else {
         try {
            boolean actual = targetLocation.getBlock().getType().isAir();
            return this.eval(actual, expected, this.conditionsBlueprint.getRunIfLocationIsAirIs());
         } catch (Exception var4) {
            Logger.warn("Failed to check if location is air at '" + targetLocation + "': " + var4.getMessage());
            return false;
         }
      }
   }

   private boolean isOnFloor(Location targetLocation) {
      Boolean expected = this.conditionsBlueprint.getIsOnFloor();
      if (expected == null) {
         return true;
      } else {
         try {
            Block current = targetLocation.getBlock();
            Block below = targetLocation.clone().subtract(0.0, 1.0, 0.0).getBlock();
            boolean actual = !current.getType().isSolid() && below.getType().isSolid();
            return this.eval(actual, expected, this.conditionsBlueprint.getRunIfIsOnFloorIs());
         } catch (Exception var6) {
            Logger.warn("Failed to check if location is on floor at '" + targetLocation + "': " + var6.getMessage());
            return false;
         }
      }
   }

   private boolean isStandingOn(Location targetLocation) {
      Material mat = this.conditionsBlueprint.getIsStandingOnMaterial();
      if (mat == null) {
         return true;
      } else {
         try {
            boolean actual = targetLocation.clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == mat;
            return this.eval(actual, Boolean.TRUE, this.conditionsBlueprint.getRunIfIsStandingOnMaterialIs());
         } catch (Exception var4) {
            Logger.warn("Failed to check if location is standing on material at '" + targetLocation + "': " + var4.getMessage());
            return false;
         }
      }
   }

   public boolean meetsPreActionConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
      if (this.scriptTargets == null) {
         return true;
      } else {
         ScriptActionData data = new ScriptActionData(eliteEntity, directTarget, this.scriptTargets, this.runtimeOwner.getScriptZone(), null);
         Collection<LivingEntity> entities = this.scriptTargets.getTargetEntities(data);
         int count = entities.size();
         if (this.targetCountLowerThan(count) && this.targetCountGreaterThan(count)) {
            for (LivingEntity e : entities) {
               if (!this.checkConditions(e)) {
                  return false;
               }
            }

            for (Location loc : this.scriptTargets.getTargetLocations(data, entities)) {
               if (!this.checkConditions(loc)) {
                  return false;
               }
            }

            return this.checkRandomizer();
         } else {
            return false;
         }
      }
   }

   public boolean meetsActionConditions(ScriptActionData data) {
      if (this.scriptTargets == null) {
         return true;
      } else if (data == null) {
         Logger.warn("ScriptActionData is null in meetsActionConditions.");
         return false;
      } else if (this.scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.SELF) && !this.isAliveCheck(data.getEliteEntity().getLivingEntity())
         )
       {
         return false;
      } else if (!this.conditionsBlueprint.getConditionType().equals(ConditionType.BLOCKING)) {
         return true;
      } else {
         Collection<LivingEntity> entities = this.scriptTargets.getTargetEntities(data);
         int count = entities.size();
         if (this.targetCountLowerThan(count) && this.targetCountGreaterThan(count)) {
            for (LivingEntity e : entities) {
               if (!this.checkConditions(e)) {
                  return false;
               }
            }

            for (Location loc : this.scriptTargets.getTargetLocations(data, entities)) {
               if (!this.checkConditions(loc)) {
                  return false;
               }
            }

            return this.checkRandomizer();
         } else {
            return false;
         }
      }
   }

   private boolean checkConditions(LivingEntity livingEntity) {
      if (this.scriptTargets == null) {
         return true;
      } else {
         return livingEntity == null
            ? false
            : this.isAliveCheck(livingEntity)
               && this.hasTagsCheck(livingEntity)
               && this.checkConditions(livingEntity.getLocation())
               && this.doesNotHaveTags(livingEntity);
      }
   }

   private boolean checkConditions(Location location) {
      if (this.scriptTargets == null) {
         return true;
      } else {
         return location == null ? true : this.isAirCheck(location) && this.isOnFloor(location) && this.isStandingOn(location);
      }
   }

   protected Collection<Location> validateLocations(ScriptActionData data, @NotNull Collection<Location> original) {
      if (this.scriptTargets == null) {
         return original;
      } else if (data == null) {
         Logger.warn("ScriptActionData is null in validateLocations.");
         return original;
      } else {
         Collection<Location> targets = this.scriptTargets.getTargetLocations(data);
         if (this.scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            original.removeIf(locx -> !this.checkConditions(locx));
            return original;
         } else if (targets == null) {
            return Collections.emptyList();
         } else {
            for (Location loc : targets) {
               if (!this.checkConditions(loc)) {
                  return Collections.emptyList();
               }
            }

            return original;
         }
      }
   }

   protected Collection<LivingEntity> validateEntities(ScriptActionData data, @NotNull Collection<LivingEntity> original) {
      if (this.scriptTargets == null) {
         return original;
      } else if (data == null) {
         Logger.warn("ScriptActionData is null in validateEntities.");
         return original;
      } else {
         if (this.scriptTargets.getTargetBlueprint().getTargetType().equals(TargetType.ACTION_TARGET)) {
            original.removeIf(ent -> !this.checkConditions(ent));
         } else {
            for (LivingEntity e : this.scriptTargets.getTargetEntities(data)) {
               if (!this.checkConditions(e)) {
                  return new ArrayList<>();
               }
            }
         }

         return original;
      }
   }
}

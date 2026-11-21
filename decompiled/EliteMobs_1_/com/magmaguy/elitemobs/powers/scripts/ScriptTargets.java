package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ScriptTargets {
   private final ScriptTargetsBlueprint targetBlueprint;
   private final ScriptRuntimeOwner runtimeOwner;

   public ScriptTargets(ScriptTargetsBlueprint targetBlueprint, ScriptRuntimeOwner runtimeOwner) {
      this.targetBlueprint = targetBlueprint;
      this.runtimeOwner = runtimeOwner;
   }

   public List<?> getAnonymousTargets(boolean locations, ScriptActionData scriptActionData) {
      List<?> inherited = scriptActionData.inheritedTargets(this);
      if (inherited != null) {
         return inherited;
      } else {
         return locations ? this.getTargetLocations(scriptActionData).stream().toList() : this.getTargetEntities(scriptActionData).stream().toList();
      }
   }

   public void setAnonymousTargets(List<?> targets, ScriptActionData data) {
      if (!this.targetBlueprint.isTrack() && !this.animatedZone(data)) {
         data.captureInheritedTargets(this, targets);
      }
   }

   private boolean animatedZone(ScriptActionData data) {
      ScriptZone zone = this.targetBlueprint.isZoneTarget() ? this.resolveZoneData(data).getScriptZone() : this.getScriptZone();
      return zone != null && zone.isValid() && zone.getZoneBlueprint().getAnimationDuration().getValue() > 0;
   }

   public Location processLocationFromString(EliteEntity eliteEntity, String locationString, ScriptActionData scriptActionData) {
      if (locationString == null) {
         Logger.warn("Failed to get location target in script " + this.targetBlueprint.getScriptName() + " in " + this.runtimeOwner.getFileName());
         return null;
      } else {
         Location parsedLocation = ConfigurationLocation.serialize(locationString);
         if (parsedLocation.getWorld() == null && locationString.split(",")[0].equalsIgnoreCase("same_as_boss")) {
            parsedLocation.setWorld(eliteEntity.getLocation().getWorld());
         }

         if (parsedLocation.getWorld() == null && eliteEntity.getLocation() != null && eliteEntity.getLocation().getWorld() != null) {
            World bossWorld = eliteEntity.getLocation().getWorld();
            String configuredWorldName = ConfigurationLocation.worldName(locationString);
            if (configuredWorldName != null && !configuredWorldName.isBlank() && bossWorld.getName().matches(Pattern.quote(configuredWorldName) + "_\\d+")) {
               parsedLocation.setWorld(bossWorld);
            }
         }

         return this.addOffsets(parsedLocation, scriptActionData);
      }
   }

   protected void cacheTargets(ScriptActionData data) {
      boolean animated = this.animatedZone(data);
      if (!this.targetBlueprint.isTrack() || animated) {
         ScriptZone zone = this.getScriptZone();
         if (zone != null && zone.isValid() && data.getShapesCachedByTarget() == null) {
            data.setShapesCachedByTarget(zone.generateShapes(data, true));
         }

         if (!animated) {
            data.captureLocations(this, this.getTargetLocations(data));
         }
      }
   }

   public Collection<LivingEntity> getTargetEntities(ScriptActionData scriptActionData) {
      Location eliteEntityLocation = scriptActionData.getEliteEntity().getLocation();
      if (this.targetBlueprint == null) {
         Logger.warn(
            "An action tried to run with an invalid target! Check which on it is by reading the startup logs and fix it! No target will be acquired for now."
         );
         return new ArrayList<>();
      } else {
         LivingEntity selfEntity = scriptActionData.getEliteEntity().getUnsyncedLivingEntity();
         UUID selfUUID = selfEntity == null ? null : selfEntity.getUniqueId();
         boolean eliteLocationGone = eliteEntityLocation == null || eliteEntityLocation.getWorld() == null;
         switch (this.targetBlueprint.getTargetType()) {
            case ALL_PLAYERS:
               return new ArrayList<>(Bukkit.getOnlinePlayers());
            case WORLD_PLAYERS:
               if (eliteLocationGone) {
                  return new ArrayList<>();
               }

               return new ArrayList<>(eliteEntityLocation.getWorld().getPlayers());
            case NEARBY_PLAYERS:
               if (eliteLocationGone) {
                  return new ArrayList<>();
               }

               return eliteEntityLocation.getWorld()
                  .getNearbyEntities(
                     eliteEntityLocation,
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     entityx -> entityx.getType() == EntityType.PLAYER
                  )
                  .stream()
                  .map(Player.class::cast)
                  .collect(Collectors.toSet());
            case NEARBY_MOBS:
               if (eliteLocationGone) {
                  return new ArrayList<>();
               }

               return eliteEntityLocation.getWorld()
                  .getNearbyEntities(
                     eliteEntityLocation,
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     entityx -> entityx.getType() != EntityType.PLAYER
                        && entityx instanceof LivingEntity
                        && (selfUUID == null || !entityx.getUniqueId().equals(selfUUID))
                  )
                  .stream()
                  .map(LivingEntity.class::cast)
                  .collect(Collectors.toSet());
            case NEARBY_ELITES:
               if (eliteLocationGone) {
                  return new ArrayList<>();
               }

               return eliteEntityLocation.getWorld()
                  .getNearbyEntities(
                     eliteEntityLocation,
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     this.targetBlueprint.getRange().getValue().floatValue(),
                     entityx -> EntityTracker.isEliteMob(entityx) && (selfUUID == null || !entityx.getUniqueId().equals(selfUUID))
                  )
                  .stream()
                  .map(LivingEntity.class::cast)
                  .collect(Collectors.toSet());
            case DIRECT_TARGET:
               return scriptActionData.getDirectTarget() == null ? new ArrayList<>() : new ArrayList<>(List.of(scriptActionData.getDirectTarget()));
            case SELF:
            case SELF_SPAWN:
               if (selfEntity == null) {
                  return new ArrayList<>();
               }

               return new ArrayList<>(List.of(selfEntity));
            case ZONE_FULL:
            case ZONE_BORDER:
            case INHERIT_SCRIPT_ZONE_FULL:
            case INHERIT_SCRIPT_ZONE_BORDER:
               ScriptActionData zoneData = this.resolveZoneData(scriptActionData);
               return zoneData.getScriptZone().getZoneEntities(zoneData, this.zoneTargetType());
            case INHERIT_SCRIPT_TARGET:
               ScriptActionData parent = this.requireParent(scriptActionData);
               List<?> inherited = parent.getScriptTargets().getAnonymousTargets(false, parent);
               List<LivingEntity> entities = new ArrayList<>();

               for (Object target : inherited) {
                  if (!(target instanceof LivingEntity entity)) {
                     throw new IllegalStateException("INHERIT_SCRIPT_TARGET requires entities in " + this.targetBlueprint.getScriptName());
                  }

                  entities.add(entity);
               }

               return entities;
            case LOCATION:
            case LOCATIONS:
            case LANDING_LOCATION:
            case ACTION_TARGET:
               return new ArrayList<>();
            default:
               throw new IllegalStateException("Invalid target type in " + this.targetBlueprint.getScriptName());
         }
      }
   }

   public Collection<Location> getTargetLocations(ScriptActionData scriptActionData) {
      return this.getTargetLocations(scriptActionData, null);
   }

   Collection<Location> getTargetLocations(ScriptActionData scriptActionData, Collection<LivingEntity> entities) {
      List<Location> frozen = scriptActionData.locationSnapshot(this);
      if (frozen != null) {
         return frozen;
      } else {
         switch (this.getTargetBlueprint().getTargetType()) {
            case ALL_PLAYERS:
            case WORLD_PLAYERS:
            case NEARBY_PLAYERS:
            case NEARBY_MOBS:
            case NEARBY_ELITES:
            case DIRECT_TARGET:
            case SELF:
               return (entities == null ? this.getTargetEntities(scriptActionData) : entities)
                  .stream()
                  .map(targetEntity -> this.addOffsets(targetEntity.getLocation(), scriptActionData))
                  .collect(Collectors.toSet());
            case SELF_SPAWN:
               return new ArrayList<>(List.of(this.addOffsets(scriptActionData.getEliteEntity().getSpawnLocation(), scriptActionData)));
            case ZONE_FULL:
            case ZONE_BORDER:
            case INHERIT_SCRIPT_ZONE_FULL:
            case INHERIT_SCRIPT_ZONE_BORDER:
               Collection<Location> newLocations = this.getLocationFromZone(scriptActionData);
               if (this.targetBlueprint.getCoverage().getValue() < 1.0F) {
                  newLocations.removeIf(targetLocation -> ThreadLocalRandom.current().nextDouble() > this.targetBlueprint.getCoverage().getValue().floatValue());
               }

               return newLocations;
            case INHERIT_SCRIPT_TARGET:
               ScriptActionData parent = this.requireParent(scriptActionData);
               List<?> inherited = parent.getScriptTargets().getAnonymousTargets(true, parent);
               List<Location> locations = new ArrayList<>();

               for (Object target : inherited) {
                  if (target instanceof Location location) {
                     locations.add(location.clone());
                  } else {
                     if (!(target instanceof LivingEntity entity)) {
                        throw new IllegalStateException("Invalid inherited target in " + this.targetBlueprint.getScriptName());
                     }

                     locations.add(entity.getLocation());
                  }
               }

               return locations;
            case LOCATION:
               return new ArrayList<>(List.of(this.getLocation(scriptActionData.getEliteEntity(), scriptActionData)));
            case LOCATIONS:
               return this.getLocations(scriptActionData.getEliteEntity(), scriptActionData);
            case LANDING_LOCATION:
               return new ArrayList<>(List.of(scriptActionData.getLandingLocation().clone()));
            case ACTION_TARGET:
               return new ArrayList<>();
            default:
               throw new IllegalStateException("Invalid target type in " + this.targetBlueprint.getScriptName());
         }
      }
   }

   private Collection<Location> getLocationFromZone(ScriptActionData data) {
      ScriptActionData zoneData = this.resolveZoneData(data);
      return this.addOffsets(zoneData.getScriptZone().getZoneLocations(zoneData, this.zoneTargetType()), data);
   }

   ScriptActionData resolveZoneData(ScriptActionData data) {
      ScriptActionData resolved = switch (this.targetBlueprint.getTargetType()) {
         case INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER -> this.requireParent(data);
         default -> data;
      };
      if (resolved.getScriptZone() != null && resolved.getScriptZone().isValid()) {
         return resolved;
      } else {
         throw new IllegalStateException("No valid zone for " + this.targetBlueprint.getTargetType() + " in script " + this.targetBlueprint.getScriptName());
      }
   }

   private ScriptActionData requireParent(ScriptActionData data) {
      ScriptActionData parent = data.getInheritedScriptActionData();
      if (parent == null) {
         throw new IllegalStateException(
            "No parent invocation for " + this.targetBlueprint.getTargetType() + " in script " + this.targetBlueprint.getScriptName()
         );
      } else {
         return parent;
      }
   }

   private TargetType zoneTargetType() {
      return switch (this.targetBlueprint.getTargetType()) {
         case ZONE_FULL, INHERIT_SCRIPT_ZONE_FULL -> TargetType.ZONE_FULL;
         case ZONE_BORDER, INHERIT_SCRIPT_ZONE_BORDER -> TargetType.ZONE_BORDER;
         default -> throw new IllegalStateException("Not a zone target");
      };
   }

   private ScriptZone getScriptZone() {
      return this.runtimeOwner.getScriptZone();
   }

   private Collection<Location> getLocations(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
      return this.targetBlueprint
         .getLocations()
         .stream()
         .map(rawLocation -> this.processLocationFromString(eliteEntity, rawLocation, scriptActionData))
         .collect(Collectors.toSet());
   }

   private Location getLocation(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
      return this.processLocationFromString(eliteEntity, this.targetBlueprint.getLocation(), scriptActionData);
   }

   private Location addOffsets(Location originalLocation, ScriptActionData scriptActionData) {
      Location location = originalLocation.clone().add(this.targetBlueprint.getOffset().getValue());
      if (this.targetBlueprint.getScriptRelativeVectorBlueprint() != null) {
         location.add(
            new ScriptRelativeVector(this.targetBlueprint.getScriptRelativeVectorBlueprint(), this.runtimeOwner, location).getVector(scriptActionData)
         );
      }

      return location;
   }

   private Collection<Location> addOffsets(Collection<Location> locations, ScriptActionData scriptActionData) {
      return locations.stream().map(location -> this.addOffsets(location, scriptActionData)).collect(Collectors.toCollection(ArrayList::new));
   }

   @Generated
   public ScriptTargetsBlueprint getTargetBlueprint() {
      return this.targetBlueprint;
   }

   @Generated
   public ScriptRuntimeOwner getRuntimeOwner() {
      return this.runtimeOwner;
   }
}

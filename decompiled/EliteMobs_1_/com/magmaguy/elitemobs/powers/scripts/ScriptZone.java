package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cone;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cuboid;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cylinder;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Dome;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Sphere;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.shapes.RotatingRay;
import com.magmaguy.elitemobs.utils.shapes.StaticRay;
import com.magmaguy.elitemobs.utils.shapes.TranslatingRay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class ScriptZone {
   private static final double MAX_LOCAL_QUERY_EXTENT = 256.0;
   private final ScriptZoneBlueprint zoneBlueprint;
   private final ScriptTargets targets;
   private final boolean isValid;
   private ScriptTargets finalTargets = null;
   private ScriptTargets targets2 = null;
   private ScriptTargets finalTargets2 = null;
   private boolean zoneListener = false;
   private Set<LivingEntity> entitiesInZone;
   private ScriptZone.ZoneListenerTask zoneListenerTask = null;

   public ScriptZone(ScriptZoneBlueprint zoneBlueprint, ScriptRuntimeOwner runtimeOwner) {
      this.zoneBlueprint = zoneBlueprint;
      this.targets = new ScriptTargets(zoneBlueprint.getTarget(), runtimeOwner);
      if (zoneBlueprint.getFinalTarget() != null) {
         this.finalTargets = new ScriptTargets(zoneBlueprint.getFinalTarget(), runtimeOwner);
      }

      if (zoneBlueprint.getTarget2() != null) {
         this.targets2 = new ScriptTargets(zoneBlueprint.getTarget2(), runtimeOwner);
      }

      if (zoneBlueprint.getFinalTarget2() != null) {
         this.finalTargets2 = new ScriptTargets(zoneBlueprint.getFinalTarget2(), runtimeOwner);
      }

      this.isValid = zoneBlueprint.getTarget() != null;
   }

   public void startZoneListener(EliteEntity eliteEntity) {
      if (this.zoneListener) {
         if (this.zoneListenerTask != null) {
            this.zoneListenerTask.cancel();
         }

         this.entitiesInZone = new HashSet<>();
         ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, this.targets, this);
         this.zoneListenerTask = new ScriptZone.ZoneListenerTask(eliteEntity, scriptActionData);
         this.zoneListenerTask.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
      }
   }

   public void shutdown() {
      if (this.zoneListenerTask != null) {
         this.zoneListenerTask.cancel();
         this.zoneListenerTask = null;
      }

      if (this.entitiesInZone != null) {
         this.entitiesInZone.clear();
      }
   }

   public void zoneEnterEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
      new EventCaller(new ScriptZoneEnterEvent(eliteEntity, livingEntity));
   }

   public void zoneLeaveEvent(EliteEntity eliteEntity, LivingEntity livingEntity) {
      new EventCaller(new ScriptZoneLeaveEvent(eliteEntity, livingEntity));
   }

   protected Collection<LivingEntity> getZoneEntities(ScriptActionData data, TargetType targetType) {
      return this.getEntitiesInArea(this.generateShapes(data, false), targetType);
   }

   protected Collection<Location> getZoneLocations(ScriptActionData data, TargetType targetType) {
      return this.consolidateLists(
         this.generateShapes(data, false)
            .stream()
            .map(shape -> targetType == TargetType.ZONE_FULL ? shape.getLocations() : shape.getEdgeLocations())
            .collect(Collectors.toSet())
      );
   }

   boolean originWorldLost(ScriptActionData data) {
      if (data.getShapesCachedByTarget() != null) {
         for (Shape shape : data.getShapesCachedByTarget()) {
            Location center = shape.getCenter();
            if (center == null || center.getWorld() == null) {
               return true;
            }
         }
      } else {
         for (Location origin : this.targets.getTargetLocations(data)) {
            if (origin == null || origin.getWorld() == null) {
               return true;
            }
         }
      }

      return false;
   }

   private Collection<Location> consolidateLists(Collection<Collection<Location>> originalLocations) {
      Collection<Location> parsedLocations = new ArrayList<>();
      originalLocations.forEach(parsedLocations::addAll);
      return parsedLocations;
   }

   public List<Shape> generateShapes(ScriptActionData scriptActionData, boolean force) {
      try {
         if (!force && scriptActionData.getShapesCachedByTarget() != null) {
            return scriptActionData.getShapesCachedByTarget();
         } else {
            List<Shape> shapes = new ArrayList<>();

            for (Location shapeTargetLocation : this.targets.getTargetLocations(scriptActionData)) {
               switch (this.zoneBlueprint.getShapeTypeEnum()) {
                  case CYLINDER:
                     shapes.add(
                        new Cylinder(
                           shapeTargetLocation,
                           this.zoneBlueprint.getRadius().getValue().floatValue(),
                           this.zoneBlueprint.getHeight().getValue().floatValue(),
                           this.zoneBlueprint.getBorderRadius().getValue().floatValue()
                        )
                     );
                     break;
                  case SPHERE:
                     shapes.add(
                        new Sphere(
                           this.zoneBlueprint.getRadius().getValue().floatValue(),
                           shapeTargetLocation,
                           this.zoneBlueprint.getBorderRadius().getValue().floatValue()
                        )
                     );
                     break;
                  case DOME:
                     shapes.add(
                        new Dome(
                           this.zoneBlueprint.getRadius().getValue().floatValue(),
                           shapeTargetLocation,
                           this.zoneBlueprint.getBorderRadius().getValue().floatValue()
                        )
                     );
                     break;
                  case CONE:
                     if (this.targets2 == null) {
                        Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a CONE but no set target2 for the cone!");
                     } else {
                        List<Location> target2Locations = new ArrayList<>(this.targets2.getTargetLocations(scriptActionData));
                        if (!target2Locations.isEmpty()) {
                           Location secondPoint = target2Locations.get(0);
                           shapes.add(
                              new Cone(
                                 shapeTargetLocation,
                                 secondPoint,
                                 this.zoneBlueprint.getRadius().getValue().floatValue(),
                                 this.zoneBlueprint.getBorderRadius().getValue().floatValue()
                              )
                           );
                        }
                     }
                     break;
                  case STATIC_RAY:
                     if (this.targets2 == null) {
                        Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a STATIC_RAY but no set target2 for the ray!");
                     } else {
                        for (Location locationx : this.targets2.getTargetLocations(scriptActionData)) {
                           if (this.rayLocationValidator(shapeTargetLocation, locationx)) {
                              shapes.add(
                                 new StaticRay(
                                    this.zoneBlueprint.isIgnoresSolidBlocks(),
                                    this.zoneBlueprint.getPointRadius().getValue().floatValue(),
                                    shapeTargetLocation,
                                    locationx
                                 )
                              );
                           }
                        }
                     }
                     break;
                  case ROTATING_RAY:
                     if (this.targets2 == null) {
                        Logger.warn("Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a ROTATING_RAY but no set target2 for the ray!");
                     } else {
                        for (Location target2Location : this.targets2.getTargetLocations(scriptActionData)) {
                           if (this.rayLocationValidator(shapeTargetLocation, target2Location)) {
                              shapes.add(
                                 new RotatingRay(
                                    this.zoneBlueprint.isIgnoresSolidBlocks(),
                                    this.zoneBlueprint.getPointRadius().getValue().floatValue(),
                                    shapeTargetLocation,
                                    target2Location,
                                    this.zoneBlueprint.getPitchPreRotation().getValue().floatValue(),
                                    this.zoneBlueprint.getYawPreRotation().getValue().floatValue(),
                                    this.zoneBlueprint.getPitchRotation().getValue().floatValue(),
                                    this.zoneBlueprint.getYawRotation().getValue().floatValue(),
                                    this.zoneBlueprint.getAnimationDuration().getValue()
                                 )
                              );
                           }
                        }
                     }
                     break;
                  case TRANSLATING_RAY:
                     if (this.targets2 == null) {
                        Logger.warn(
                           "Script for boss '" + scriptActionData.getEliteEntity().getName() + "' has a TRANSLATING_RAY but no set target2 for the ray!"
                        );
                     } else {
                        Location targetLocationEnd = null;
                        if (this.finalTargets != null) {
                           List<Location> finalTargetsList = new ArrayList<>(this.finalTargets.getTargetLocations(scriptActionData));
                           if (!finalTargetsList.isEmpty()) {
                              targetLocationEnd = finalTargetsList.get(0);
                           }
                        }

                        Location target2LocationEnd = null;
                        if (this.finalTargets2 != null) {
                           List<Location> finalTargetsList = new ArrayList<>(this.finalTargets2.getTargetLocations(scriptActionData));
                           if (!finalTargetsList.isEmpty()) {
                              target2LocationEnd = finalTargetsList.get(0);
                           }
                        }

                        for (Location location : this.targets2.getTargetLocations(scriptActionData)) {
                           if (this.rayLocationValidator(shapeTargetLocation, location)) {
                              shapes.add(
                                 new TranslatingRay(
                                    this.zoneBlueprint.isIgnoresSolidBlocks(),
                                    this.zoneBlueprint.getPointRadius().getValue().floatValue(),
                                    shapeTargetLocation,
                                    targetLocationEnd,
                                    location,
                                    target2LocationEnd,
                                    this.zoneBlueprint.getAnimationDuration().getValue()
                                 )
                              );
                           }
                        }
                     }
                     break;
                  case CUBOID:
                     shapes.add(
                        new Cuboid(
                           this.zoneBlueprint.getX().getValue(),
                           this.zoneBlueprint.getY().getValue(),
                           this.zoneBlueprint.getZ().getValue(),
                           this.zoneBlueprint.getXBorder().getValue(),
                           this.zoneBlueprint.getYBorder().getValue(),
                           this.zoneBlueprint.getZBorder().getValue(),
                           shapeTargetLocation
                        )
                     );
                     break;
                  default:
                     Logger.warn("Unsupported shape type '" + this.zoneBlueprint.getShapeTypeEnum() + "' in script zone.");
               }
            }

            return shapes;
         }
      } catch (Exception var10) {
         Logger.warn("Error generating shapes: " + var10.getMessage());
         var10.printStackTrace();
         return Collections.emptyList();
      }
   }

   private boolean rayLocationValidator(Location location1, Location location2) {
      return location1 != null && location2 != null && location1.getWorld().equals(location2.getWorld());
   }

   private Collection<LivingEntity> getEntitiesInArea(List<Shape> shapes, TargetType targetType) {
      Set<LivingEntity> validatedEntities = new HashSet<>();
      Map<World, Collection<? extends LivingEntity>> candidatesByWorld = new HashMap<>();

      for (Shape shape : shapes) {
         Collection<? extends LivingEntity> livingEntities = this.localCandidates(shape);
         if (livingEntities == null) {
            livingEntities = candidatesByWorld.computeIfAbsent(shape.getCenter().getWorld(), world -> {
               Collection var10000;
               if (this.zoneBlueprint.getFilter() != null) {
                  switch (this.zoneBlueprint.getFilter()) {
                     case PLAYER:
                        var10000 = this.filterByPlayer(shape.getCenter());
                        break;
                     case ELITE:
                        var10000 = this.filterByElite(shape.getCenter());
                        break;
                     case LIVING:
                        var10000 = this.filterByLiving(shape.getCenter());
                        break;
                     default:
                        throw new MatchException(null, null);
                  }
               } else {
                  var10000 = this.filterByLiving(shape.getCenter());
               }

               return var10000;
            });
         }

         for (LivingEntity livingEntity : livingEntities) {
            boolean contains = targetType.equals(TargetType.ZONE_FULL) ? shape.contains(livingEntity) : shape.borderContains(livingEntity);
            if (contains) {
               validatedEntities.add(livingEntity);
            }
         }
      }

      return validatedEntities;
   }

   private Collection<LivingEntity> localCandidates(Shape shape) {
      World world = shape.getCenter().getWorld();
      BoundingBox bounds = shape.getEntityQueryBounds();
      if (world != null && bounds != null && !(bounds.getWidthX() > 256.0) && !(bounds.getHeight() > 256.0) && !(bounds.getWidthZ() > 256.0)) {
         double minX = Math.nextDown(bounds.getMinX());
         double minY = Math.nextDown(bounds.getMinY());
         double minZ = Math.nextDown(bounds.getMinZ());
         double maxX = Math.nextUp(bounds.getMaxX());
         double maxY = Math.nextUp(bounds.getMaxY());
         double maxZ = Math.nextUp(bounds.getMaxZ());
         if (Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(minZ) && Double.isFinite(maxX) && Double.isFinite(maxY) && Double.isFinite(maxZ)
            )
          {
            BoundingBox queryBounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
            return world.getNearbyEntities(queryBounds, entity -> entity instanceof LivingEntity)
               .stream()
               .map(entity -> (LivingEntity)entity)
               .filter(entity -> {
                  if (this.zoneBlueprint.getFilter() != null) {
                     switch (this.zoneBlueprint.getFilter()) {
                        case PLAYER:
                           if (entity instanceof Player) {
                              return true;
                           }
                           break;
                        case ELITE:
                           if (EntityTracker.getEliteMobEntity(entity) != null) {
                              return true;
                           }
                           break;
                        case LIVING:
                           return true;
                        default:
                           throw new MatchException(null, null);
                     }

                     return false;
                  } else {
                     return true;
                  }
               })
               .toList();
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Collection<LivingEntity> filterByPlayer(Location center) {
      World world = center.getWorld();
      if (world != null) {
         return new ArrayList<>(world.getPlayers());
      } else {
         Logger.warn("World is null in filterByPlayer.");
         return Collections.emptyList();
      }
   }

   private Collection<LivingEntity> filterByElite(Location center) {
      World world = center.getWorld();
      if (world != null) {
         return world.getLivingEntities().stream().filter(entity -> EntityTracker.getEliteMobEntity(entity) != null).collect(Collectors.toList());
      } else {
         Logger.warn("World is null in filterByElite.");
         return Collections.emptyList();
      }
   }

   private Collection<LivingEntity> filterByLiving(Location center) {
      World world = center.getWorld();
      if (world != null) {
         return world.getLivingEntities();
      } else {
         Logger.warn("World is null in filterByLiving.");
         return Collections.emptyList();
      }
   }

   @Generated
   public ScriptZoneBlueprint getZoneBlueprint() {
      return this.zoneBlueprint;
   }

   @Generated
   public boolean isValid() {
      return this.isValid;
   }

   @Generated
   public void setZoneListener(boolean zoneListener) {
      this.zoneListener = zoneListener;
   }

   private class ZoneListenerTask extends BukkitRunnable {
      private final EliteEntity eliteEntity;
      private final ScriptActionData scriptActionData;

      public ZoneListenerTask(EliteEntity eliteEntity, ScriptActionData scriptActionData) {
         this.eliteEntity = eliteEntity;
         this.scriptActionData = scriptActionData;
      }

      public void run() {
         try {
            if (this.eliteEntity.getLivingEntity() == null || !this.eliteEntity.getLivingEntity().isValid()) {
               this.cancel();
               return;
            }

            if (this.eliteEntity.getLivingEntity().getLocation().getWorld() == null) {
               this.cancel();
               return;
            }

            Collection<LivingEntity> newEntities = ScriptZone.this.getEntitiesInArea(
               ScriptZone.this.generateShapes(this.scriptActionData, false), TargetType.ZONE_FULL
            );

            for (LivingEntity livingEntity : newEntities) {
               if (!ScriptZone.this.entitiesInZone.contains(livingEntity)) {
                  ScriptZone.this.zoneEnterEvent(this.eliteEntity, livingEntity);
               }
            }

            for (LivingEntity livingEntityx : ScriptZone.this.entitiesInZone) {
               if (!newEntities.contains(livingEntityx)) {
                  ScriptZone.this.zoneLeaveEvent(this.eliteEntity, livingEntityx);
               }
            }

            ScriptZone.this.entitiesInZone = new HashSet<>(newEntities);
         } catch (Exception var4) {
            Logger.warn("Error in ZoneListenerTask: " + var4.getMessage());
            this.cancel();
         }
      }
   }
}

package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

public class ScriptActionData {
   private final Map<ScriptTargets, List<Location>> locationSnapshots = new IdentityHashMap<>();
   private final Map<ScriptTargets, List<?>> inheritedTargets = new IdentityHashMap<>();
   private final TargetType targetType;
   private final ScriptTargets scriptTargets;
   private Collection<Location> locations = null;
   private EliteEntity eliteEntity;
   private LivingEntity directTarget = null;
   private Location landingLocation = null;
   private ScriptZone scriptZone = null;
   private ScriptActionData inheritedScriptActionData = null;
   private Event event;
   private Collection<LivingEntity> previousEntityTargets;
   private Collection<Location> previousLocationTargets;
   private List<Shape> shapesCachedByTarget;

   List<Location> locationSnapshot(ScriptTargets targets) {
      List<Location> snapshot = this.locationSnapshots.get(targets);
      return snapshot == null ? null : snapshot.stream().<Location>map(Location::clone).collect(Collectors.toCollection(ArrayList::new));
   }

   void captureLocations(ScriptTargets targets, Collection<Location> locations) {
      this.locationSnapshots.putIfAbsent(targets, locations.stream().<Location>map(Location::clone).toList());
   }

   List<?> inheritedTargets(ScriptTargets targets) {
      List<?> snapshot = this.inheritedTargets.get(targets);
      return snapshot == null ? null : copyTargets(snapshot);
   }

   void captureInheritedTargets(ScriptTargets targets, List<?> values) {
      this.inheritedTargets.put(targets, copyTargets(values));
   }

   private static List<?> copyTargets(List<?> targets) {
      return targets.stream().map(target -> target instanceof Location location ? location.clone() : target).collect(Collectors.toCollection(ArrayList::new));
   }

   boolean originatesFromDeath() {
      for (ScriptActionData data = this; data != null; data = data.inheritedScriptActionData) {
         if (data.event != null) {
            return data.event instanceof EliteMobDeathEvent;
         }
      }

      return false;
   }

   boolean repeatsActionInChain() {
      for (ScriptActionData data = this.inheritedScriptActionData; data != null; data = data.inheritedScriptActionData) {
         if (data.scriptTargets == this.scriptTargets) {
            return true;
         }
      }

      return false;
   }

   public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, ScriptTargets scriptTargets, Event event) {
      this.eliteEntity = eliteEntity;
      this.directTarget = directTarget;
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.event = event;
   }

   public ScriptActionData(EliteEntity eliteEntity, LivingEntity directTarget, ScriptTargets scriptTargets, ScriptZone scriptZone, Event event) {
      this.eliteEntity = eliteEntity;
      this.directTarget = directTarget;
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.scriptZone = scriptZone;
      this.event = event;
   }

   public ScriptActionData(ScriptTargets scriptTargets, ScriptZone scriptZone, ScriptActionData inheritedScriptActionData) {
      this.eliteEntity = inheritedScriptActionData.getEliteEntity();
      this.directTarget = inheritedScriptActionData.getDirectTarget();
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.scriptZone = scriptZone;
      this.inheritedScriptActionData = inheritedScriptActionData;
      this.landingLocation = inheritedScriptActionData.getLandingLocation();
   }

   public ScriptActionData(EliteEntity eliteEntity, ScriptTargets scriptTargets, ScriptZone scriptZone) {
      this.eliteEntity = eliteEntity;
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.scriptZone = scriptZone;
   }

   public ScriptActionData(ScriptTargets scriptTargets, ScriptZone scriptZone, ScriptActionData inheritedScriptActionData, Location landingLocation) {
      this.eliteEntity = inheritedScriptActionData.getEliteEntity();
      this.directTarget = inheritedScriptActionData.getDirectTarget();
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.scriptZone = scriptZone;
      this.inheritedScriptActionData = inheritedScriptActionData;
      this.landingLocation = landingLocation;
   }

   public ScriptActionData(
      EliteEntity eliteEntity,
      LivingEntity directTarget,
      ScriptTargets scriptTargets,
      Collection<LivingEntity> previousEntityTargets,
      Collection<Location> previousLocationTargets
   ) {
      this.eliteEntity = eliteEntity;
      this.directTarget = directTarget;
      this.scriptTargets = scriptTargets;
      this.targetType = scriptTargets.getTargetBlueprint().getTargetType();
      this.previousEntityTargets = previousEntityTargets;
      this.previousLocationTargets = previousLocationTargets;
   }

   @Generated
   public TargetType getTargetType() {
      return this.targetType;
   }

   @Generated
   public ScriptTargets getScriptTargets() {
      return this.scriptTargets;
   }

   @Generated
   public Collection<Location> getLocations() {
      return this.locations;
   }

   @Generated
   public void setLocations(Collection<Location> locations) {
      this.locations = locations;
   }

   @Generated
   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   @Generated
   public void setEliteEntity(EliteEntity eliteEntity) {
      this.eliteEntity = eliteEntity;
   }

   @Generated
   public LivingEntity getDirectTarget() {
      return this.directTarget;
   }

   @Generated
   public void setDirectTarget(LivingEntity directTarget) {
      this.directTarget = directTarget;
   }

   @Generated
   public Location getLandingLocation() {
      return this.landingLocation;
   }

   @Generated
   public void setLandingLocation(Location landingLocation) {
      this.landingLocation = landingLocation;
   }

   @Generated
   public ScriptZone getScriptZone() {
      return this.scriptZone;
   }

   @Generated
   public ScriptActionData getInheritedScriptActionData() {
      return this.inheritedScriptActionData;
   }

   @Generated
   public void setInheritedScriptActionData(ScriptActionData inheritedScriptActionData) {
      this.inheritedScriptActionData = inheritedScriptActionData;
   }

   @Generated
   public Event getEvent() {
      return this.event;
   }

   @Generated
   public void setPreviousEntityTargets(Collection<LivingEntity> previousEntityTargets) {
      this.previousEntityTargets = previousEntityTargets;
   }

   @Generated
   public Collection<LivingEntity> getPreviousEntityTargets() {
      return this.previousEntityTargets;
   }

   @Generated
   public void setPreviousLocationTargets(Collection<Location> previousLocationTargets) {
      this.previousLocationTargets = previousLocationTargets;
   }

   @Generated
   public Collection<Location> getPreviousLocationTargets() {
      return this.previousLocationTargets;
   }

   @Generated
   public List<Shape> getShapesCachedByTarget() {
      return this.shapesCachedByTarget;
   }

   @Generated
   public void setShapesCachedByTarget(List<Shape> shapesCachedByTarget) {
      this.shapesCachedByTarget = shapesCachedByTarget;
   }
}

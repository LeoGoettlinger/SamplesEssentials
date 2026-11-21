package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.Set;

public record MindBodyCapabilities(
   Set<MindBodyLocomotion> supportedLocomotions,
   Set<MindBodyLocomotion> pathfindingLocomotions,
   boolean uniformScale,
   double minimumUniformScale,
   double maximumUniformScale,
   boolean independentDimensions,
   boolean entityCollisionToggle,
   boolean blockCollisionToggle
) {
   public MindBodyCapabilities(
      Set<MindBodyLocomotion> supportedLocomotions,
      Set<MindBodyLocomotion> pathfindingLocomotions,
      boolean uniformScale,
      double minimumUniformScale,
      double maximumUniformScale,
      boolean independentDimensions,
      boolean entityCollisionToggle,
      boolean blockCollisionToggle
   ) {
      supportedLocomotions = Set.copyOf(Objects.requireNonNull(supportedLocomotions, "supportedLocomotions"));
      pathfindingLocomotions = Set.copyOf(Objects.requireNonNull(pathfindingLocomotions, "pathfindingLocomotions"));
      if (supportedLocomotions.isEmpty()) {
         throw new IllegalArgumentException("At least one locomotion profile must be supported");
      } else if (!supportedLocomotions.containsAll(pathfindingLocomotions)) {
         throw new IllegalArgumentException("Pathfinding locomotion must be supported locomotion");
      } else if (Double.isFinite(minimumUniformScale)
         && Double.isFinite(maximumUniformScale)
         && !(minimumUniformScale <= 0.0)
         && !(maximumUniformScale < minimumUniformScale)) {
         if (uniformScale || Double.compare(minimumUniformScale, 1.0) == 0 && Double.compare(maximumUniformScale, 1.0) == 0) {
            this.supportedLocomotions = supportedLocomotions;
            this.pathfindingLocomotions = pathfindingLocomotions;
            this.uniformScale = uniformScale;
            this.minimumUniformScale = minimumUniformScale;
            this.maximumUniformScale = maximumUniformScale;
            this.independentDimensions = independentDimensions;
            this.entityCollisionToggle = entityCollisionToggle;
            this.blockCollisionToggle = blockCollisionToggle;
         } else {
            throw new IllegalArgumentException("Unsupported uniform scaling must use fixed 1.0 bounds");
         }
      } else {
         throw new IllegalArgumentException("Uniform scale bounds are invalid");
      }
   }

   public void validate(MindBodyProfile profile) {
      Objects.requireNonNull(profile, "profile");
      if (!this.supportedLocomotions.contains(profile.locomotion())) {
         throw new IllegalArgumentException("Unsupported Mind body locomotion: " + profile.locomotion());
      } else if ((this.uniformScale || Double.compare(profile.uniformScale(), 1.0) == 0)
         && !(profile.uniformScale() < this.minimumUniformScale)
         && !(profile.uniformScale() > this.maximumUniformScale)) {
         if (!this.entityCollisionToggle && !profile.entityCollidable()) {
            throw new IllegalArgumentException("The active native adapter cannot disable entity collision");
         }
      } else {
         throw new IllegalArgumentException(
            "Mind body scale " + profile.uniformScale() + " is outside supported bounds " + this.minimumUniformScale + ".." + this.maximumUniformScale
         );
      }
   }

   public boolean canPathfind(MindBodyLocomotion locomotion) {
      return this.pathfindingLocomotions.contains(Objects.requireNonNull(locomotion, "locomotion"));
   }
}

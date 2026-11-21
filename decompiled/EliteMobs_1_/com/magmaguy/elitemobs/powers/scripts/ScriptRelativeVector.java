package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ScriptRelativeVector {
   private final ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint;
   private ScriptTargets sourceTarget = null;
   private ScriptTargets destinationTarget = null;
   private Vector cachedVector = null;
   private Location actionLocation = null;
   private boolean sourceIsAction = false;
   private boolean destinationIsAction = false;

   public ScriptRelativeVector(ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint, ScriptRuntimeOwner runtimeOwner, Location actionLocation) {
      this.scriptRelativeVectorBlueprint = scriptRelativeVectorBlueprint;
      this.actionLocation = actionLocation;
      if (!scriptRelativeVectorBlueprint.getSourceTarget().getTargetType().equals(TargetType.ACTION_TARGET)) {
         this.sourceTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getSourceTarget(), runtimeOwner);
      } else {
         this.sourceIsAction = true;
      }

      if (!scriptRelativeVectorBlueprint.getDestinationTarget().getTargetType().equals(TargetType.ACTION_TARGET)) {
         this.destinationTarget = new ScriptTargets(scriptRelativeVectorBlueprint.getDestinationTarget(), runtimeOwner);
      } else {
         this.destinationIsAction = true;
      }
   }

   public Vector getVector(ScriptActionData scriptActionData) {
      if (this.cachedVector != null) {
         return this.cachedVector;
      } else {
         Location sourceLocation = this.endpoint(this.sourceIsAction, this.sourceTarget, scriptActionData);
         Location destinationLocation = this.endpoint(this.destinationIsAction, this.destinationTarget, scriptActionData);
         if (sourceLocation != null
            && destinationLocation != null
            && sourceLocation.getWorld() != null
            && sourceLocation.getWorld().equals(destinationLocation.getWorld())) {
            Vector vector = destinationLocation.clone().subtract(sourceLocation).toVector();
            if (this.scriptRelativeVectorBlueprint.isNormalize() && vector.lengthSquared() > 0.0) {
               vector.normalize();
            }

            vector.multiply(this.scriptRelativeVectorBlueprint.getMultiplier().getValue());
            vector.add(this.scriptRelativeVectorBlueprint.getOffset().getValue());
            return vector;
         } else {
            return new Vector();
         }
      }
   }

   private Location endpoint(boolean isAction, ScriptTargets target, ScriptActionData data) {
      if (isAction) {
         return this.actionLocation;
      } else if (target == null) {
         return null;
      } else {
         Collection<Location> locations = target.getTargetLocations(data);
         return locations.isEmpty() ? null : locations.iterator().next();
      }
   }

   public void cacheVector(ScriptActionData scriptActionData) {
      this.cachedVector = this.getVector(scriptActionData);
   }
}

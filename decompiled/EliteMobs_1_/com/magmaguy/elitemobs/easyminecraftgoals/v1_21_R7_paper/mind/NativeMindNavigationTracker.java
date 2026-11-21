package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindNavigationProgress;
import com.magmaguy.elitemobs.magmacore.ai.MindNavigationStatus;
import com.magmaguy.elitemobs.magmacore.ai.MindPosition;
import java.util.Optional;
import net.minecraft.world.entity.EntityInsentient;
import org.bukkit.Location;

final class NativeMindNavigationTracker {
   private static final double ARRIVAL_DISTANCE_SQUARED = 0.25;
   private static final double PROGRESS_DISTANCE_SQUARED = 0.0025;
   private final EntityInsentient mob;
   private MindPosition destination;
   private MindNavigationProgress lastProgress;
   private int consecutiveStuckTicks;

   NativeMindNavigationTracker(EntityInsentient mob) {
      this.mob = mob;
   }

   void destinationRequested(Location requested) {
      this.destination = MindPosition.from(requested);
      if (this.lastProgress == null) {
         this.lastProgress = new MindNavigationProgress(this.mob.ao().au(), this.currentPosition());
         this.consecutiveStuckTicks = 0;
      }
   }

   void beginTick(long gameTick) {
      if (this.destination != null) {
         MindPosition current = this.currentPosition();
         if (distanceSquared(current, this.destination) <= 0.25) {
            this.reset();
         } else {
            if (distanceSquared(current, this.lastProgress.position()) >= 0.0025) {
               this.lastProgress = new MindNavigationProgress(gameTick, current);
               this.consecutiveStuckTicks = 0;
            } else if (this.consecutiveStuckTicks < Integer.MAX_VALUE) {
               this.consecutiveStuckTicks++;
            }
         }
      }
   }

   MindNavigationStatus status() {
      return this.destination == null
         ? MindNavigationStatus.idle()
         : new MindNavigationStatus(Optional.of(this.destination), !this.mob.N().l(), Optional.of(this.lastProgress), this.consecutiveStuckTicks);
   }

   void reset() {
      this.destination = null;
      this.lastProgress = null;
      this.consecutiveStuckTicks = 0;
   }

   private MindPosition currentPosition() {
      return MindPosition.from(this.mob.getBukkitEntity().getLocation());
   }

   private static double distanceSquared(MindPosition first, MindPosition second) {
      if (!first.world().equals(second.world())) {
         return Double.POSITIVE_INFINITY;
      } else {
         double x = first.x() - second.x();
         double y = first.y() - second.y();
         double z = first.z() - second.z();
         return x * x + y * y + z * z;
      }
   }
}

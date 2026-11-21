package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Ray;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.Lerp;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class TranslatingRay extends Ray {
   public TranslatingRay(
      boolean ignoresSolidBlocks, double pointRadius, Location target, Location finalTarget, Location target2, Location finalTarget2, int animationDuration
   ) {
      super(ignoresSolidBlocks, pointRadius, target, target2);
      RotatingRay.validateEndpoints(target, target2, pointRadius, animationDuration);
      Location firstEnd = finalTarget == null ? target : finalTarget;
      Location secondEnd = finalTarget2 == null ? target2 : finalTarget2;
      RotatingRay.validateEndpoints(target, firstEnd, pointRadius, animationDuration);
      RotatingRay.validateEndpoints(target, secondEnd, pointRadius, animationDuration);
      RotatingRay.validateEndpoints(firstEnd, secondEnd, pointRadius, animationDuration);
      this.centerLocation = target.clone();
      this.locations = this.drawLine(this.centerLocation, target2.clone());
      if (animationDuration > 0) {
         this.startAnimation(target.clone(), firstEnd.clone(), target2.clone(), secondEnd.clone(), animationDuration);
      }
   }

   private void startAnimation(final Location firstStart, final Location firstEnd, final Location secondStart, final Location secondEnd, final int duration) {
      (new BukkitRunnable() {
         private int frame;

         public void run() {
            try {
               double progress = (double)(++this.frame) / duration;
               Location source = Lerp.lerpLocation(firstStart, firstEnd, progress);
               Location target = Lerp.lerpLocation(secondStart, secondEnd, progress);
               TranslatingRay.this.locations = TranslatingRay.this.drawLine(source, target);
               TranslatingRay.this.centerLocation = source;
               if (this.frame == duration) {
                  this.cancel();
               }
            } catch (RuntimeException var5) {
               this.cancel();
               Logger.warn("Stopped translating ray after geometry failure: " + var5);
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
   }
}

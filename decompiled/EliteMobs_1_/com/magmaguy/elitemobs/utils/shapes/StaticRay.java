package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.magmacore.scripting.zones.Ray;
import org.bukkit.Location;

public class StaticRay extends Ray {
   public StaticRay(boolean ignoresSolidBlocks, double pointRadius, Location centerLocation, Location initialTargetLocation) {
      super(ignoresSolidBlocks, pointRadius, centerLocation, initialTargetLocation);
      this.locations = this.drawLine(centerLocation, initialTargetLocation);
   }
}

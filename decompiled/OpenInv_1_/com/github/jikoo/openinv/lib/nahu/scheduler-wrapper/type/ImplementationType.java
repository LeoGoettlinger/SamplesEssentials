package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.type;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public enum ImplementationType {
   FOLIA(
      "io.papermc.paper.threadedregions.RegionizedServer",
      "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler",
      "io.papermc.paper.threadedregions.scheduler.RegionScheduler"
   ),
   BUKKIT("org.bukkit.scheduler.BukkitScheduler"),
   UNKNOWN();

   private final String[] classNames;

   private ImplementationType(String... classNames) {
      this.classNames = classNames;
   }

   public String[] getClassNames() {
      return this.classNames;
   }

   public boolean isApplicable() {
      for (String className : this.getClassNames()) {
         try {
            Class.forName(className);
            return true;
         } catch (ClassNotFoundException var6) {
         }
      }

      return false;
   }

   public boolean isUnknown() {
      return this == UNKNOWN;
   }

   @NotNull
   public static ImplementationType find() {
      return Arrays.stream(values()).filter(ImplementationType::isApplicable).findFirst().orElse(UNKNOWN);
   }
}

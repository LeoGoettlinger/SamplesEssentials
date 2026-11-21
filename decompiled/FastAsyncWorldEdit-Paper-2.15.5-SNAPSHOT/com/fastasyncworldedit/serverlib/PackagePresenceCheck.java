package com.fastasyncworldedit.serverlib;

import com.fastasyncworldedit.serverlib.util.PackageChecker;

public abstract class PackagePresenceCheck extends Check {
   private final String[] illegalPackages;

   public PackagePresenceCheck(CheckType type, String... packages) {
      super(type);
      this.illegalPackages = packages;
   }

   @Override
   public boolean shouldFlag() {
      for (String illegalPackage : this.illegalPackages) {
         if (PackageChecker.packageExists(illegalPackage)) {
            return true;
         }
      }

      return false;
   }
}

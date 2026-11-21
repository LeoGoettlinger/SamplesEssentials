package com.magmaguy.elitemobs.advancedcombat;

final class ClassIdentityLabelFormatter {
   private ClassIdentityLabelFormatter() {
   }

   static String format(String className, int effectiveLevel) {
      return "&f[" + effectiveLevel + "] &b" + className;
   }
}

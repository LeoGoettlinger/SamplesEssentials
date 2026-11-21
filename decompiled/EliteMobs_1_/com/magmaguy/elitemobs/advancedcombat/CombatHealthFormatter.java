package com.magmaguy.elitemobs.advancedcombat;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CombatHealthFormatter {
   private static final int DECIMAL_PLACES = 2;

   private CombatHealthFormatter() {
   }

   public static String format(double health) {
      return Double.isFinite(health) && !(health <= 0.0) ? BigDecimal.valueOf(health).setScale(2, RoundingMode.DOWN).stripTrailingZeros().toPlainString() : "0";
   }
}

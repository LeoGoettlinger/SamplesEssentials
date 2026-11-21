package com.magmaguy.elitemobs.magmacore.util;

public class Round {
   private Round() {
      throw new IllegalStateException("Utility class");
   }

   public static double decimalPlaces(double value, int places) {
      double number = Math.pow(10.0, places);
      return Math.round(value * number) / number;
   }

   public static float decimalPlaces(float value, int places) {
      double number = Math.pow(10.0, places);
      return (float)Math.round(value * number) / (float)number;
   }

   public static double fourDecimalPlaces(double value) {
      return decimalPlaces(value, 4);
   }

   public static float fourDecimalPlaces(float value) {
      return decimalPlaces(value, 4);
   }

   public static double twoDecimalPlaces(double value) {
      return decimalPlaces(value, 2);
   }

   public static double oneDecimalPlace(double value) {
      return decimalPlaces(value, 1);
   }
}

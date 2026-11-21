package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;

public class TaxUtils {
   public static double getTotalAfterTax(double original, CurrencyHook currencyHook) {
      Number tax = getTaxPercent(currencyHook);
      double taxMulti = getMultiplierFormat(tax.doubleValue());
      return taxMulti == 1.0 ? original : floorIfUnsupported(original * taxMulti, currencyHook);
   }

   public static double getTotalTax(double original, CurrencyHook currencyHook) {
      Number tax = getTaxPercent(currencyHook);
      double taxMulti = getMultiplierFormat(tax.doubleValue());
      return taxMulti == 1.0 ? 0.0 : ceilIfUnsupported(original - original * taxMulti, currencyHook);
   }

   public static Number getTaxPercent(CurrencyHook currencyHook) {
      Number value = (Number)currencyHook.getSettings().getOrDefault("tax", 0);
      return Math.min(value.doubleValue(), 99.0);
   }

   public static double getMultiplierFormat(double original) {
      return (100.0 - original) / 100.0;
   }

   private static double floorIfUnsupported(double amount, CurrencyHook currencyHook) {
      return currencyHook.usesDouble() ? amount : Math.floor(amount);
   }

   private static double ceilIfUnsupported(double amount, CurrencyHook currencyHook) {
      return currencyHook.usesDouble() ? amount : Math.ceil(amount);
   }
}

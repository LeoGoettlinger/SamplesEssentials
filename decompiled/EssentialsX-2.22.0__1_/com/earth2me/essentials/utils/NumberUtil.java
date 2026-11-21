package com.earth2me.essentials.utils;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.commands.InvalidModifierException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import net.ess3.api.IEssentials;

public final class NumberUtil {
   private static final BigDecimal THOUSAND = new BigDecimal(1000);
   private static final BigDecimal MILLION = new BigDecimal(1000000);
   private static final BigDecimal BILLION = new BigDecimal(1000000000);
   private static final BigDecimal TRILLION = new BigDecimal(1000000000000L);
   private static final DecimalFormat twoDPlaces = new DecimalFormat("#,###.##");
   private static final DecimalFormat currencyFormat = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
   private static Locale PRETTY_LOCALE = Locale.US;
   private static NumberFormat PRETTY_FORMAT = NumberFormat.getInstance(PRETTY_LOCALE);

   private NumberUtil() {
   }

   public static void internalSetPrettyFormat(final NumberFormat prettyFormat) {
      PRETTY_FORMAT = prettyFormat;
   }

   public static String shortCurrency(final BigDecimal value, final IEssentials ess) {
      return ess.getSettings().isCurrencySymbolSuffixed()
         ? formatAsCurrency(value) + ess.getSettings().getCurrencySymbol()
         : ess.getSettings().getCurrencySymbol() + formatAsCurrency(value);
   }

   public static String formatDouble(final double value) {
      return twoDPlaces.format(value);
   }

   public static String formatAsCurrency(final BigDecimal value) {
      String str = currencyFormat.format(value);
      if (str.endsWith(".00")) {
         str = str.substring(0, str.length() - 3);
      }

      return str;
   }

   public static String formatAsPrettyCurrency(final BigDecimal value) {
      String str = PRETTY_FORMAT.format(value);
      if (str.endsWith(".00")) {
         str = str.substring(0, str.length() - 3);
      }

      return str;
   }

   public static String displayCurrency(final BigDecimal value, final IEssentials ess) {
      return displayCurrency(value, ess, false);
   }

   public static String displayCurrencyExactly(final BigDecimal value, final IEssentials ess) {
      return displayCurrency(value, ess, true);
   }

   private static String displayCurrency(final BigDecimal value, final IEssentials ess, final boolean exact) {
      String currency = exact ? value.toPlainString() : formatAsPrettyCurrency(value);
      String sign = "";
      if (value.signum() < 0) {
         currency = currency.substring(1);
         sign = "-";
      }

      return ess.getSettings().isCurrencySymbolSuffixed()
         ? sign + I18n.tlLiteral("currency", currency, ess.getSettings().getCurrencySymbol())
         : sign + I18n.tlLiteral("currency", ess.getSettings().getCurrencySymbol(), currency);
   }

   public static String sanitizeCurrencyString(final String input, final IEssentials ess) {
      return input.replace(ess.getSettings().getCurrencySymbol(), "");
   }

   public static boolean isInt(final String sInt) {
      try {
         Integer.parseInt(sInt);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static boolean isLong(final String sLong) {
      try {
         Long.parseLong(sLong);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static boolean isPositiveInt(final String sInt) {
      return !isInt(sInt) ? false : Integer.parseInt(sInt) > 0;
   }

   public static boolean isNumeric(final String sNum) {
      for (char sChar : sNum.toCharArray()) {
         if (!Character.isDigit(sChar)) {
            return false;
         }
      }

      return true;
   }

   public static boolean isHexadecimal(final String sNum) {
      try {
         Integer.parseInt(sNum, 16);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static BigDecimal parseStringToBDecimal(final String sArg, final Locale locale) throws ParseException, InvalidModifierException {
      if (sArg.isEmpty()) {
         throw new IllegalArgumentException();
      } else {
         String sanitizedString = sArg.replaceAll("[^0-9.,]", "");
         BigDecimal multiplier = null;
         String format = sArg.replace(sanitizedString, "").toUpperCase();
         switch (format) {
            case "":
               break;
            case "K":
               multiplier = THOUSAND;
               break;
            case "M":
               multiplier = MILLION;
               break;
            case "B":
               multiplier = BILLION;
               break;
            case "T":
               multiplier = TRILLION;
               break;
            default:
               throw new InvalidModifierException();
         }

         NumberFormat format = NumberFormat.getInstance(locale);
         Number parsed = format.parse(sanitizedString);
         BigDecimal amount = new BigDecimal(parsed.toString());
         if (multiplier != null) {
            amount = amount.multiply(multiplier);
         }

         return amount;
      }
   }

   public static BigDecimal parseStringToBDecimal(final String sArg) throws ParseException, InvalidModifierException {
      return parseStringToBDecimal(sArg, PRETTY_LOCALE);
   }

   public static int constrainToRange(int value, int min, int max) {
      return Math.min(Math.max(value, min), max);
   }

   static {
      twoDPlaces.setRoundingMode(RoundingMode.HALF_UP);
      currencyFormat.setRoundingMode(RoundingMode.FLOOR);
      PRETTY_FORMAT.setRoundingMode(RoundingMode.FLOOR);
      PRETTY_FORMAT.setGroupingUsed(true);
      PRETTY_FORMAT.setMinimumFractionDigits(2);
      PRETTY_FORMAT.setMaximumFractionDigits(2);
   }
}

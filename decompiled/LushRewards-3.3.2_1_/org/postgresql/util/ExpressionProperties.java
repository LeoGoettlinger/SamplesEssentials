package org.postgresql.util;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.postgresql.util.internal.Nullness;

public class ExpressionProperties extends Properties {
   @Regex(1)
   private static final Pattern EXPRESSION = Pattern.compile("\\$\\{([^}]+)\\}");
   private final Properties[] defaults;

   public ExpressionProperties(Properties... defaults) {
      this.defaults = defaults;
   }

   @Nullable
   @Override
   public String getProperty(String key) {
      String value = this.getRawPropertyValue(key);
      return this.replaceProperties(value);
   }

   @PolyNull
   @Override
   public String getProperty(String key, @PolyNull String defaultValue) {
      String value = this.getRawPropertyValue(key);
      if (value == null) {
         value = defaultValue;
      }

      return this.replaceProperties(value);
   }

   @Nullable
   public String getRawPropertyValue(String key) {
      String value = super.getProperty(key);
      if (value != null) {
         return value;
      } else {
         for (Properties properties : this.defaults) {
            value = properties.getProperty(key);
            if (value != null) {
               return value;
            }
         }

         return null;
      }
   }

   @PolyNull
   private String replaceProperties(@PolyNull String value) {
      if (value == null) {
         return null;
      } else {
         Matcher matcher = EXPRESSION.matcher(value);
         StringBuffer sb = null;

         while (matcher.find()) {
            if (sb == null) {
               sb = new StringBuffer();
            }

            String propValue = this.getProperty(Nullness.castNonNull(matcher.group(1)));
            if (propValue == null) {
               propValue = matcher.group();
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(propValue));
         }

         if (sb == null) {
            return value;
         } else {
            matcher.appendTail(sb);
            return sb.toString();
         }
      }
   }
}

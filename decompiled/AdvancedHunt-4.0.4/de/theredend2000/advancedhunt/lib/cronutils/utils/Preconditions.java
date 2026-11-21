package de.theredend2000.advancedhunt.lib.cronutils.utils;

import java.util.Collection;

public class Preconditions {
   private Preconditions() {
   }

   public static void checkArgument(boolean expression, Object errorMessage) {
      if (!expression) {
         throw new IllegalArgumentException(String.valueOf(errorMessage));
      }
   }

   public static void checkPositive(int value, Object errorMessage) {
      if (value < 0) {
         throw new IllegalArgumentException(String.valueOf(errorMessage));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
      if (!expression) {
         throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
      if (!expression) {
         throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
      }
   }

   public static <T> T checkNotNull(T reference) {
      if (reference == null) {
         throw new NullPointerException();
      } else {
         return reference;
      }
   }

   public static <T> T checkNotNull(T reference, Object errorMessage) {
      if (reference == null) {
         throw new NullPointerException(String.valueOf(errorMessage));
      } else {
         return reference;
      }
   }

   public static String checkNotNullNorEmpty(String reference, Object errorMessage) {
      if (reference == null) {
         throw new NullPointerException(String.valueOf(errorMessage));
      } else if (reference.isEmpty()) {
         throw new IllegalArgumentException(String.valueOf(errorMessage));
      } else {
         return reference;
      }
   }

   public static <T extends Collection<?>> T checkNotNullNorEmpty(T reference, Object errorMessage) {
      if (reference == null) {
         throw new NullPointerException(String.valueOf(errorMessage));
      } else if (reference.isEmpty()) {
         throw new IllegalArgumentException(String.valueOf(errorMessage));
      } else {
         return reference;
      }
   }

   private static String format(String nullableTemplate, Object... args) {
      String template = String.valueOf(nullableTemplate);
      StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
      int templateStart = 0;
      int i = 0;

      while (i < args.length) {
         int placeholderStart = template.indexOf("%s", templateStart);
         if (placeholderStart == -1) {
            break;
         }

         builder.append(template.substring(templateStart, placeholderStart));
         builder.append(args[i++]);
         templateStart = placeholderStart + 2;
      }

      builder.append(template.substring(templateStart));
      if (i < args.length) {
         builder.append(" [");
         builder.append(args[i++]);

         while (i < args.length) {
            builder.append(", ");
            builder.append(args[i++]);
         }

         builder.append(']');
      }

      return builder.toString();
   }
}

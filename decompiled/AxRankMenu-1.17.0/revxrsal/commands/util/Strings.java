package revxrsal.commands.util;

import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Switch;

public final class Strings {
   public static final Pattern SPACE = Pattern.compile(" ", 16);
   public static final Pattern VERTICAL_BAR = Pattern.compile("|", 16);
   public static final Pattern SNOWFLAKE = Pattern.compile("<(@!|@|@&|#)(?<snowflake>\\d{18})>");

   private Strings() {
   }

   public static LinkedList<String> splitBySpace(String text) {
      String[] result = SPACE.split(text);
      LinkedList<String> list = new LinkedList<>();
      java.util.Collections.addAll(list, result);
      return list;
   }

   @Nullable
   public static String getSnowflake(String mention) {
      Matcher matcher = SNOWFLAKE.matcher(mention);
      return matcher.find() ? matcher.group(2) : null;
   }

   @Nullable
   public static String getOverriddenName(@NotNull Parameter parameter) {
      Named named = parameter.getAnnotation(Named.class);
      if (named != null) {
         return named.value();
      } else {
         Switch switchAnn = parameter.getAnnotation(Switch.class);
         if (switchAnn != null) {
            return switchAnn.value().isEmpty() ? parameter.getName() : switchAnn.value();
         } else {
            Flag flag = parameter.getAnnotation(Flag.class);
            if (flag != null) {
               return flag.value().isEmpty() ? parameter.getName() : flag.value();
            } else {
               return null;
            }
         }
      }
   }

   @NotNull
   public static String stripNamespace(String namespace, @NotNull String command) {
      int colon = command.indexOf(namespace + ':');
      return colon == -1 ? command : command.substring(namespace.length() + 1);
   }

   @NotNull
   public static String stripNamespace(@NotNull String command) {
      int colon = command.indexOf(58);
      return colon == -1 ? command : command.substring(colon + 1);
   }

   public static String repeat(String string, int count) {
      Preconditions.notNull(string, "string");
      if (count <= 1) {
         Preconditions.checkArgument(count >= 0, "invalid count: " + count);
         return count == 0 ? "" : string;
      } else {
         int len = string.length();
         long longSize = (long)len * count;
         int size = (int)longSize;
         if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
         } else {
            char[] array = new char[size];
            string.getChars(0, len, array, 0);

            int n;
            for (n = len; n < size - n; n <<= 1) {
               System.arraycopy(array, 0, array, n, n);
            }

            System.arraycopy(array, 0, array, n, size - n);
            return new String(array);
         }
      }
   }

   public static String colorize(@NotNull String text) {
      char[] b = text.toCharArray();

      for (int i = 0; i < b.length - 1; i++) {
         if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
            b[i] = 167;
            b[i + 1] = Character.toLowerCase(b[i + 1]);
         }
      }

      return new String(b);
   }
}

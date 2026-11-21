package github.nighter.smartspawner.spawner.config;

public final class SpawnerConfigName {
   private static final int MAX_LENGTH = 128;

   private SpawnerConfigName() {
   }

   public static String normalize(String input) {
      if (input != null && !input.isBlank()) {
         StringBuilder result = new StringBuilder(input.length());
         boolean separator = true;

         for (int i = 0; i < input.length(); i++) {
            char c = Character.toLowerCase(input.charAt(i));
            if (Character.isLetterOrDigit(c) || c == '-') {
               if (result.length() < 128) {
                  result.append(c);
               }

               separator = false;
            } else if (!separator) {
               if (result.length() < 128) {
                  result.append('_');
               }

               separator = true;
            }
         }

         int length = result.length();
         if (length > 0 && result.charAt(length - 1) == '_') {
            result.setLength(length - 1);
         }

         return result.toString();
      } else {
         return "";
      }
   }

   public static String defaultName(String typeName) {
      return normalize(typeName) + "_spawner";
   }
}

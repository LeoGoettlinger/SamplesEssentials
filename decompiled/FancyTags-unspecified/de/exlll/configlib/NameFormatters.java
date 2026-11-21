package de.exlll.configlib;

public enum NameFormatters implements NameFormatter {
   IDENTITY {
      @Override
      public String format(String name) {
         return name;
      }
   },
   LOWER_UNDERSCORE {
      @Override
      public String format(String name) {
         StringBuilder builder = new StringBuilder(name.length());

         for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
               char lower = Character.toLowerCase(c);
               builder.append('_').append(lower);
            } else {
               builder.append(c);
            }
         }

         return builder.toString();
      }
   },
   UPPER_UNDERSCORE {
      @Override
      public String format(String name) {
         StringBuilder builder = new StringBuilder(name.length());

         for (char c : name.toCharArray()) {
            if (Character.isLowerCase(c)) {
               builder.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
               builder.append('_').append(c);
            } else {
               builder.append(c);
            }
         }

         return builder.toString();
      }
   },
   LOWER_KEBAB_CASE {
      @Override
      public String format(String name) {
         StringBuilder builder = new StringBuilder(name.length());

         for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
               builder.append('-');
               builder.append(Character.toLowerCase(c));
            } else {
               builder.append(c);
            }
         }

         return builder.toString();
      }
   },
   UPPER_KEBAB_CASE {
      @Override
      public String format(String name) {
         StringBuilder builder = new StringBuilder(name.length());

         for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLowerCase(c)) {
               builder.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
               builder.append('-');
               builder.append(c);
            } else {
               builder.append(c);
            }
         }

         return builder.toString();
      }
   };
}

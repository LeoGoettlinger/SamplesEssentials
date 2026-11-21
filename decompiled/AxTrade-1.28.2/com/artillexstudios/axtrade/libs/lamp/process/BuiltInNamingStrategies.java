package com.artillexstudios.axtrade.libs.lamp.process;

final class BuiltInNamingStrategies {
   static String separateCamelCase(String name, String separator) {
      StringBuilder translation = new StringBuilder();

      for (int i = 0; i < name.length(); i++) {
         char character = name.charAt(i);
         if (Character.isUpperCase(character) && translation.length() != 0) {
            translation.append(separator);
         }

         translation.append(character);
      }

      return translation.toString();
   }

   static String upperCaseFirstLetter(String name) {
      StringBuilder fieldNameBuilder = new StringBuilder();
      int index = 0;

      char firstCharacter;
      for (firstCharacter = name.charAt(index); index < name.length() - 1 && !Character.isLetter(firstCharacter); firstCharacter = name.charAt(++index)) {
         fieldNameBuilder.append(firstCharacter);
      }

      if (index == name.length()) {
         return fieldNameBuilder.toString();
      } else if (!Character.isUpperCase(firstCharacter)) {
         String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
         return fieldNameBuilder.append(modifiedTarget).toString();
      } else {
         return name;
      }
   }

   private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
      return indexOfSubstring < srcString.length() ? firstCharacter + srcString.substring(indexOfSubstring) : String.valueOf(firstCharacter);
   }
}

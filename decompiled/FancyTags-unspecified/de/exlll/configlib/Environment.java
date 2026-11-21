package de.exlll.configlib;

import java.util.Map;

interface Environment {
   String getValue(String var1);

   Map<String, String> getEnvironment();

   public static final class SystemEnvironment implements Environment {
      @Override
      public String getValue(String environmentVariable) {
         return System.getenv(environmentVariable);
      }

      @Override
      public Map<String, String> getEnvironment() {
         return System.getenv();
      }
   }
}

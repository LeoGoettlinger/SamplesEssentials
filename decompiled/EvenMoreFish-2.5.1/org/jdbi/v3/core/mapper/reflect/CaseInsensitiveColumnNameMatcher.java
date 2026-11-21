package org.jdbi.v3.core.mapper.reflect;

public final class CaseInsensitiveColumnNameMatcher implements ColumnNameMatcher {
   @Override
   public boolean columnNameMatches(String columnName, String propertyName) {
      return this.doMatch(columnName, propertyName, false);
   }

   @Override
   public boolean columnNameStartsWith(String columnName, String prefix) {
      return this.doMatch(columnName, prefix, true);
   }

   private boolean doMatch(String columnName, String propertyName, boolean columnNameMayTrailCharacters) {
      int cPos = 0;
      int pPos = 0;

      while (true) {
         boolean cEnded = cPos >= columnName.length();
         boolean pEnded = pPos >= propertyName.length();
         if (cEnded || pEnded) {
            return pEnded && (cEnded || columnNameMayTrailCharacters);
         }

         char pChar = propertyName.charAt(pPos);
         char cChar = columnName.charAt(cPos);
         if (pChar == '.') {
            pPos++;
         } else {
            if (Character.toLowerCase(cChar) != Character.toLowerCase(pChar)) {
               return false;
            }

            pPos++;
            cPos++;
         }
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }
}

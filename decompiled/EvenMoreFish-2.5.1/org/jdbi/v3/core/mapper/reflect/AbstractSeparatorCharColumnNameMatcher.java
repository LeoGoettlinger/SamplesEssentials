package org.jdbi.v3.core.mapper.reflect;

public abstract class AbstractSeparatorCharColumnNameMatcher implements ColumnNameMatcher {
   private final char separatorChar;

   protected AbstractSeparatorCharColumnNameMatcher(char separatorChar) {
      this.separatorChar = separatorChar;
   }

   @Override
   public boolean columnNameMatches(String columnName, String propertyName) {
      return this.doMatch(columnName, propertyName, false);
   }

   @Override
   public boolean columnNameStartsWith(String columnName, String prefix) {
      int dotIndex = prefix.lastIndexOf(46);
      String propertyName = dotIndex == -1 ? prefix : prefix.substring(0, dotIndex);
      return this.doMatch(columnName, propertyName, true);
   }

   private boolean doMatch(String columnName, String propertyName, boolean columnNameMayTrailCharacters) {
      int cPos = 0;
      int pPos = 0;
      boolean separatorMatched = false;

      while (true) {
         boolean cEnded = cPos >= columnName.length();
         boolean pEnded = pPos >= propertyName.length();
         if (cEnded || pEnded) {
            return pEnded && (cEnded || columnNameMayTrailCharacters);
         }

         char pChar = propertyName.charAt(pPos);
         char cChar = columnName.charAt(cPos);
         if (pChar != '.' && pChar != this.separatorChar) {
            separatorMatched = false;
            if (cChar != this.separatorChar || !Character.isLetterOrDigit(pChar)) {
               if (Character.toLowerCase(cChar) != Character.toLowerCase(pChar)) {
                  return false;
               }

               pPos++;
            }

            cPos++;
         } else {
            pPos++;
            if (cChar == this.separatorChar) {
               cPos++;
               separatorMatched = true;
            } else if (!separatorMatched) {
               return false;
            }
         }
      }
   }
}

package de.theredend2000.advancedhunt.lib.hikari;

import java.sql.SQLException;

public interface SQLExceptionOverride {
   default SQLExceptionOverride.Override adjudicate(SQLException sqlException) {
      return SQLExceptionOverride.Override.CONTINUE_EVICT;
   }

   public static enum Override {
      CONTINUE_EVICT,
      DO_NOT_EVICT;
   }
}

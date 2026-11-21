package org.evenmorefish.fish.libs.hikaricp.hikari;

import java.sql.SQLException;

public interface SQLExceptionOverride {
   default SQLExceptionOverride.Override adjudicate(SQLException sqlException) {
      return SQLExceptionOverride.Override.CONTINUE_EVICT;
   }

   public static enum Override {
      CONTINUE_EVICT,
      DO_NOT_EVICT,
      MUST_EVICT;
   }
}

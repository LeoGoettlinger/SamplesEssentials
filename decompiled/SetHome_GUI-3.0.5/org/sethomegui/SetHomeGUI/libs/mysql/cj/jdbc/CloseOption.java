package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc;

import java.util.Arrays;

public enum CloseOption {
   IMPLICIT,
   FORCED,
   ROLLBACK,
   PROPAGATE,
   NO_CACHE;

   public boolean in(CloseOption... options) {
      return Arrays.stream(options).anyMatch(this::equals);
   }

   public boolean notIn(CloseOption... options) {
      return Arrays.stream(options).noneMatch(this::equals);
   }
}

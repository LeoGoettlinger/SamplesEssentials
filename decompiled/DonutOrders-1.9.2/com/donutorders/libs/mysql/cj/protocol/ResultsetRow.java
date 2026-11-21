package com.donutorders.libs.mysql.cj.protocol;

import com.donutorders.libs.mysql.cj.result.Row;

public interface ResultsetRow extends Row, ProtocolEntity {
   default boolean isBinaryEncoded() {
      return false;
   }
}

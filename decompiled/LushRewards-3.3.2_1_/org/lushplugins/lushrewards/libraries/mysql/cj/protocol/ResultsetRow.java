package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import org.lushplugins.lushrewards.libraries.mysql.cj.result.Row;

public interface ResultsetRow extends Row, ProtocolEntity {
   default boolean isBinaryEncoded() {
      return false;
   }
}

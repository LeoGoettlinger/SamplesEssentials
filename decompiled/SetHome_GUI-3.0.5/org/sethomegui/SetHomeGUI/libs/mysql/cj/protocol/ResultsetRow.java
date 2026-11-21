package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Row;

public interface ResultsetRow extends Row, ProtocolEntity {
   default boolean isBinaryEncoded() {
      return false;
   }
}

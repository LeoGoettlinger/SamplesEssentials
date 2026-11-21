package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

public interface JsonValue {
   default String toFormattedString() {
      return this.toString();
   }
}

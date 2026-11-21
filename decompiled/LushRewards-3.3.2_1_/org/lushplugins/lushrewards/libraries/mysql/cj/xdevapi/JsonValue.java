package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

public interface JsonValue {
   default String toFormattedString() {
      return this.toString();
   }
}

package org.jdbi.v3.core.statement;

import org.jdbi.v3.meta.Alpha;

@Alpha
public enum BindListStyle {
   PLAIN("", ""),
   ROWS("(", ")");

   private final String elementPrefix;
   private final String elementSuffix;

   private BindListStyle(String elementPrefix, String elementSuffix) {
      this.elementPrefix = elementPrefix;
      this.elementSuffix = elementSuffix;
   }

   void appendElement(StringBuilder target, String parameterName) {
      target.append(this.elementPrefix).append(parameterName).append(this.elementSuffix);
   }
}

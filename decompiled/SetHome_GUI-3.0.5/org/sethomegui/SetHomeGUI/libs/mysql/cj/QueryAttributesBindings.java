package org.sethomegui.SetHomeGUI.libs.mysql.cj;

import java.util.function.Consumer;

public interface QueryAttributesBindings {
   void setAttribute(String var1, Object var2);

   void removeAttribute(String var1);

   int getCount();

   BindValue getAttributeValue(int var1);

   void runThroughAll(Consumer<BindValue> var1);

   boolean containsAttribute(String var1);

   void clearAttributes();
}

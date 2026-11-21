package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.querybuilder;

public abstract class Value {
   public boolean isCombinable() {
      return false;
   }

   @Override
   public abstract String toString();
}

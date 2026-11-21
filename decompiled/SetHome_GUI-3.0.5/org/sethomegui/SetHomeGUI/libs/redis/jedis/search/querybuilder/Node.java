package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.querybuilder;

public interface Node {
   String toString(Node.Parenthesize var1);

   @Override
   String toString();

   public static enum Parenthesize {
      ALWAYS,
      NEVER,
      DEFAULT;
   }
}

package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.querybuilder;

public class DisjunctUnionNode extends DisjunctNode {
   @Override
   protected String getJoinString() {
      return "|";
   }
}

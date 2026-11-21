package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.querybuilder;

public class UnionNode extends QueryNode {
   @Override
   protected String getJoinString() {
      return "|";
   }
}

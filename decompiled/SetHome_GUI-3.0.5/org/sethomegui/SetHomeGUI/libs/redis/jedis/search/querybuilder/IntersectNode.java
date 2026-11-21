package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.querybuilder;

public class IntersectNode extends QueryNode {
   @Override
   protected String getJoinString() {
      return " ";
   }
}

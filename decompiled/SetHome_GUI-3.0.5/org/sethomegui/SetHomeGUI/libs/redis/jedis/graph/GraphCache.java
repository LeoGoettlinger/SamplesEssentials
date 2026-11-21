package org.sethomegui.SetHomeGUI.libs.redis.jedis.graph;

@Deprecated
interface GraphCache {
   String getLabel(int var1);

   String getRelationshipType(int var1);

   String getPropertyName(int var1);
}

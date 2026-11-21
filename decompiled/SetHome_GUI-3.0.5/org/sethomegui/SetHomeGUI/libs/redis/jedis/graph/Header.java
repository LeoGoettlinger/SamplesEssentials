package org.sethomegui.SetHomeGUI.libs.redis.jedis.graph;

import java.util.List;

@Deprecated
public interface Header {
   List<ResultSet.ColumnType> getSchemaTypes();

   List<String> getSchemaNames();
}

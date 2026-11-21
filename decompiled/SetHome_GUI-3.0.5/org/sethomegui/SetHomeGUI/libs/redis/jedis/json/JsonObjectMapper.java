package org.sethomegui.SetHomeGUI.libs.redis.jedis.json;

public interface JsonObjectMapper {
   <T> T fromJson(String var1, Class<T> var2);

   String toJson(Object var1);
}

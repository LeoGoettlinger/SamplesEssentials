package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

public interface ResponseConstructor<T, R extends MineSkinResponse<T>> {
   R construct(int var1, Map<String, String> var2, JsonObject var3, Gson var4, Class<T> var5);
}

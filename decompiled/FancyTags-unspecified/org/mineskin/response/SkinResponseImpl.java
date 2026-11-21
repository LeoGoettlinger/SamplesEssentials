package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import org.mineskin.data.SkinInfo;

public class SkinResponseImpl extends AbstractMineSkinResponse<SkinInfo> implements SkinResponse {
   public SkinResponseImpl(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, Class<SkinInfo> clazz) {
      super(status, headers, rawBody, gson, "skin", clazz);
   }

   @Override
   public SkinInfo getSkin() {
      return this.getBody();
   }
}

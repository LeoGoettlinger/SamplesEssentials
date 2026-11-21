package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;
import org.mineskin.data.RateLimitInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.data.UsageInfo;

public class GenerateResponseImpl extends AbstractMineSkinResponse<SkinInfo> implements GenerateResponse {
   private final RateLimitInfo rateLimit;
   private final UsageInfo usage;

   public GenerateResponseImpl(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, Class<SkinInfo> clazz) {
      super(status, headers, rawBody, gson, "skin", clazz);
      this.rateLimit = gson.fromJson(rawBody.get("rateLimit"), RateLimitInfo.class);
      this.usage = gson.fromJson(rawBody.get("usage"), UsageInfo.class);
   }

   @Override
   public SkinInfo getSkin() {
      return this.getBody();
   }

   @Override
   public RateLimitInfo getRateLimit() {
      return this.rateLimit;
   }

   @Override
   public UsageInfo getUsage() {
      return this.usage;
   }
}

package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import org.mineskin.data.JobInfo;

public class JobListResponseImpl extends AbstractMineSkinResponse<List<JobInfo>> implements JobListResponse {
   private static final TypeToken<List<JobInfo>> LIST_TYPE_TOKEN = new TypeToken<List<JobInfo>>() {};

   public JobListResponseImpl(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, Class<List<JobInfo>> clazz) {
      super(status, headers, rawBody, gson, "jobs", clazz);
   }

   protected List<JobInfo> parseBody(JsonObject rawBody, Gson gson, String primaryField, Class<List<JobInfo>> clazz) {
      return gson.fromJson(rawBody.get(primaryField), LIST_TYPE_TOKEN.getType());
   }

   @Override
   public List<JobInfo> getJobs() {
      return this.getBody();
   }
}

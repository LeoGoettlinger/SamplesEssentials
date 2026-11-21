package de.oliver.fancyanalytics.sdk.records;

import de.oliver.fancyanalytics.sdk.ApiClient;
import de.oliver.fancyanalytics.sdk.utils.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Future;

public class RecordService {
   private final ApiClient apiClient;

   public RecordService(ApiClient apiClient) {
      this.apiClient = apiClient;
   }

   public Future<Boolean> createRecord(String projectID, Record record) {
      return ApiClient.EXECUTOR
         .submit(
            () -> {
               HttpRequest request = new HttpRequest(this.apiClient.getBaseUrl() + "/v1/projects/" + projectID + "/records")
                  .withMethod("POST")
                  .withHeader("AuthorizationToken", this.apiClient.getProjectAuthToken())
                  .withBody(record);

               try {
                  HttpResponse<String> resp = request.send();
                  if (resp.statusCode() != 200) {
                     if (!this.apiClient.isDisableLogging()) {
                        ApiClient.LOGGER.severe("Failed to create record (status " + resp.statusCode() + "): " + resp.body());
                     }

                     return false;
                  }
               } catch (Exception var5) {
                  return false;
               }

               return true;
            }
         );
   }
}

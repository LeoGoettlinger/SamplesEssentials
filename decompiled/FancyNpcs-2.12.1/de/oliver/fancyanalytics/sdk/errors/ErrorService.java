package de.oliver.fancyanalytics.sdk.errors;

import de.oliver.fancyanalytics.sdk.ApiClient;
import de.oliver.fancyanalytics.sdk.utils.HttpRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.Future;

public class ErrorService {
   private final ApiClient apiClient;

   public ErrorService(ApiClient apiClient) {
      this.apiClient = apiClient;
   }

   public Future<Boolean> createError(String projectID, Error event) {
      return ApiClient.EXECUTOR
         .submit(
            () -> {
               HttpRequest request = new HttpRequest(this.apiClient.getBaseUrl() + "/v1/projects/" + projectID + "/errors")
                  .withMethod("POST")
                  .withHeader("AuthorizationToken", this.apiClient.getProjectAuthToken())
                  .withBody(event);

               try {
                  HttpResponse<String> resp = request.send();
                  if (resp.statusCode() != 200) {
                     if (!this.apiClient.isDisableLogging()) {
                        ApiClient.LOGGER.severe("Failed to create error (status " + resp.statusCode() + "): " + resp.body());
                     }

                     return false;
                  }
               } catch (HttpTimeoutException var5) {
                  return false;
               } catch (IOException | InterruptedException | URISyntaxException var6) {
                  if (!this.apiClient.isDisableLogging()) {
                     ApiClient.LOGGER.severe("Unexpected exception while creating error: " + var6.getMessage());
                  }

                  return false;
               }

               return true;
            }
         );
   }
}

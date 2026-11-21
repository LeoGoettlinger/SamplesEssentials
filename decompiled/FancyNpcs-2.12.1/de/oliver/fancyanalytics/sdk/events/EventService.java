package de.oliver.fancyanalytics.sdk.events;

import de.oliver.fancyanalytics.sdk.ApiClient;
import de.oliver.fancyanalytics.sdk.utils.HttpRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.Future;

public class EventService {
   private final ApiClient apiClient;

   public EventService(ApiClient apiClient) {
      this.apiClient = apiClient;
   }

   public Future<Boolean> createEvent(String projectID, Event event) {
      return ApiClient.EXECUTOR
         .submit(
            () -> {
               HttpRequest request = new HttpRequest(this.apiClient.getBaseUrl() + "/v1/projects/" + projectID + "/events")
                  .withMethod("POST")
                  .withHeader("AuthorizationToken", this.apiClient.getProjectAuthToken())
                  .withBody(event);

               try {
                  HttpResponse<String> resp = request.send();
                  if (resp.statusCode() != 201) {
                     if (!this.apiClient.isDisableLogging()) {
                        ApiClient.LOGGER.severe("Failed to create event (status " + resp.statusCode() + "): " + resp.body());
                     }

                     return false;
                  }
               } catch (HttpTimeoutException var5) {
                  return false;
               } catch (IOException | InterruptedException | URISyntaxException var6) {
                  if (!this.apiClient.isDisableLogging()) {
                     ApiClient.LOGGER.severe("Unexpected exception while creating event: " + var6.getMessage());
                  }

                  return false;
               }

               return true;
            }
         );
   }
}

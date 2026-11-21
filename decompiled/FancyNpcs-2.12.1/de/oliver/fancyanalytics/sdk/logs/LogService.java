package de.oliver.fancyanalytics.sdk.logs;

import de.oliver.fancyanalytics.sdk.ApiClient;
import de.oliver.fancyanalytics.sdk.utils.HttpRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.Future;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class LogService {
   private static final String BASE_URL = "https://fancyanalytics.net/collector";
   private final ApiClient apiClient;

   public LogService(ApiClient apiClient) {
      this.apiClient = apiClient;
   }

   public Future<Boolean> createLogRecord(String projectID, LogRecord logRecord) {
      if (projectID == null || projectID.isEmpty()) {
         throw new IllegalArgumentException("Project ID cannot be null or empty");
      } else if (logRecord == null) {
         throw new IllegalArgumentException("LogRecord cannot be null");
      } else if (!logRecord.projectId().equals(projectID)) {
         throw new IllegalArgumentException("LogRecord project ID does not match the provided project ID");
      } else {
         return ApiClient.EXECUTOR.submit(() -> {
            HttpRequest request = new HttpRequest("https://fancyanalytics.net/collector/api/v1/logs").withMethod("POST").withBody(logRecord);

            try {
               HttpResponse<String> resp = request.send();
               if (resp.statusCode() != 201) {
                  if (!this.apiClient.isDisableLogging()) {
                     ApiClient.LOGGER.severe("Failed to create log record (status " + resp.statusCode() + "): " + resp.body());
                  }

                  return false;
               }
            } catch (HttpTimeoutException var4) {
               return false;
            } catch (IOException | InterruptedException | URISyntaxException var5) {
               if (!this.apiClient.isDisableLogging()) {
                  ApiClient.LOGGER.severe("Unexpected exception while creating log record: " + var5.getMessage());
               }

               return false;
            }

            return true;
         });
      }
   }
}

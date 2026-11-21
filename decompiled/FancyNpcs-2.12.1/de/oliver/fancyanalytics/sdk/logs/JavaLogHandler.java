package de.oliver.fancyanalytics.sdk.logs;

import de.oliver.fancyanalytics.sdk.ApiClient;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class JavaLogHandler extends Handler {
   private final ApiClient apiClient;

   public JavaLogHandler(ApiClient apiClient) {
      this.apiClient = apiClient;
   }

   @Override
   public void publish(java.util.logging.LogRecord record) {
      LogRecord r = new LogRecord("", record.getLoggerName(), record.getInstant(), record.getLevel().getName(), record.getMessage(), null);

      try {
         Boolean success = this.apiClient.getLogService().createLogRecord("", r).get();
         if (!success && !this.apiClient.isDisableLogging()) {
            ApiClient.LOGGER.severe("Failed to publish log record: " + record.getMessage());
         }
      } catch (ExecutionException | InterruptedException var4) {
         if (!this.apiClient.isDisableLogging()) {
            ApiClient.LOGGER.severe("Failed to publish log record: " + var4.getMessage());
         }
      } catch (IllegalArgumentException var5) {
         if (!this.apiClient.isDisableLogging()) {
            ApiClient.LOGGER.warning("Invalid log record: " + var5.getMessage());
         }
      }
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws SecurityException {
   }
}

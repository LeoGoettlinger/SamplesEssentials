package de.oliver.fancyanalytics.sdk;

import de.oliver.fancyanalytics.sdk.errors.ErrorService;
import de.oliver.fancyanalytics.sdk.events.EventService;
import de.oliver.fancyanalytics.sdk.logs.LogService;
import de.oliver.fancyanalytics.sdk.records.RecordService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class ApiClient {
   public static final Logger LOGGER = Logger.getLogger("FancyAnalytics");
   public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(4, r -> new Thread(r, "FancyAnalytics-JavaSDK"));
   private final String baseUrl;
   private final String apiAuthToken;
   private final String projectAuthToken;
   private final RecordService recordService;
   private final EventService eventService;
   private final ErrorService errorService;
   private final LogService logService;
   private boolean disableLogging = false;

   public ApiClient(String baseUrl, String apiAuthToken, String projectAuthToken) {
      this.baseUrl = baseUrl;
      this.apiAuthToken = apiAuthToken;
      this.projectAuthToken = projectAuthToken;
      this.recordService = new RecordService(this);
      this.eventService = new EventService(this);
      this.errorService = new ErrorService(this);
      this.logService = new LogService(this);
   }

   public String getBaseUrl() {
      return this.baseUrl;
   }

   public String getApiAuthToken() {
      return this.apiAuthToken;
   }

   public String getProjectAuthToken() {
      return this.projectAuthToken;
   }

   public boolean isDisableLogging() {
      return this.disableLogging;
   }

   public void setDisableLogging(boolean disableLogging) {
      this.disableLogging = disableLogging;
   }

   public RecordService getRecordService() {
      return this.recordService;
   }

   public EventService getEventService() {
      return this.eventService;
   }

   public ErrorService getErrorService() {
      return this.errorService;
   }

   public LogService getLogService() {
      return this.logService;
   }
}

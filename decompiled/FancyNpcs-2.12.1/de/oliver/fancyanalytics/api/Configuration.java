package de.oliver.fancyanalytics.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class Configuration {
   private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private static final File CONFIG_FILE = new File("plugins/FancyAnalytics/config.json");
   private static final File OLD_CONFIG_FILE = new File("FancyAnalytics/config.yml");
   @SerializedName("sender_id")
   private final String senderID;
   @SerializedName("send_metrics")
   private boolean sendMetrics;
   @SerializedName("send_errors")
   private boolean sendErrors;
   @SerializedName("disable_logging")
   private boolean disableLogging;

   public Configuration(String senderID, boolean sendMetrics, boolean sendErrors, boolean disableLogging) {
      this.senderID = senderID;
      this.sendMetrics = sendMetrics;
      this.sendErrors = sendErrors;
      this.disableLogging = disableLogging;
   }

   public static Configuration loadConfig() {
      if (OLD_CONFIG_FILE.exists()) {
         try {
            CONFIG_FILE.getParentFile().mkdirs();
            Files.move(OLD_CONFIG_FILE.toPath(), CONFIG_FILE.toPath());
            Files.delete(OLD_CONFIG_FILE.getParentFile().toPath());
         } catch (Exception var5) {
            FancyAnalyticsAPI.LOGGER.warning("Failed to move old config file: " + var5.getMessage());
         }
      }

      if (!CONFIG_FILE.exists()) {
         Configuration config = new Configuration(UUID.randomUUID() + ":" + UUID.randomUUID() + ":" + UUID.randomUUID(), true, true, false);
         String json = gson.toJson(config);

         try {
            CONFIG_FILE.getParentFile().mkdirs();
            CONFIG_FILE.createNewFile();
            Files.write(CONFIG_FILE.toPath(), json.getBytes());
         } catch (Exception var3) {
            FancyAnalyticsAPI.LOGGER.warning("Failed to create config file: " + var3.getMessage());
         }

         return config;
      } else {
         try {
            String json = Files.readString(CONFIG_FILE.toPath());
            return gson.fromJson(json, Configuration.class);
         } catch (Exception var4) {
            FancyAnalyticsAPI.LOGGER.warning("Failed to load config file: " + var4.getMessage());
            return new Configuration(UUID.randomUUID() + ":" + UUID.randomUUID() + ":" + UUID.randomUUID(), true, true, false);
         }
      }
   }

   public String getSenderID() {
      return this.senderID;
   }

   public boolean sendMetrics() {
      return this.sendMetrics;
   }

   public void setSendMetrics(boolean sendMetrics) {
      this.sendMetrics = sendMetrics;
   }

   public boolean sendErrors() {
      return this.sendErrors;
   }

   public void sendErrors(boolean sendErrors) {
      this.sendErrors = sendErrors;
   }

   public boolean disableLogging() {
      return this.disableLogging;
   }

   public void setDisableLogging(boolean disableLogging) {
      this.disableLogging = disableLogging;
   }
}

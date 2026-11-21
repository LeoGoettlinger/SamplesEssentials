package dev.cwhead.GravesX.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

public final class MclogsUtil {
   private static final String API_URL = "https://api.mclo.gs/1/log";

   private MclogsUtil() {
   }

   public static String postLogToMclogs(String content) {
      HttpsURLConnection connection = null;

      String err;
      try {
         URL url = new URL("https://api.mclo.gs/1/log");
         connection = (HttpsURLConnection)url.openConnection();
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(15000);
         connection.setDoOutput(true);
         connection.setUseCaches(false);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("User-Agent", "GravesX/1 (+https://mclo.gs/)");
         String safeContent = content == null ? "" : content;
         String urlParameters = "content=" + URLEncoder.encode(safeContent, StandardCharsets.UTF_8.name());

         try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
            dataOutputStream.write(urlParameters.getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
         }

         int responseCode = connection.getResponseCode();
         boolean ok = responseCode >= 200 && responseCode < 300;

         try (
            InputStream is = ok ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is != null ? is : InputStream.nullInputStream(), StandardCharsets.UTF_8));
         ) {
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               sb.append(line);
            }

            if (!ok) {
               throw new IOException("HTTP " + responseCode + " Error response: " + sb);
            }

            JSONObject jsonResponse = new JSONObject(sb.toString());
            if (!jsonResponse.optBoolean("success", false)) {
               err = jsonResponse.optString("error", "unknown");
               throw new IOException("Log upload failed. Error: " + err);
            }

            err = jsonResponse.optString("url", null);
         }
      } catch (JSONException | IOException var28) {
         var28.printStackTrace();
         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }

      return err;
   }
}

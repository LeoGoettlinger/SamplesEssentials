package dev.cwhead.GravesX.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ToptalUtil {
   private static final String API_URL = "https://hastebin.com/documents";
   private static final String VIEW_BASE = "https://hastebin.com/";
   private static final int MAX_LEN = 400000;
   private static final int CONNECT_TIMEOUT_MS = 10000;
   private static final int READ_TIMEOUT_MS = 15000;
   private static final String USER_AGENT = "GravesX/1 ToptalUtil (+hastebin)";

   private ToptalUtil() {
   }

   public static String post(String content, String bearerToken) {
      if (content != null && !content.isEmpty()) {
         if (content.length() > 400000) {
            return null;
         } else {
            HttpURLConnection conn = null;

            String key;
            try {
               byte[] data = content.getBytes(StandardCharsets.UTF_8);
               conn = (HttpURLConnection)new URL("https://hastebin.com/documents").openConnection();
               setup(conn);
               if (bearerToken != null && !bearerToken.isBlank()) {
                  conn.setRequestProperty("Authorization", "Bearer " + bearerToken.trim());
               }

               conn.setFixedLengthStreamingMode(data.length);

               try (OutputStream os = conn.getOutputStream()) {
                  os.write(data);
               }

               int code = conn.getResponseCode();
               boolean ok = code >= 200 && code < 300;

               try (
                  InputStream is = ok ? conn.getInputStream() : (conn.getErrorStream() != null ? conn.getErrorStream() : InputStream.nullInputStream());
                  BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
               ) {
                  StringBuilder sb = new StringBuilder();

                  String line;
                  while ((line = br.readLine()) != null) {
                     sb.append(line);
                  }

                  if (ok) {
                     key = extractKey(sb.toString());
                     return key != null && !key.isBlank() ? "https://hastebin.com/" + key : null;
                  }

                  key = null;
               }
            } catch (IOException var27) {
               return null;
            } finally {
               if (conn != null) {
                  conn.disconnect();
               }
            }

            return key;
         }
      } else {
         return null;
      }
   }

   private static void setup(HttpURLConnection conn) throws ProtocolException {
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(15000);
      conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("User-Agent", "GravesX/1 ToptalUtil (+hastebin)");
   }

   private static String extractKey(String json) {
      if (json == null) {
         return null;
      } else {
         int idx = json.indexOf("\"key\"");
         if (idx < 0) {
            return null;
         } else {
            int colon = json.indexOf(58, idx);
            int q1 = json.indexOf(34, colon + 1);
            int q2 = q1 >= 0 ? json.indexOf(34, q1 + 1) : -1;
            return q1 >= 0 && q2 > q1 ? json.substring(q1 + 1, q2).trim() : null;
         }
      }
   }
}

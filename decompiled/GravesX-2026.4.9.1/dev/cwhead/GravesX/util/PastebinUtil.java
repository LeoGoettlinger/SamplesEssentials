package dev.cwhead.GravesX.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PastebinUtil {
   private static final String API_URL = "https://pastebin.com/api/api_post.php";

   private PastebinUtil() {
   }

   public static String post(String devKey, String userKey, String title, String content, String privacy, String expire) {
      if (devKey != null && !devKey.isBlank()) {
         int pastePrivate = mapPrivacyToInt(privacy);
         String exp = expire != null && !expire.isBlank() ? expire.toUpperCase(Locale.ROOT) : "1W";
         String safeTitle = title != null && !title.isBlank() ? title : "GravesX Dump " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
         HttpURLConnection conn = null;

         String var32;
         try {
            StringBuilder body = new StringBuilder();
            append(body, "api_dev_key", devKey);
            append(body, "api_option", "paste");
            append(body, "api_paste_code", content == null ? "" : content);
            append(body, "api_paste_private", String.valueOf(pastePrivate));
            append(body, "api_paste_name", safeTitle);
            append(body, "api_paste_expire_date", exp);
            append(body, "api_paste_format", "text");
            if (userKey != null && !userKey.isBlank()) {
               append(body, "api_user_key", userKey);
            }

            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            conn = (HttpURLConnection)new URL("https://pastebin.com/api/api_post.php").openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Accept", "text/plain");
            conn.setRequestProperty("User-Agent", "GravesX/1 PastebinUtil");
            conn.setFixedLengthStreamingMode(out.length);

            try (OutputStream os = conn.getOutputStream()) {
               os.write(out);
            }

            int code = conn.getResponseCode();
            InputStream stream = code >= 200 && code < 400 ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) {
               stream = InputStream.nullInputStream();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
               String resp = br.readLine();
               if (code < 200 || code >= 300 || resp == null || !resp.startsWith("http")) {
                  return null;
               }

               var32 = resp.trim();
            }
         } catch (IOException var28) {
            return null;
         } finally {
            if (conn != null) {
               conn.disconnect();
            }
         }

         return var32;
      } else {
         return null;
      }
   }

   private static int mapPrivacyToInt(String privacy) {
      String p = safeUpper(privacy, "UNLISTED");
      if ("PUBLIC".equals(p)) {
         return 0;
      } else {
         return "PRIVATE".equals(p) ? 2 : 1;
      }
   }

   private static void append(StringBuilder sb, String key, String val) {
      if (!sb.isEmpty()) {
         sb.append('&');
      }

      sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append('=').append(URLEncoder.encode(val == null ? "" : val, StandardCharsets.UTF_8));
   }

   private static String safeUpper(String in, String dflt) {
      return in != null && !in.isBlank() ? in.toUpperCase(Locale.ROOT) : dflt;
   }
}

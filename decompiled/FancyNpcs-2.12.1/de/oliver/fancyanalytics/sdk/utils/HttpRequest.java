package de.oliver.fancyanalytics.sdk.utils;

import com.google.gson.Gson;
import de.oliver.fancyanalytics.sdk.ApiClient;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpRequest {
   private static final Gson gson = new Gson();
   private static final HttpClient client = HttpClient.newBuilder().executor(ApiClient.EXECUTOR).connectTimeout(Duration.ofSeconds(5L)).build();
   private static AtomicInteger timeoutCount = new AtomicInteger(0);
   private static AtomicLong pauseRequestsUntil = new AtomicLong(0L);
   private final String url;
   private String method = "GET";
   private Object body = null;
   private Map<String, String> headers = new HashMap<>();
   private Duration timeout = Duration.ofSeconds(5L);

   public HttpRequest(String url, String method, Object body, Map<String, String> headers) {
      this.url = url;
      this.method = method;
      this.body = body;
      this.headers = headers;
   }

   public HttpRequest(String url) {
      this.url = url;
   }

   public HttpResponse<String> send() throws URISyntaxException, IOException, InterruptedException {
      if (System.currentTimeMillis() < pauseRequestsUntil.get()) {
         throw new HttpTimeoutException("Request paused due to previous timeouts.");
      } else {
         URI uri = new URI(this.url);
         Builder builder = java.net.http.HttpRequest.newBuilder().uri(uri).timeout(this.timeout).header("User-Agent", "FancyAnalytics Java-SDK");
         if (!this.method.equalsIgnoreCase("GET") && this.body != null) {
            String json = gson.toJson(this.body);
            if (!json.isEmpty()) {
               builder.method(this.method, BodyPublishers.ofString(json));
            }
         } else {
            builder.method(this.method, BodyPublishers.noBody());
         }

         if (!this.headers.isEmpty()) {
            for (Entry<String, String> entry : this.headers.entrySet()) {
               builder.header(entry.getKey(), entry.getValue());
            }
         }

         try {
            return client.send(builder.build(), BodyHandlers.ofString());
         } catch (HttpTimeoutException var7) {
            int timeouts = timeoutCount.incrementAndGet();
            if (timeouts >= 3) {
               long pauseUntil = System.currentTimeMillis() + 3600000L;
               pauseRequestsUntil.set(pauseUntil);
               timeoutCount.set(0);
            }

            throw var7;
         }
      }
   }

   public String getUrl() {
      return this.url;
   }

   public String getMethod() {
      return this.method;
   }

   public HttpRequest withMethod(String method) {
      this.method = method;
      return this;
   }

   public Object getBody() {
      return this.body;
   }

   public HttpRequest withBody(Object body) {
      this.body = body;
      return this;
   }

   public Map<String, String> getHeaders() {
      return this.headers;
   }

   public HttpRequest withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
   }

   public HttpRequest withHeader(String key, String value) {
      this.headers.put(key, value);
      return this;
   }

   public Duration getTimeout() {
      return this.timeout;
   }

   public HttpRequest withTimeout(Duration timeout) {
      this.timeout = timeout;
      return this;
   }
}

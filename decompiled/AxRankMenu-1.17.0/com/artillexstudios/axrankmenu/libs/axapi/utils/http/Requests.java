package com.artillexstudios.axrankmenu.libs.axapi.utils.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Requests {
   private static HttpRequest createRequest(RequestType type, String url, Map<String, String> headers, Supplier<String> bodySupplier) {
      Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

      builder = switch (type) {
         case GET -> builder.GET();
         case POST -> {
            if (bodySupplier == null) {
               throw new IllegalArgumentException("The body supplier of a POST request can't be null!");
            }

            String object = bodySupplier.get();
            if (object == null) {
               throw new IllegalArgumentException("The body of a POST request can't be null!");
            }

            yield builder.POST(BodyPublishers.ofString(object));
         }
         case DELETE -> builder.DELETE();
      };
      if (headers != null) {
         headers.forEach(builder::setHeader);
      }

      return builder.build();
   }

   private static Supplier<String> transformSupplier(Supplier<JsonObject> bodySupplier) {
      return () -> {
         JsonObject body = bodySupplier.get();
         if (body == null) {
            throw new IllegalArgumentException("The body of a POST request can't be null!");
         } else {
            return Requests.Holder.GSON.toJson(body);
         }
      };
   }

   public static HttpResponse<String> sendTyped(RequestType type, String url, Map<String, String> headers, Supplier<JsonObject> body) {
      HttpRequest request = createRequest(type, url, headers, transformSupplier(body));

      try {
         return Requests.Holder.CLIENT.send(request, BodyHandlers.ofString());
      } catch (InterruptedException | IOException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static CompletableFuture<HttpResponse<String>> sendTypedAsync(RequestType type, String url, Map<String, String> headers, Supplier<String> body) {
      HttpRequest request = createRequest(type, url, headers, body);
      return Requests.Holder.CLIENT.sendAsync(request, BodyHandlers.ofString());
   }

   public static HttpResponse<String> post(String url, Map<String, String> headers, Supplier<JsonObject> body) {
      return sendTyped(RequestType.POST, url, headers, body);
   }

   public static HttpResponse<String> delete(String url, Map<String, String> headers) {
      return sendTyped(RequestType.DELETE, url, headers, null);
   }

   public static HttpResponse<String> get(String url, Map<String, String> headers) {
      return sendTyped(RequestType.GET, url, headers, null);
   }

   public static CompletableFuture<HttpResponse<String>> postAsync(String url, Map<String, String> headers, Supplier<JsonObject> body) {
      return sendTypedAsync(RequestType.POST, url, headers, transformSupplier(body));
   }

   public static CompletableFuture<HttpResponse<String>> postAsyncString(String url, Map<String, String> headers, Supplier<String> body) {
      return sendTypedAsync(RequestType.POST, url, headers, body);
   }

   public static CompletableFuture<HttpResponse<String>> deleteAsync(String url, Map<String, String> headers) {
      return sendTypedAsync(RequestType.DELETE, url, headers, null);
   }

   public static CompletableFuture<HttpResponse<String>> getAsync(String url, Map<String, String> headers) {
      return sendTypedAsync(RequestType.GET, url, headers, null);
   }

   private static final class Holder {
      private static final HttpClient CLIENT = HttpClient.newHttpClient();
      private static final Gson GSON = new Gson();
   }
}

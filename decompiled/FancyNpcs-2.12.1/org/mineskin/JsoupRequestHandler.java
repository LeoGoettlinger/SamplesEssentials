package org.mineskin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.mineskin.data.CodeAndMessage;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.exception.MineskinException;
import org.mineskin.request.RequestHandler;
import org.mineskin.request.RequestHandlerConstructor;
import org.mineskin.request.RequestInterceptor;
import org.mineskin.request.ResponseInterceptor;
import org.mineskin.response.MineSkinResponse;
import org.mineskin.response.ResponseConstructor;

public class JsoupRequestHandler extends RequestHandler {
   private final String userAgent;
   private final String apiKey;
   private final int timeout;
   private final List<RequestInterceptor<Connection>> requestInterceptors;
   private final List<ResponseInterceptor<Connection.Response>> responseInterceptors;

   public JsoupRequestHandler(String baseUrl, String userAgent, String apiKey, int timeout, Gson gson) {
      this(baseUrl, userAgent, apiKey, timeout, gson, List.of(), List.of());
   }

   private JsoupRequestHandler(
      String baseUrl,
      String userAgent,
      String apiKey,
      int timeout,
      Gson gson,
      List<RequestInterceptor<Connection>> requestInterceptors,
      List<ResponseInterceptor<Connection.Response>> responseInterceptors
   ) {
      super(baseUrl, userAgent, apiKey, timeout, gson);
      this.userAgent = userAgent;
      this.apiKey = apiKey;
      this.timeout = timeout;
      this.requestInterceptors = requestInterceptors;
      this.responseInterceptors = responseInterceptors;
   }

   public static JsoupRequestHandler.Builder withRequestInterceptor(RequestInterceptor<Connection> interceptor) {
      return new JsoupRequestHandler.Builder().withRequestInterceptor(interceptor);
   }

   public static JsoupRequestHandler.Builder withResponseInterceptor(ResponseInterceptor<Connection.Response> interceptor) {
      return new JsoupRequestHandler.Builder().withResponseInterceptor(interceptor);
   }

   private Connection requestBase(Connection.Method method, String url) {
      url = this.baseUrl + url;
      MineSkinClientImpl.LOGGER.log(Level.FINE, method + " " + url);
      Connection connection = Jsoup.connect(url).method(method).userAgent(this.userAgent).ignoreContentType(true).ignoreHttpErrors(true).timeout(this.timeout);
      if (this.apiKey != null) {
         connection.header("Authorization", "Bearer " + this.apiKey);
      }

      return connection;
   }

   private Connection.Response execute(Connection connection) throws IOException {
      for (RequestInterceptor<Connection> interceptor : this.requestInterceptors) {
         interceptor.intercept(connection);
      }

      return connection.execute();
   }

   private <T, R extends MineSkinResponse<T>> R wrapResponse(Connection.Response response, Class<T> clazz, ResponseConstructor<T, R> constructor) {
      for (ResponseInterceptor<Connection.Response> interceptor : this.responseInterceptors) {
         interceptor.intercept(response);
      }

      try {
         JsonObject jsonBody = this.gson.fromJson(response.body(), JsonObject.class);
         R wrapped = constructor.construct(response.statusCode(), this.lowercaseHeaders(response.headers()), jsonBody, this.gson, clazz);
         if (!wrapped.isSuccess()) {
            throw new MineSkinRequestException(
               wrapped.getFirstError().map(CodeAndMessage::code).orElse("request_failed"),
               wrapped.getFirstError().map(CodeAndMessage::message).orElse("Request Failed"),
               wrapped
            );
         } else {
            return wrapped;
         }
      } catch (JsonParseException var6) {
         MineSkinClientImpl.LOGGER.log(Level.WARNING, "Failed to parse response body: " + response.body(), (Throwable)var6);
         throw new MineskinException("Failed to parse response", var6);
      }
   }

   private Map<String, String> lowercaseHeaders(Map<String, String> headers) {
      return headers.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Entry::getValue));
   }

   @Override
   public <T, R extends MineSkinResponse<T>> R getJson(String url, Class<T> clazz, ResponseConstructor<T, R> constructor) throws IOException {
      Connection.Response response = this.execute(this.requestBase(Connection.Method.GET, url));
      return this.wrapResponse(response, clazz, constructor);
   }

   @Override
   public <T, R extends MineSkinResponse<T>> R postJson(String url, JsonObject data, Class<T> clazz, ResponseConstructor<T, R> constructor) throws IOException {
      Connection connection = this.requestBase(Connection.Method.POST, url).requestBody(data.toString()).header("Content-Type", "application/json");
      Connection.Response response = this.execute(connection);
      return this.wrapResponse(response, clazz, constructor);
   }

   @Override
   public <T, R extends MineSkinResponse<T>> R postFormDataFile(
      String url, String key, String filename, InputStream in, Map<String, String> data, Class<T> clazz, ResponseConstructor<T, R> constructor
   ) throws IOException {
      Connection connection = this.requestBase(Connection.Method.POST, url).header("Content-Type", "multipart/form-data");
      connection.data(key, filename, in);
      connection.data(data);
      Connection.Response response = this.execute(connection);
      return this.wrapResponse(response, clazz, constructor);
   }

   public static final class Builder implements RequestHandlerConstructor {
      private final List<RequestInterceptor<Connection>> requestInterceptors = new ArrayList<>();
      private final List<ResponseInterceptor<Connection.Response>> responseInterceptors = new ArrayList<>();

      private Builder() {
      }

      public JsoupRequestHandler.Builder withRequestInterceptor(RequestInterceptor<Connection> interceptor) {
         this.requestInterceptors.add(interceptor);
         return this;
      }

      public JsoupRequestHandler.Builder withResponseInterceptor(ResponseInterceptor<Connection.Response> interceptor) {
         this.responseInterceptors.add(interceptor);
         return this;
      }

      @Override
      public RequestHandler construct(String baseUrl, String userAgent, String apiKey, int timeout, Gson gson) {
         return new JsoupRequestHandler(
            baseUrl, userAgent, apiKey, timeout, gson, List.copyOf(this.requestInterceptors), List.copyOf(this.responseInterceptors)
         );
      }
   }
}

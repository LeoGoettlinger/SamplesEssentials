package org.mineskin.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mineskin.data.CodeAndMessage;
import org.mineskin.data.MutableBreadcrumbed;

public abstract class AbstractMineSkinResponse<T> implements MineSkinResponse<T> {
   private final boolean success;
   private final int status;
   private final List<CodeAndMessage> messages;
   private final List<CodeAndMessage> errors;
   private final List<CodeAndMessage> warnings;
   private final Map<String, String> headers;
   private final String server;
   private final String breadcrumb;
   private final JsonObject rawBody;
   private final T body;

   public AbstractMineSkinResponse(int status, Map<String, String> headers, JsonObject rawBody, Gson gson, String primaryField, Class<T> clazz) {
      if (rawBody.has("success")) {
         this.success = rawBody.get("success").getAsBoolean();
      } else {
         this.success = status == 200;
      }

      this.status = status;
      this.messages = rawBody.has("messages") ? gson.fromJson(rawBody.get("messages"), CodeAndMessage.LIST_TYPE_TOKEN.getType()) : Collections.emptyList();
      this.warnings = rawBody.has("warnings") ? gson.fromJson(rawBody.get("warnings"), CodeAndMessage.LIST_TYPE_TOKEN.getType()) : Collections.emptyList();
      this.errors = rawBody.has("errors") ? gson.fromJson(rawBody.get("errors"), CodeAndMessage.LIST_TYPE_TOKEN.getType()) : Collections.emptyList();
      this.headers = headers.entrySet()
         .stream()
         .filter(e -> e.getKey().startsWith("mineskin-") || e.getKey().startsWith("x-mineskin-"))
         .collect(HashMap::new, (m, e) -> m.put(e.getKey().toLowerCase(), e.getValue()), HashMap::putAll);
      this.server = headers.get("mineskin-server");
      this.breadcrumb = headers.get("mineskin-breadcrumb");
      this.rawBody = rawBody;
      this.body = this.parseBody(rawBody, gson, primaryField, clazz);
      if (this.body instanceof MutableBreadcrumbed breadcrumbed) {
         breadcrumbed.setBreadcrumb(this.breadcrumb);
      }
   }

   protected T parseBody(JsonObject rawBody, Gson gson, String primaryField, Class<T> clazz) {
      return gson.fromJson(rawBody.get(primaryField), clazz);
   }

   @Override
   public boolean isSuccess() {
      return this.success;
   }

   @Override
   public int getStatus() {
      return this.status;
   }

   @Override
   public List<CodeAndMessage> getMessages() {
      return this.messages;
   }

   @Override
   public Optional<CodeAndMessage> getFirstMessage() {
      return this.messages.stream().findFirst();
   }

   @Override
   public List<CodeAndMessage> getErrors() {
      return this.errors;
   }

   @Override
   public boolean hasErrors() {
      return !this.errors.isEmpty();
   }

   @Override
   public Optional<CodeAndMessage> getFirstError() {
      return this.errors.stream().findFirst();
   }

   @Override
   public Optional<CodeAndMessage> getErrorOrMessage() {
      return this.getFirstError().or(this::getFirstMessage);
   }

   @Override
   public List<CodeAndMessage> getWarnings() {
      return this.warnings;
   }

   @Override
   public Optional<CodeAndMessage> getFirstWarning() {
      return this.warnings.stream().findFirst();
   }

   @Override
   public String getServer() {
      return this.server;
   }

   @Override
   public String getBreadcrumb() {
      return this.breadcrumb;
   }

   @Override
   public T getBody() {
      return this.body;
   }

   public JsonObject getRawBody() {
      return this.rawBody;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + "{success="
         + this.success
         + ", status="
         + this.status
         + ", server='"
         + this.server
         + "', breadcrumb='"
         + this.breadcrumb
         + "', headers="
         + this.headers
         + ", messages="
         + this.messages
         + ", errors="
         + this.errors
         + ", warnings="
         + this.warnings
         + "}\n"
         + this.rawBody;
   }
}

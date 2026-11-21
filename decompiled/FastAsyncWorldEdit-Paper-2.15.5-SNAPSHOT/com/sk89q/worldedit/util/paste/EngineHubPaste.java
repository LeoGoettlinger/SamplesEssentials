package com.sk89q.worldedit.util.paste;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.net.HttpRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class EngineHubPaste implements Paster {
   private static final Gson GSON = new Gson();

   @Override
   public Callable<URL> paste(String content, PasteMetadata metadata) {
      return new EngineHubPaste.PasteTask(content, metadata);
   }

   private static final class PasteTask implements Callable<URL> {
      private final String content;
      private final PasteMetadata metadata;

      private PasteTask(String content, PasteMetadata metadata) {
         this.content = content;
         this.metadata = metadata;
      }

      public URL call() throws IOException, InterruptedException {
         URL initialUrl = HttpRequest.url("https://paste.enginehub.org/signed_paste");
         HttpRequest requestBuilder = HttpRequest.get(initialUrl);
         requestBuilder.header("x-paste-meta-from", "EngineHub");
         if (this.metadata.name != null) {
            requestBuilder.header("x-paste-meta-name", this.metadata.name);
         }

         if (this.metadata.author != null) {
            requestBuilder.header("x-paste-meta-author", this.metadata.author);
         }

         if (this.metadata.extension != null) {
            requestBuilder.header("x-paste-meta-extension", this.metadata.extension);
         }

         EngineHubPaste.SignedPasteResponse response = (EngineHubPaste.SignedPasteResponse)EngineHubPaste.GSON
            .fromJson(
               requestBuilder.execute().expectResponseCode(200).returnContent().asString("UTF-8"),
               TypeToken.get(EngineHubPaste.SignedPasteResponse.class).getType()
            );
         HttpRequest.Form form = HttpRequest.Form.create();

         for (Entry<String, String> entry : response.uploadFields.entrySet()) {
            form.add(entry.getKey(), entry.getValue());
         }

         form.add("file", this.content);
         URL url = HttpRequest.url(response.uploadUrl);
         HttpRequest.post(url).bodyMultipartForm(form).execute().expectResponseCode(200, 204);
         return URI.create(response.viewUrl).toURL();
      }
   }

   private static final class SignedPasteResponse {
      String viewUrl;
      String uploadUrl;
      Map<String, String> uploadFields;
   }
}

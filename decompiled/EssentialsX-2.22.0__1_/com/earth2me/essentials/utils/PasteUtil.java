package com.earth2me.essentials.utils;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public final class PasteUtil {
   private static final String PASTE_URL = "https://pastes.dev/";
   private static final String PASTE_UPLOAD_URL = "https://api.pastes.dev/post";
   private static final ExecutorService PASTE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
   private static final Gson GSON = new Gson();

   private PasteUtil() {
   }

   public static CompletableFuture<PasteUtil.PasteResult> createPaste(List<PasteUtil.PasteFile> pages) {
      CompletableFuture<PasteUtil.PasteResult> future = new CompletableFuture<>();
      PASTE_EXECUTOR_SERVICE.submit(() -> {
         try {
            HttpURLConnection connection = (HttpURLConnection)new URL("https://api.pastes.dev/post").openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "EssentialsX plugin");
            connection.setRequestProperty("Content-Type", "text/json");
            connection.setRequestProperty("Content-Encoding", "gzip");
            JsonObject body = new JsonObject();
            JsonArray files = new JsonArray();

            for (PasteUtil.PasteFile page : pages) {
               JsonObject file = new JsonObject();
               JsonObject content = new JsonObject();
               file.addProperty("name", page.getName());
               content.addProperty("format", "text");
               content.addProperty("value", page.getContents());
               file.add("content", content);
               files.add(file);
            }

            body.add("files", files);
            OutputStream os = new GZIPOutputStream(connection.getOutputStream());

            try {
               os.write(body.toString().getBytes(Charsets.UTF_8));
            } catch (Throwable var10) {
               try {
                  os.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            os.close();
            if (connection.getResponseCode() >= 400) {
               future.completeExceptionally(new Error(CharStreams.toString(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))));
               return;
            }

            JsonObject object = (JsonObject)GSON.fromJson(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8), JsonObject.class);
            String pasteId = object.get("key").getAsString();
            String pasteUrl = "https://pastes.dev/" + pasteId;
            connection.disconnect();
            PasteUtil.PasteResult result = new PasteUtil.PasteResult(pasteId, pasteUrl, null);
            future.complete(result);
         } catch (Exception var11) {
            future.completeExceptionally(var11);
         }
      });
      return future;
   }

   public static class PasteFile {
      private final String name;
      private final String contents;

      public PasteFile(final String name, final String contents) {
         this.name = name;
         this.contents = contents;
      }

      public String getName() {
         return this.name;
      }

      public String getContents() {
         return this.contents;
      }
   }

   public static class PasteResult {
      private final String pasteId;
      private final String pasteUrl;
      @Nullable
      private final String deletionKey;

      protected PasteResult(String pasteId, final String pasteUrl, @Nullable final String deletionKey) {
         this.pasteId = pasteId;
         this.pasteUrl = pasteUrl;
         this.deletionKey = deletionKey;
      }

      public String getPasteUrl() {
         return this.pasteUrl;
      }

      @Nullable
      public String getDeletionKey() {
         return this.deletionKey;
      }

      public String getPasteId() {
         return this.pasteId;
      }
   }
}

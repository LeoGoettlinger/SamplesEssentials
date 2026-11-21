package de.theredend2000.advancedhunt.lib.kyori.adventure.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;

record ResourcePackInfoImpl(UUID id, URI uri, String hash) implements ResourcePackInfo {
   ResourcePackInfoImpl(final UUID id, final URI uri, final String hash) {
      this.id = Objects.requireNonNull(id, "id");
      this.uri = Objects.requireNonNull(uri, "uri");
      this.hash = Objects.requireNonNull(hash, "hash");
   }

   static CompletableFuture<String> computeHash(final URI uri, final Executor exec) {
      CompletableFuture<String> result = new CompletableFuture<>();
      exec.execute(() -> {
         try {
            URL url = uri.toURL();
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("User-Agent", "adventure/" + ResourcePackInfoImpl.class.getPackage().getSpecificationVersion() + " (pack-fetcher)");

            try (InputStream is = conn.getInputStream()) {
               MessageDigest digest = MessageDigest.getInstance("SHA-1");
               byte[] buf = new byte[8192];

               int read;
               while ((read = is.read(buf)) != -1) {
                  digest.update(buf, 0, read);
               }

               result.complete(bytesToString(digest.digest()));
            }
         } catch (NoSuchAlgorithmException | IOException var10) {
            result.completeExceptionally(var10);
         }
      });
      return result;
   }

   static String bytesToString(final byte[] arr) {
      StringBuilder builder = new StringBuilder(arr.length * 2);
      Formatter fmt = new Formatter(builder, Locale.ROOT);

      for (byte b : arr) {
         fmt.format("%02x", b & 255);
      }

      return builder.toString();
   }

   static final class BuilderImpl implements ResourcePackInfo.Builder {
      @Nullable
      private UUID id;
      @Nullable
      private URI uri;
      @Nullable
      private String hash;

      @Override
      public ResourcePackInfo.Builder id(final UUID id) {
         this.id = Objects.requireNonNull(id, "id");
         return this;
      }

      @Override
      public ResourcePackInfo.Builder uri(final URI uri) {
         this.uri = Objects.requireNonNull(uri, "uri");
         if (this.id == null) {
            this.id = UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8));
         }

         return this;
      }

      @Override
      public ResourcePackInfo.Builder hash(final String hash) {
         this.hash = Objects.requireNonNull(hash, "hash");
         return this;
      }

      @Override
      public ResourcePackInfo build() {
         return new ResourcePackInfoImpl(this.id, this.uri, this.hash);
      }

      @Override
      public CompletableFuture<ResourcePackInfo> computeHashAndBuild(final Executor executor) {
         return ResourcePackInfoImpl.computeHash(Objects.requireNonNull(this.uri, "uri"), executor).thenApply(hash -> {
            this.hash(hash);
            return this.build();
         });
      }
   }
}

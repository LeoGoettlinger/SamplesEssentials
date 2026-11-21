package ch.njol.skript.update;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class GithubChecker implements UpdateChecker {
   private final Gson gson = new Gson();

   private List<GithubChecker.ResponseEntry> deserialize(String str) {
      assert str != null : "Cannot deserialize null string";

      Type listType = (new TypeToken<List<GithubChecker.ResponseEntry>>() {}).getType();
      List<GithubChecker.ResponseEntry> responses = (List<GithubChecker.ResponseEntry>)this.gson.fromJson(str, listType);

      assert responses != null;

      return responses;
   }

   @Override
   public CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel) {
      CompletableFuture<UpdateManifest> future = CompletableFuture.<List>supplyAsync(() -> {
         URL url;
         try {
            url = new URL(manifest.updateSource);
         } catch (MalformedURLException var7) {
            throw new RuntimeException(var7);
         }

         try {
            List x2;
            try (Scanner scan = new Scanner(url.openStream(), "UTF-8")) {
               String out = scan.useDelimiter("\\A").next();

               assert out != null;

               x2 = this.deserialize(out);
            }

            return x2;
         } catch (IOException var9) {
            throw new RuntimeException(var9);
         }
      }).thenApply(releases -> {
         GithubChecker.ResponseEntry latest = null;
         GithubChecker.ResponseEntry current = null;

         for (GithubChecker.ResponseEntry release : releases) {
            String name = release.tag_name;

            assert name != null;

            if (latest == null && channel.check(name)) {
               latest = release;
            }

            if (manifest.id.equals(name)) {
               current = release;
               break;
            }
         }

         if (latest == null) {
            return null;
         } else if (current != null && latest.id == current.id) {
            return null;
         } else if (latest.assets.isEmpty()) {
            return null;
         } else {
            try {
               String namex = latest.tag_name;

               assert namex != null;

               String createdAt = latest.created_at;

               assert createdAt != null;

               String patchNotes = latest.body;

               assert patchNotes != null;

               URL download;
               if (manifest.downloadSource != null) {
                  download = new URL(manifest.downloadSource);
               } else {
                  download = new URL(latest.assets.get(0).browser_download_url);
               }

               return new UpdateManifest(namex, createdAt, patchNotes, download);
            } catch (MalformedURLException var9) {
               throw new RuntimeException(var9);
            }
         }
      });

      assert future != null;

      return future;
   }

   public static class ResponseEntry {
      public String url;
      public String assets_url;
      public String upload_url;
      public String html_url;
      public int id;
      public String tag_name;
      public String target_commitish;
      public String name;
      public boolean draft;
      public boolean prerelease;
      public String created_at;
      public String published_at;
      public List<GithubChecker.ResponseEntry.AssetsEntry> assets;
      public String body;
      public GithubChecker.ResponseEntry.Author author;

      @Override
      public String toString() {
         return this.tag_name;
      }

      public static class AssetsEntry {
         public int size;
         public int download_count;
         public String browser_download_url;
      }

      public static class Author {
         public String login;
         public int id;
      }
   }
}

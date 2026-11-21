package com.earth2me.essentials.updatecheck;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.adventure.ComponentHolder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class UpdateChecker {
   private static final String REPO = "EssentialsX/Essentials";
   private static final String BRANCH = "2.x";
   private static final String LATEST_RELEASE_URL = "https://api.github.com/repos/EssentialsX/Essentials/releases/latest";
   private static final String LATEST_RELEASE_PROXY_URL = "https://webapi.essentialsx.net/api/v1/github/essx-v2/releases/latest";
   private static final String DISTANCE_URL = "https://api.github.com/repos/EssentialsX/Essentials/compare/{0}...{1}";
   private static final String DISTANCE_PROXY_URL = "https://webapi.essentialsx.net/api/v1/github/essx-v2/compare/{0}/{1}";
   private final Essentials ess;
   private final String versionIdentifier;
   private final String versionBranch;
   private final boolean devBuild;
   private long lastFetchTime = 0L;
   private CompletableFuture<UpdateChecker.RemoteVersion> pendingDevFuture;
   private CompletableFuture<UpdateChecker.RemoteVersion> pendingReleaseFuture;
   private String latestRelease = null;
   private UpdateChecker.RemoteVersion cachedDev = null;
   private UpdateChecker.RemoteVersion cachedRelease = null;

   public UpdateChecker(Essentials ess) {
      String identifier = "INVALID";
      String branch = "INVALID";
      boolean dev = false;
      InputStream inputStream = UpdateChecker.class.getClassLoader().getResourceAsStream("release");
      if (inputStream != null) {
         List<String> versionStr = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
         if (versionStr.size() == 2) {
            if (versionStr.get(0).matches("\\d+\\.\\d+\\.\\d+-(?:dev|rc|beta|alpha)\\+\\d+-[0-9a-f]{7,40}")) {
               identifier = versionStr.get(0).split("-")[2];
               dev = true;
            } else {
               identifier = versionStr.get(0);
            }

            branch = versionStr.get(1);
         }
      }

      this.ess = ess;
      this.versionIdentifier = identifier;
      this.versionBranch = branch;
      this.devBuild = dev;
   }

   public boolean isDevBuild() {
      return this.devBuild;
   }

   public CompletableFuture<UpdateChecker.RemoteVersion> fetchLatestDev() {
      if (this.cachedDev != null && System.currentTimeMillis() - this.lastFetchTime <= 1800000L) {
         return CompletableFuture.completedFuture(this.cachedDev);
      } else if (this.pendingDevFuture != null) {
         return this.pendingDevFuture;
      } else {
         this.pendingDevFuture = new CompletableFuture<>();
         this.ess.runTaskAsynchronously(() -> {
            this.pendingDevFuture.complete(this.cachedDev = this.fetchDistance("2.x", this.getVersionIdentifier()));
            this.pendingDevFuture = null;
            this.lastFetchTime = System.currentTimeMillis();
         });
         return this.pendingDevFuture;
      }
   }

   public CompletableFuture<UpdateChecker.RemoteVersion> fetchLatestRelease() {
      if (this.cachedRelease != null && System.currentTimeMillis() - this.lastFetchTime <= 1800000L) {
         return CompletableFuture.completedFuture(this.cachedRelease);
      } else if (this.pendingReleaseFuture != null) {
         return this.pendingReleaseFuture;
      } else {
         this.pendingReleaseFuture = new CompletableFuture<>();
         this.ess
            .runTaskAsynchronously(
               () -> {
                  try {
                     HttpURLConnection connection = this.tryRequestWithFallback(
                        "https://api.github.com/repos/EssentialsX/Essentials/releases/latest",
                        "https://webapi.essentialsx.net/api/v1/github/essx-v2/releases/latest"
                     );
                     if (connection.getResponseCode() == 404) {
                        this.pendingReleaseFuture.complete(this.cachedRelease = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.UNKNOWN));
                     } else if (connection.getResponseCode() != 500 && connection.getResponseCode() != 403) {
                        try {
                           BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                           try {
                              this.latestRelease = ((JsonObject)new Gson().fromJson(reader, JsonObject.class)).get("tag_name").getAsString();
                              this.pendingReleaseFuture.complete(this.cachedRelease = this.fetchDistance(this.latestRelease, this.getVersionIdentifier()));
                           } catch (Throwable var6) {
                              try {
                                 reader.close();
                              } catch (Throwable var5) {
                                 var6.addSuppressed(var5);
                              }

                              throw var6;
                           }

                           reader.close();
                        } catch (NumberFormatException | JsonSyntaxException var7) {
                           var7.printStackTrace();
                           this.pendingReleaseFuture.complete(new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR));
                        }
                     } else {
                        this.pendingReleaseFuture.complete(new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR));
                     }
                  } catch (IOException var8) {
                     var8.printStackTrace();
                     this.pendingReleaseFuture.complete(new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR));
                  }

                  this.pendingReleaseFuture = null;
                  this.lastFetchTime = System.currentTimeMillis();
               }
            );
         return this.pendingReleaseFuture;
      }
   }

   public String getVersionIdentifier() {
      return this.versionIdentifier;
   }

   public String getVersionBranch() {
      return this.versionBranch;
   }

   public String getBuildInfo() {
      return "id:'" + this.getVersionIdentifier() + "' branch:'" + this.getVersionBranch() + "' isDev:" + this.isDevBuild();
   }

   public String getLatestRelease() {
      return this.latestRelease;
   }

   private HttpURLConnection tryRequestWithFallback(final String githubUrl, final String fallbackUrl) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)new URL(githubUrl).openConnection();

      try {
         connection.connect();
         if (connection.getResponseCode() != 500 && connection.getResponseCode() != 403) {
            return connection;
         }
      } catch (IOException var5) {
      }

      connection = (HttpURLConnection)new URL(fallbackUrl).openConnection();
      connection.connect();
      return connection;
   }

   private UpdateChecker.RemoteVersion tryProcessDistance(final HttpURLConnection connection) throws IOException {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

         UpdateChecker.RemoteVersion var13;
         label72: {
            label71: {
               label70: {
                  label69: {
                     try {
                        JsonObject obj = (JsonObject)new Gson().fromJson(reader, JsonObject.class);
                        String var4 = obj.get("status").getAsString();
                        switch (var4) {
                           case "identical":
                              var13 = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.IDENTICAL, 0);
                              break label72;
                           case "ahead":
                              var13 = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.AHEAD, 0);
                              break label71;
                           case "behind":
                              var13 = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.BEHIND, obj.get("behind_by").getAsInt());
                              break label70;
                           case "diverged":
                              var13 = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.DIVERGED, obj.get("behind_by").getAsInt());
                              break label69;
                        }

                        var13 = new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.UNKNOWN);
                     } catch (Throwable var8) {
                        try {
                           reader.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }

                        throw var8;
                     }

                     reader.close();
                     return var13;
                  }

                  reader.close();
                  return var13;
               }

               reader.close();
               return var13;
            }

            reader.close();
            return var13;
         }

         reader.close();
         return var13;
      } catch (NumberFormatException | JsonSyntaxException var9) {
         var9.printStackTrace();
         return new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR);
      }
   }

   private UpdateChecker.RemoteVersion fetchDistance(final String head, final String hash) {
      try {
         HttpURLConnection connection = this.tryRequestWithFallback(
            MessageFormat.format("https://api.github.com/repos/EssentialsX/Essentials/compare/{0}...{1}", head, hash),
            MessageFormat.format("https://webapi.essentialsx.net/api/v1/github/essx-v2/compare/{0}/{1}", head, hash)
         );
         if (connection.getResponseCode() == 404) {
            return new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.UNKNOWN);
         } else {
            return connection.getResponseCode() != 500 && connection.getResponseCode() != 403
               ? this.tryProcessDistance(connection)
               : new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR);
         }
      } catch (IOException var4) {
         var4.printStackTrace();
         return new UpdateChecker.RemoteVersion(UpdateChecker.BranchStatus.ERROR);
      }
   }

   public ComponentHolder[] getVersionMessages(final boolean sendLatestMessage, final boolean verboseErrors, final CommandSource source) {
      if (!this.ess.getSettings().isUpdateCheckEnabled()) {
         return new ComponentHolder[]{source.tlComponent("versionCheckDisabled")};
      } else if (this.isDevBuild()) {
         UpdateChecker.RemoteVersion latestDev = this.fetchLatestDev().join();
         switch (latestDev.getBranchStatus()) {
            case IDENTICAL:
               return sendLatestMessage ? new ComponentHolder[]{source.tlComponent("versionDevLatest")} : new ComponentHolder[0];
            case AHEAD:
            case DIVERGED:
               return new ComponentHolder[]{
                  source.tlComponent(latestDev.getDistance() == 0 ? "versionDevDivergedLatest" : "versionDevDiverged", latestDev.getDistance()),
                  source.tlComponent("versionDevDivergedBranch", this.getVersionBranch())
               };
            case BEHIND:
               return new ComponentHolder[]{
                  source.tlComponent("versionDevBehind", latestDev.getDistance()),
                  source.tlComponent("versionReleaseNewLink", "https://essentialsx.net/downloads.html")
               };
            case ERROR:
               return new ComponentHolder[]{source.tlComponent(verboseErrors ? "versionError" : "versionErrorPlayer", this.getBuildInfo())};
            case UNKNOWN:
               return verboseErrors ? new ComponentHolder[]{source.tlComponent("versionCustom", this.getBuildInfo())} : new ComponentHolder[0];
            default:
               return new ComponentHolder[0];
         }
      } else {
         UpdateChecker.RemoteVersion latestRelease = this.fetchLatestRelease().join();
         switch (latestRelease.getBranchStatus()) {
            case IDENTICAL:
               return sendLatestMessage ? new ComponentHolder[]{source.tlComponent("versionReleaseLatest")} : new ComponentHolder[0];
            case AHEAD:
            case DIVERGED:
            case UNKNOWN:
               return verboseErrors ? new ComponentHolder[]{source.tlComponent("versionCustom", this.getBuildInfo())} : new ComponentHolder[0];
            case BEHIND:
               return new ComponentHolder[]{
                  source.tlComponent("versionReleaseNew", this.getLatestRelease()),
                  source.tlComponent("versionReleaseNewLink", "https://essentialsx.net/downloads.html?branch=stable")
               };
            case ERROR:
               return new ComponentHolder[]{source.tlComponent(verboseErrors ? "versionError" : "versionErrorPlayer", this.getBuildInfo())};
            default:
               return new ComponentHolder[0];
         }
      }
   }

   private static enum BranchStatus {
      IDENTICAL,
      AHEAD,
      BEHIND,
      DIVERGED,
      ERROR,
      UNKNOWN;
   }

   private static class RemoteVersion {
      private final UpdateChecker.BranchStatus branchStatus;
      private final int distance;

      RemoteVersion(UpdateChecker.BranchStatus branchStatus) {
         this(branchStatus, 0);
      }

      RemoteVersion(UpdateChecker.BranchStatus branchStatus, int distance) {
         this.branchStatus = branchStatus;
         this.distance = distance;
      }

      public UpdateChecker.BranchStatus getBranchStatus() {
         return this.branchStatus;
      }

      public int getDistance() {
         return this.distance;
      }
   }
}

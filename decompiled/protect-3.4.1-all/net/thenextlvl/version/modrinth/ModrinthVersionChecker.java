package net.thenextlvl.version.modrinth;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.thenextlvl.version.Version;
import net.thenextlvl.version.VersionChecker;

public abstract class ModrinthVersionChecker<V extends Version> implements VersionChecker<ModrinthVersion, V> {
   private static final String API_URL = "https://api.modrinth.com/v2/project/%s/";
   private static final HttpClient client = HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_2).build();
   private static final Gson gson = new Gson();
   private Set<ModrinthVersion> versions = new HashSet<>();
   private final String id;

   public ModrinthVersionChecker(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   @Override
   public CompletableFuture<V> retrieveLatestVersion() {
      return this.retrieveModrinthVersions().thenApply(versions -> {
         List<ModrinthVersion> modrinthVersions = List.copyOf(versions);
         ModrinthVersion version = modrinthVersions.getFirst();
         return version.versionNumber();
      }).thenApply(this::parseVersion);
   }

   @Override
   public CompletableFuture<Set<V>> retrieveVersions() {
      return this.retrieveModrinthVersions().thenApply(versions -> versions.stream().map(this::parseVersion).collect(Collectors.toUnmodifiableSet()));
   }

   @Override
   public CompletableFuture<Optional<V>> retrieveLatestSupportedVersion() {
      return this.retrieveModrinthVersions()
         .thenApply(versions -> versions.stream().filter(this::isSupported).map(this::parseVersion).max(Comparable::compareTo));
   }

   @Override
   public Set<V> getSupportedVersions() {
      return this.versions.stream().filter(this::isSupported).map(this::parseVersion).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public Set<V> getVersions() {
      return this.versions.stream().map(this::parseVersion).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public Optional<V> getLatestSupportedVersion() {
      return this.versions.stream().filter(this::isSupported).map(this::parseVersion).max(Comparable::compareTo);
   }

   @Override
   public Optional<V> getLatestVersion() {
      return this.versions.stream().map(this::parseVersion).max(Comparable::compareTo);
   }

   public V parseVersion(ModrinthVersion version) {
      return this.parseVersion(version.name());
   }

   protected String getVersionQuery() {
      return "version";
   }

   public final CompletableFuture<Set<ModrinthVersion>> retrieveModrinthVersions() {
      return this.get(this.getVersionQuery()).thenApply(response -> {
         if (response.statusCode() != 200) {
            throw new IllegalStateException("Server responded with status code %s: %s".formatted(response.statusCode(), response.body()));
         } else {
            this.versions = Objects.requireNonNullElse((Set<ModrinthVersion>)gson.fromJson(response.body(), ModrinthVersions.class), this.versions);
            return this.versions;
         }
      });
   }

   private CompletableFuture<HttpResponse<String>> get(String path) {
      return client.sendAsync(
            HttpRequest.newBuilder()
               .uri(
                  URI.create(
                     "https://api.modrinth.com/v2/project/%s/".formatted(this.getId()) + path.replace("[", "%5B").replace("]", "%5D").replace("\"", "%22")
                  )
               )
               .build(),
            BodyHandlers.ofString()
         )
         .orTimeout(1L, TimeUnit.MINUTES);
   }
}

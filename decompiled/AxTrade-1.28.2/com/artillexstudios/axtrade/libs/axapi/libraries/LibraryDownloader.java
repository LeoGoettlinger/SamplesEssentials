package com.artillexstudios.axtrade.libs.axapi.libraries;

import com.artillexstudios.axtrade.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axtrade.libs.axapi.utils.file.FileUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class LibraryDownloader {
   private final LibraryCache libraryCache;
   private final TransitiveDependencyCollector collector;
   private final RelocationHelper helper;
   private final List<Library> libraries = new ArrayList<>();
   private final List<Repository> repositories = new ArrayList<>();
   private final List<Relocation> relocations = new ArrayList<>();
   private final List<Path> libraryPaths = new ArrayList<>();
   private final Path librariesFolder;
   private final boolean cleanIfNoCache;
   private int maxDepth;
   private boolean loaded = false;

   public LibraryDownloader(Path librariesFolder) {
      this(librariesFolder, false);
   }

   public LibraryDownloader(Path librariesFolder, boolean cleanIfNoCache) {
      this.librariesFolder = librariesFolder;
      this.cleanIfNoCache = cleanIfNoCache;
      this.libraryCache = new LibraryCache(this.librariesFolder);
      this.collector = new TransitiveDependencyCollector(this);
      if (this.cleanIfNoCache && !this.libraryCache.isCacheCreated()) {
         this.deletePath(librariesFolder);
      }

      this.libraryCache.load();
      this.addRepository(new Repository("https://repo.artillex-studios.com/releases/"));
      this.addRepository(new Repository("https://repo2.artillex-studios.com/releases/"));
      this.addRepository(new Repository("https://repo1.maven.org/maven2/"));
      this.helper = new RelocationHelper(this);
      this.loaded = true;
   }

   private void deletePath(Path path) {
      if (Files.exists(path)) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.info("Cleaning up library at path: {}", path);
         }

         FileUtils.deleteNested(path);
      }
   }

   public Path addLibrary(Library library) {
      return this.addLibrary(library, false);
   }

   public Path addLibrary(Library library, boolean transitive) {
      long start = System.currentTimeMillis();
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.info("Loading library {}", library);
      }

      if (this.isAlreadyLoaded(library)) {
         LogUtils.info("Library {} has already been loaded, skipping!", library);
         return null;
      } else {
         Library cachedLibrary = this.libraryCache.getCachedLibrary(library);
         Path path = this.getPath(library);
         if (cachedLibrary != null && path != null && Files.exists(path)) {
            if (FeatureFlags.DEBUG.get()) {
               LogUtils.info("Loading {} from cache...", library);
            }

            this.libraryPaths.add(path);
            this.libraries.add(cachedLibrary);

            for (Library transitiveDependency : cachedLibrary.transitiveDependencies()) {
               this.addLibrary(transitiveDependency, true);
            }

            LogUtils.info("Library {} loaded in {}ms from cache!", library, System.currentTimeMillis() - start);
            return path;
         } else if (path != null && Files.exists(path) && !this.loaded) {
            return path;
         } else {
            if (cachedLibrary != null) {
               LogUtils.info("Library {} has an older version, cleaning up...", library);
               this.cleanupFiles(cachedLibrary);
            }

            Library withTransitives = library;
            if (this.loaded) {
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.info("Searching for transitive dependencies of library: {}...", library);
               }

               withTransitives = this.collector.withTransitiveDependencies(library);
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.info("Found {} transitive dependencies of library: {}!", withTransitives.transitiveDependencies().size(), withTransitives);
               }

               if (!transitive) {
                  this.libraryCache.cache(withTransitives);
               }
            }

            if (this.loaded) {
               this.libraries.add(withTransitives);
            }

            this.downloadLibrary(withTransitives);
            LogUtils.info("Downloaded library {} in {}ms!", library, System.currentTimeMillis() - start);
            return path;
         }
      }
   }

   private boolean isAlreadyLoaded(Library library) {
      for (Library loaded : this.libraries) {
         if (LibraryCache.checkWithoutVersion(library, loaded)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public String getMd5Checksum(Library library) {
      for (Repository repository : this.repositories) {
         String md5Checksum = this.getMd5Checksum(repository, library);
         if (md5Checksum != null) {
            return md5Checksum;
         }
      }

      return null;
   }

   @Nullable
   public String getMd5Checksum(Repository repository, Library library) {
      try {
         String var4;
         try (InputStream stream = repository.getMd5ChecksumURI(library).toURL().openStream()) {
            var4 = new String(stream.readAllBytes(), StandardCharsets.UTF_8).strip();
         }

         return var4;
      } catch (IOException var8) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.error("Failed to get MD5 checksum of library {}", library);
         }

         return null;
      }
   }

   private void cleanupFiles(Library library) {
      for (Library transitiveDependency : library.transitiveDependencies()) {
         this.cleanupFiles(transitiveDependency);
      }

      this.deletePath(this.getPath(library));
   }

   private void downloadLibrary(Library library) {
      for (Library transitiveDependency : library.transitiveDependencies()) {
         this.downloadLibrary(transitiveDependency);
      }

      boolean found = false;
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.info("Downloading library {}...", library);
      }

      Path path = this.getPath(library);
      Path resolve = null;

      for (Repository repository : this.getRepositories()) {
         URI jarURI = repository.getJarURI(library);
         resolve = this.loaded ? path.resolveSibling(path.getFileName() + ".resolving") : path;

         try {
            try (
               InputStream inputStream = jarURI.toURL().openStream();
               FileOutputStream stream = new FileOutputStream(resolve.toFile());
            ) {
               byte[] bytes = new byte[8192];

               int length;
               while ((length = inputStream.read(bytes)) != -1) {
                  stream.write(bytes, 0, length);
               }

               String checksum = this.getMd5Checksum(library);
               String fileChecksum = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(resolve)));
               if (checksum == null || checksum.equalsIgnoreCase(fileChecksum)) {
                  this.libraryPaths.add(path);
                  found = true;
                  break;
               }

               LogUtils.info("Library {} failed checksum check! Downloading again...", library);
               this.downloadLibrary(library);
            }

            return;
         } catch (FileNotFoundException var18) {
         } catch (NoSuchAlgorithmException | IOException var19) {
            LogUtils.error("An exception occurred while downloading library {}!", library, var19);
         }
      }

      if (!found) {
         LogUtils.error("Failed to download library {}", library);
      }

      if (this.loaded && resolve != null && Files.exists(resolve)) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.info("Relocating library: {}", library);
         }

         this.helper.relocate(resolve, path, this.createRelocationsMap());
         this.deletePath(resolve);
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.info("Relocated library: {}", library);
         }
      }
   }

   public LibraryDownloader withMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
   }

   public void addRepository(Repository repository) {
      this.repositories.add(repository);
      if (!this.libraryCache.cache(repository)) {
         ;
      }
   }

   public void addRelocation(Relocation relocation) {
      this.relocations.add(relocation);
      if (!this.libraryCache.cache(relocation)) {
         if (FeatureFlags.DEBUG.get()) {
            LogUtils.info("Re-downloading libraries, as a new relocation has been added!");
         }

         this.deletePath(this.librariesFolder);
         this.libraryCache.save();
         this.collector.reset();

         for (Library library : this.libraries) {
            this.downloadLibrary(library);
         }
      }
   }

   private Map<String, String> createRelocationsMap() {
      Map<String, String> relocations = new HashMap<>();

      for (Relocation relocation : this.relocations) {
         relocations.put(relocation.from(), relocation.to());
      }

      return relocations;
   }

   private Path getPath(Library library) {
      return library == null
         ? null
         : this.librariesFolder
            .resolve(
               "%s.%s-%s%s.jar"
                  .formatted(
                     library.group(),
                     library.artifactId(),
                     library.version(),
                     library.classifier() != null && !library.classifier().isBlank() ? "-" + library.classifier() : ""
                  )
            );
   }

   public int getMaxDepth() {
      return this.maxDepth;
   }

   public List<Path> getLibraryPaths() {
      return Collections.unmodifiableList(this.libraryPaths);
   }

   public List<Repository> getRepositories() {
      return Collections.unmodifiableList(this.repositories);
   }
}

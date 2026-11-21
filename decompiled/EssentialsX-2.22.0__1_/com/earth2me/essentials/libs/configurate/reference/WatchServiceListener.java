package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.loader.ConfigurationLoader;
import com.earth2me.essentials.libs.configurate.reactive.Disposable;
import com.earth2me.essentials.libs.configurate.reactive.Subscriber;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

public final class WatchServiceListener implements AutoCloseable {
   private static final Kind<?>[] DEFAULT_WATCH_EVENTS = new Kind[]{
      StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
   };
   private static final int PARALLEL_THRESHOLD = 100;
   private static final ThreadFactory DEFAULT_THREAD_FACTORY = new PrefixedNameThreadFactory("Configurate-WatchService", true);
   private final WatchService watchService;
   private volatile boolean open = true;
   private final Thread executor;
   final Executor taskExecutor;
   private final ConcurrentHashMap<Path, DirectoryListenerRegistration> activeListeners = new ConcurrentHashMap<>();
   private static final ThreadLocal<IOException> exceptionHolder = new ThreadLocal<>();

   public static WatchServiceListener.Builder builder() {
      return new WatchServiceListener.Builder();
   }

   public static WatchServiceListener create() throws IOException {
      return new WatchServiceListener(DEFAULT_THREAD_FACTORY, FileSystems.getDefault(), ForkJoinPool.commonPool());
   }

   private WatchServiceListener(final ThreadFactory factory, final FileSystem fileSystem, final Executor taskExecutor) throws IOException {
      this.watchService = fileSystem.newWatchService();
      this.executor = factory.newThread(() -> {
         while (this.open) {
            WatchKey key;
            try {
               key = this.watchService.take();
            } catch (InterruptedException var8) {
               this.open = false;
               Thread.currentThread().interrupt();
               break;
            } catch (ClosedWatchServiceException var9) {
               break;
            }

            Path watched = (Path)key.watchable();
            DirectoryListenerRegistration registration = this.activeListeners.get(watched);
            if (registration != null) {
               Set<Object> seenContexts = new HashSet<>();

               for (WatchEvent<?> event : key.pollEvents()) {
                  if (!key.isValid()) {
                     break;
                  }

                  if (seenContexts.add(event.context())) {
                     registration.submit(event);
                     if (registration.closeIfEmpty()) {
                        key.cancel();
                        break;
                     }
                  }
               }

               if (!key.reset()) {
                  DirectoryListenerRegistration oldListeners = this.activeListeners.remove(watched);
                  oldListeners.onClose();
               }
            }

            try {
               Thread.sleep(20L);
            } catch (InterruptedException var7) {
               Thread.currentThread().interrupt();
               break;
            }
         }
      });
      this.taskExecutor = taskExecutor;
      this.executor.start();
   }

   private DirectoryListenerRegistration registration(final Path directory) throws ConfigurateException {
      DirectoryListenerRegistration reg = this.activeListeners.computeIfAbsent(directory, dir -> {
         try {
            return new DirectoryListenerRegistration(dir.register(this.watchService, DEFAULT_WATCH_EVENTS), this.taskExecutor);
         } catch (IOException var3) {
            exceptionHolder.set(var3);
            return null;
         }
      });
      if (reg == null) {
         throw new ConfigurateException("While adding listener for " + directory, exceptionHolder.get());
      } else {
         return reg;
      }
   }

   public Disposable listenToFile(Path file, final Subscriber<WatchEvent<?>> callback) throws ConfigurateException, IllegalArgumentException {
      file = file.toAbsolutePath();
      if (Files.isDirectory(file)) {
         throw new IllegalArgumentException("Path " + file + " must be a file");
      } else {
         Path fileName = file.getFileName();
         return this.registration(file.getParent()).subscribe(fileName, callback);
      }
   }

   public Disposable listenToDirectory(Path directory, final Subscriber<WatchEvent<?>> callback) throws ConfigurateException, IllegalArgumentException {
      directory = directory.toAbsolutePath();
      if (!Files.isDirectory(directory) && Files.exists(directory)) {
         throw new IllegalArgumentException("Path " + directory + " must be a directory");
      } else {
         return this.registration(directory).subscribe(callback);
      }
   }

   public <N extends ScopedConfigurationNode<N>> ConfigurationReference<N> listenToConfiguration(
      final Function<Path, ConfigurationLoader<? extends N>> loaderFunc, final Path path
   ) throws ConfigurateException {
      return ConfigurationReference.watching(loaderFunc, path, this);
   }

   @Override
   public void close() throws IOException {
      this.open = false;
      this.watchService.close();
      this.activeListeners.forEachValue(100L, DirectoryListenerRegistration::onClose);
      this.activeListeners.clear();

      try {
         this.executor.interrupt();
         this.executor.join();
      } catch (InterruptedException var2) {
         throw new IOException("Failed to await termination of executor thread!");
      }
   }

   public static final class Builder {
      @Nullable
      private ThreadFactory threadFactory;
      @Nullable
      private FileSystem fileSystem;
      @Nullable
      private Executor taskExecutor;

      private Builder() {
      }

      public WatchServiceListener.Builder threadFactory(final ThreadFactory factory) {
         this.threadFactory = Objects.requireNonNull(factory, "factory");
         return this;
      }

      public WatchServiceListener.Builder taskExecutor(final Executor executor) {
         this.taskExecutor = Objects.requireNonNull(executor, "executor");
         return this;
      }

      public WatchServiceListener.Builder fileSystem(final FileSystem system) {
         this.fileSystem = system;
         return this;
      }

      public WatchServiceListener build() throws IOException {
         if (this.threadFactory == null) {
            this.threadFactory = WatchServiceListener.DEFAULT_THREAD_FACTORY;
         }

         if (this.fileSystem == null) {
            this.fileSystem = FileSystems.getDefault();
         }

         if (this.taskExecutor == null) {
            this.taskExecutor = ForkJoinPool.commonPool();
         }

         return new WatchServiceListener(this.threadFactory, this.fileSystem, this.taskExecutor);
      }
   }
}

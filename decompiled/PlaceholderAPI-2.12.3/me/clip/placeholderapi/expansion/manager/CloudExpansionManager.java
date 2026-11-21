package me.clip.placeholderapi.expansion.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.util.Msg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CloudExpansionManager {
   @NotNull
   public static final String USER_AGENT;
   @NotNull
   private static final String API_URL = "https://ecloud.placeholderapi.com/api/v3/?platform=bukkit";
   @NotNull
   private static final Gson GSON;
   @NotNull
   private static final Type TYPE;
   @NotNull
   private final Collector<CloudExpansion, ?, Map<String, CloudExpansion>> INDEXED_NAME_COLLECTOR = Collectors.toMap(
      CloudExpansionManager::toIndexName, Function.identity()
   );
   @NotNull
   private final PlaceholderAPIPlugin plugin;
   @NotNull
   private final Map<String, CloudExpansion> cache = new HashMap<>();
   @NotNull
   private final Map<String, CompletableFuture<File>> await = new ConcurrentHashMap<>();
   private final ExecutorService ASYNC_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("placeholderapi-io-#%1$d").build());

   public CloudExpansionManager(@NotNull PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   private static String toIndexName(@NotNull String name) {
      return name.toLowerCase(Locale.ROOT).replace(' ', '_');
   }

   @NotNull
   private static String toIndexName(@NotNull CloudExpansion expansion) {
      return toIndexName(expansion.getName());
   }

   public void load() {
      this.clean();
      this.fetch();
   }

   public void kill() {
      this.clean();
   }

   @NotNull
   @Unmodifiable
   public Map<String, CloudExpansion> getCloudExpansions() {
      return ImmutableMap.copyOf(this.cache);
   }

   public boolean isEmpty() {
      return this.cache.isEmpty();
   }

   @NotNull
   @Unmodifiable
   public Map<String, CloudExpansion> getCloudExpansionsInstalled() {
      return this.cache.isEmpty()
         ? Collections.emptyMap()
         : this.cache.values().stream().filter(CloudExpansion::hasExpansion).collect(this.INDEXED_NAME_COLLECTOR);
   }

   @NotNull
   @Unmodifiable
   public Map<String, CloudExpansion> getCloudExpansionsByAuthor(@NotNull String author) {
      return this.cache.isEmpty()
         ? Collections.emptyMap()
         : this.cache.values().stream().filter(expansion -> author.equalsIgnoreCase(expansion.getAuthor())).collect(this.INDEXED_NAME_COLLECTOR);
   }

   @NotNull
   @Unmodifiable
   public Set<String> getCloudExpansionAuthors() {
      return this.cache.values().stream().map(CloudExpansion::getAuthor).collect(Collectors.toSet());
   }

   public int getCloudExpansionAuthorCount() {
      return this.getCloudExpansionAuthors().size();
   }

   public int getCloudUpdateCount() {
      return (int)this.plugin
         .getLocalExpansionManager()
         .getExpansions()
         .stream()
         .filter(expansion -> this.findCloudExpansionByName(expansion.getName()).map(CloudExpansion::shouldUpdate).orElse(false))
         .count();
   }

   @NotNull
   public Optional<CloudExpansion> findCloudExpansionByName(@NotNull String name) {
      return Optional.ofNullable(this.cache.get(toIndexName(name)));
   }

   public void clean() {
      this.cache.clear();
      this.await.values().forEach(future -> future.cancel(true));
      this.await.clear();
   }

   public void fetch() {
      this.plugin.getLogger().info("Fetching available expansion information...");
      this.ASYNC_EXECUTOR
         .submit(
            () -> {
               Map<String, CloudExpansion> values = new ConcurrentHashMap<>();

               try {
                  URI uri = new URI("https://ecloud.placeholderapi.com/api/v3/?platform=bukkit");
                  URLConnection connection = uri.toURL().openConnection();
                  connection.setRequestProperty("User-Agent", USER_AGENT);
                  InputStream in = connection.getInputStream();

                  String json;
                  try {
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                     json = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                  } catch (Throwable var10) {
                     if (in != null) {
                        try {
                           in.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (in != null) {
                     in.close();
                  }

                  values.putAll((Map<? extends String, ? extends CloudExpansion>)GSON.fromJson(json, TYPE));
                  List<String> toRemove = new ArrayList<>();

                  for (Entry<String, CloudExpansion> entry : values.entrySet()) {
                     CloudExpansion expansion = entry.getValue();
                     if (expansion.getLatestVersion() == null || expansion.getVersion(expansion.getLatestVersion()) == null) {
                        toRemove.add(entry.getKey());
                     }
                  }

                  for (String name : toRemove) {
                     values.remove(name);
                  }
               } catch (UnknownHostException var11) {
                  this.plugin
                     .getLogger()
                     .log(
                        Level.WARNING,
                        "There is no data available from the eCloud. Please try running /papi refresh. If this does not resolve the issue, the eCloud may be blocked by your firewall, server host, or service provider.\n\nMore information: https://placeholderapi.com/ecloud-blocked",
                        (Throwable)var11
                     );
               } catch (Throwable var12) {
                  this.plugin.getLogger().log(Level.WARNING, "Failed to download expansion information", var12);
               }

               this.plugin.getScheduler().runTask(() -> {
                  try {
                     for (Entry<String, CloudExpansion> entryx : values.entrySet()) {
                        String namex = entryx.getKey();
                        CloudExpansion expansionx = entryx.getValue();
                        expansionx.setName(namex);
                        Optional<PlaceholderExpansion> localOpt = this.plugin.getLocalExpansionManager().findExpansionByName(namex);
                        if (localOpt.isPresent()) {
                           PlaceholderExpansion local = localOpt.get();
                           if (local.isRegistered()) {
                              expansionx.setHasExpansion(true);
                              expansionx.setShouldUpdate(!local.getVersion().equalsIgnoreCase(expansionx.getLatestVersion()));
                           }
                        }

                        this.cache.put(toIndexName(expansionx), expansionx);
                     }
                  } catch (Throwable var8x) {
                     this.plugin.getLogger().log(Level.WARNING, "Failed to download expansion information", var8x);
                  }
               });
            }
         );
   }

   public boolean isDownloading(@NotNull CloudExpansion expansion) {
      return this.await.containsKey(toIndexName(expansion));
   }

   @NotNull
   public CompletableFuture<File> downloadExpansion(@NotNull CloudExpansion expansion, @NotNull CloudExpansion.Version version) {
      CompletableFuture<File> previous = this.await.get(toIndexName(expansion));
      if (previous != null) {
         return previous;
      } else {
         File file = new File(this.plugin.getLocalExpansionManager().getExpansionsFolder(), "Expansion-" + toIndexName(expansion) + ".jar");
         CompletableFuture<File> download = CompletableFuture.supplyAsync(() -> {
            try {
               URLConnection connection = new URI(version.getUrl()).toURL().openConnection();
               connection.setRequestProperty("User-Agent", USER_AGENT);
               ReadableByteChannel source = Channels.newChannel(connection.getInputStream());

               try {
                  FileOutputStream target = new FileOutputStream(file);

                  try {
                     target.getChannel().transferFrom(source, 0L, Long.MAX_VALUE);
                  } catch (Throwable var9) {
                     try {
                        target.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }

                     throw var9;
                  }

                  target.close();
               } catch (Throwable var10) {
                  if (source != null) {
                     try {
                        source.close();
                     } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                     }
                  }

                  throw var10;
               }

               if (source != null) {
                  source.close();
               }

               return file;
            } catch (URISyntaxException | IOException var11) {
               throw new CompletionException(var11);
            }
         }, this.ASYNC_EXECUTOR);
         download.whenCompleteAsync((value, exception) -> {
            this.await.remove(toIndexName(expansion));
            if (exception != null) {
               Msg.severe("Failed to download %s:%s", exception, expansion.getName(), expansion.getVersion());
            }
         }, this.ASYNC_EXECUTOR);
         this.await.put(toIndexName(expansion), download);
         return download;
      }
   }

   static {
      String userAgent;
      try {
         InputStream in = PlaceholderAPIPlugin.class.getResourceAsStream("/user-agent.txt");

         try {
            if (in == null) {
               userAgent = "PlaceholderAPI-Bukkit-null";
            } else {
               userAgent = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).readLine();
            }
         } catch (Throwable var5) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (in != null) {
            in.close();
         }
      } catch (IOException var6) {
         userAgent = "PlaceholderAPI-Bukkit-null";
      }

      USER_AGENT = userAgent;
      GSON = new Gson();
      TYPE = (new TypeToken<Map<String, CloudExpansion>>() {}).getType();
   }
}

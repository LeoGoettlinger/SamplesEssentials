package org.sethomegui.Storage;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;

public class StorageManager {
   private final SetHomeGUI plugin;
   private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
   private StorageProvider provider;
   private YamlStorageProvider yamlFallback;
   private RedisBridge redis;
   private ExecutorService writeExecutor;

   public StorageManager(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public void initialize() {
      Section storageConfig = this.plugin.getMainConfig().getSection("storage");
      String type = storageConfig != null ? storageConfig.getString("type", "YAML") : "YAML";
      this.yamlFallback = new YamlStorageProvider(this.plugin);
      this.yamlFallback.initialize();
      String normalizedType = type == null ? "YAML" : type.trim().toUpperCase();
      if (storageConfig != null && normalizedType.equals("MYSQL")) {
         if (this.setupMySql(storageConfig)) {
            this.setupRedis(storageConfig);
         }
      } else {
         if (!normalizedType.equals("YAML")) {
            this.plugin.getLogger().warning("storage.type '" + type + "' no existe. Valores validos: YAML, MYSQL. Se usaran archivos YAML.");
         }

         this.provider = this.yamlFallback;
      }

      this.plugin.getLogger().info("Almacenamiento activo: " + this.provider.getName() + (this.redis != null ? " + Redis (cache y sincronizacion)" : ""));
      if (this.provider != this.yamlFallback) {
         this.writeExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SetHomeGUI-Storage-Writer");
            thread.setDaemon(true);
            return thread;
         });
         this.migrateFromYamlIfRequested(storageConfig);
      }
   }

   private boolean setupMySql(Section storageConfig) {
      Section mysqlConfig = storageConfig.getSection("mysql");
      if (mysqlConfig == null) {
         this.plugin.getLogger().severe("storage.type es MYSQL pero falta la seccion storage.mysql en config.yml. Se usaran archivos YAML.");
         this.provider = this.yamlFallback;
         return false;
      } else {
         MySqlStorageProvider mysql = new MySqlStorageProvider(this.plugin, mysqlConfig);

         try {
            mysql.initialize();
            this.provider = mysql;
            return true;
         } catch (Throwable var5) {
            this.plugin.getLogger().severe("No se pudo conectar con MySQL: " + var5.getMessage());
            this.plugin.getLogger().severe("SetHomeGUI+ seguira funcionando con archivos YAML locales. Revisa storage.mysql en config.yml.");
            mysql.shutdown();
            this.provider = this.yamlFallback;
            return false;
         }
      }
   }

   private void setupRedis(Section storageConfig) {
      Section redisConfig = storageConfig.getSection("redis");
      if (redisConfig != null && redisConfig.getBoolean("enabled", false)) {
         RedisBridge bridge = new RedisBridge(this.plugin, redisConfig);

         try {
            bridge.initialize(this::invalidate);
            this.redis = bridge;
         } catch (Throwable var5) {
            this.plugin.getLogger().severe("No se pudo conectar con Redis: " + var5.getMessage());
            this.plugin.getLogger().severe("La sincronizacion entre servidores queda desactivada; MySQL sigue funcionando con normalidad.");
            bridge.shutdown();
         }
      }
   }

   private void migrateFromYamlIfRequested(Section storageConfig) {
      if (storageConfig != null && storageConfig.getBoolean("migrate-from-yaml", false)) {
         this.plugin.getLogger().info("Migracion activada: copiando los datos locales al backend externo...");
         int migrated = 0;
         int failed = 0;

         for (UUID uuid : this.yamlFallback.listPlayers()) {
            try {
               PlayerRecord record = this.yamlFallback.load(uuid);
               if (record != null) {
                  this.provider.save(uuid, record);
                  migrated++;
               }
            } catch (Exception var7) {
               failed++;
               this.plugin.getLogger().warning("No se pudo migrar los datos de " + uuid + ": " + var7.getMessage());
            }
         }

         this.plugin.getLogger().info("Migracion terminada: " + migrated + " jugadores copiados" + (failed > 0 ? ", " + failed + " con errores" : "") + ".");
         this.plugin.getLogger().info("Pon storage.migrate-from-yaml en false para no repetir la migracion en cada arranque.");
      }
   }

   public PlayerData get(UUID uuid) {
      PlayerData cached = this.cache.get(uuid);
      if (cached != null) {
         return cached;
      } else {
         try {
            PlayerData loaded = PlayerData.of(uuid, this, this.readRaw(uuid));
            PlayerData existing = this.cache.putIfAbsent(uuid, loaded);
            return existing != null ? existing : loaded;
         } catch (Exception var5) {
            String loadErrorMsg = this.plugin
               .getMainConfig()
               .getString("messages.system-errors.load-error", "Could not load or create data file for user: %uuid%");
            this.plugin.getLogger().severe(loadErrorMsg.replace("%uuid%", uuid.toString()));
            this.plugin.getLogger().severe("Detalle: " + var5.getMessage());
            return null;
         }
      }
   }

   private String readRaw(UUID uuid) throws Exception {
      if (this.redis != null) {
         String cachedYaml = this.redis.get(uuid);
         if (cachedYaml != null) {
            return cachedYaml;
         }
      }

      PlayerRecord record = this.provider.load(uuid);
      String yaml = record == null ? "{}" : PlayerDataCodec.toYaml(record);
      if (this.redis != null) {
         this.redis.put(uuid, yaml);
      }

      return yaml;
   }

   public void preload(UUID uuid) {
      this.cache.remove(uuid);
      this.get(uuid);
   }

   public void invalidate(UUID uuid) {
      this.cache.remove(uuid);
   }

   public void persist(UUID uuid, YamlDocument document) throws IOException {
      if (document instanceof PlayerData) {
         this.cache.put(uuid, (PlayerData)document);
      }

      String yaml = document.dump();
      if (this.provider == this.yamlFallback) {
         this.yamlFallback.saveRaw(uuid, yaml);
      } else {
         PlayerRecord record = PlayerDataCodec.toRecord(uuid, document);
         this.submitWrite(uuid, record, yaml);
      }
   }

   private void submitWrite(UUID uuid, PlayerRecord record, String yaml) {
      if (this.writeExecutor != null && !this.writeExecutor.isShutdown()) {
         this.writeExecutor.execute(() -> this.writeDirect(uuid, record, yaml));
      } else {
         this.writeDirect(uuid, record, yaml);
      }
   }

   private void writeDirect(UUID uuid, PlayerRecord record, String yaml) {
      try {
         this.provider.save(uuid, record);
         if (this.redis != null) {
            this.redis.put(uuid, yaml);
            this.redis.invalidate(uuid);
         }
      } catch (Exception var6) {
         String saveErrorMsg = this.plugin.getMainConfig().getString("messages.system-errors.save-error", "Could not save home data for UUID %uuid%");
         this.plugin.getLogger().severe(saveErrorMsg.replace("%uuid%", uuid.toString()));
         this.plugin.getLogger().severe("Detalle: " + var6.getMessage());
      }
   }

   public void delete(UUID uuid) {
      this.cache.remove(uuid);

      try {
         this.provider.delete(uuid);
         if (this.redis != null) {
            this.redis.invalidate(uuid);
         }
      } catch (Exception var3) {
         this.plugin.getLogger().warning("No se pudieron borrar los datos de " + uuid + ": " + var3.getMessage());
      }
   }

   public Set<UUID> listPlayers() {
      try {
         return this.provider.listPlayers();
      } catch (Exception var2) {
         this.plugin.getLogger().warning("No se pudo listar a los jugadores del backend: " + var2.getMessage());
         return new LinkedHashSet<>();
      }
   }

   public Map<UUID, Integer> countHomesByPlayer() {
      try {
         return this.provider.countHomesByPlayer();
      } catch (Exception var2) {
         this.plugin.getLogger().warning("No se pudo contar los hogares del backend: " + var2.getMessage());
         return new ConcurrentHashMap<>();
      }
   }

   public boolean isExternalBackend() {
      return this.provider != this.yamlFallback;
   }

   public String getBackendName() {
      return this.provider.getName();
   }

   public void shutdown() {
      if (this.writeExecutor != null) {
         this.writeExecutor.shutdown();

         try {
            if (!this.writeExecutor.awaitTermination(15L, TimeUnit.SECONDS)) {
               this.plugin.getLogger().warning("Quedaron escrituras pendientes al apagar el almacenamiento.");
            }
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }

      if (this.redis != null) {
         this.redis.shutdown();
      }

      if (this.provider != null) {
         this.provider.shutdown();
      }

      this.cache.clear();
   }
}

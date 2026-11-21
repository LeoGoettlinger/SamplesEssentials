package org.sethomegui.Storage;

import java.util.UUID;
import java.util.function.Consumer;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Jedis;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.JedisPool;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.JedisPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.JedisPubSub;

public class RedisBridge {
   private static final String CHANNEL = "sethomegui:invalidate";
   private final SetHomeGUI plugin;
   private final Section config;
   private final String serverId;
   private final String keyPrefix;
   private final int cacheSeconds;
   private JedisPool pool;
   private JedisPubSub subscriber;
   private Thread subscriberThread;
   private volatile boolean running;

   public RedisBridge(SetHomeGUI plugin, Section config) {
      this.plugin = plugin;
      this.config = config;
      this.serverId = config.getString("server-id", UUID.randomUUID().toString());
      this.keyPrefix = config.getString("key-prefix", "sethomegui:player:");
      this.cacheSeconds = Math.max(1, config.getInt("cache-seconds", 300));
   }

   public void initialize(Consumer<UUID> onRemoteInvalidate) throws Exception {
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(Math.max(1, this.config.getInt("pool-size", 8)));
      String host = this.config.getString("host", "localhost");
      int port = this.config.getInt("port", 6379);
      int timeout = Math.max(1000, this.config.getInt("timeout", 2000));
      String password = this.config.getString("password", "");
      int database = this.config.getInt("database", 0);
      this.pool = new JedisPool(
         poolConfig, host, port, timeout, password != null && !password.isEmpty() ? password : null, database, this.config.getBoolean("ssl", false)
      );

      try (Jedis jedis = this.pool.getResource()) {
         jedis.ping();
      }

      this.running = true;
      this.startSubscriber(onRemoteInvalidate);
   }

   private void startSubscriber(final Consumer<UUID> onRemoteInvalidate) {
      this.subscriber = new JedisPubSub() {
         public void onMessage(String channel, String message) {
            int separator = message.indexOf(124);
            if (separator > 0) {
               String origin = message.substring(0, separator);
               if (!origin.equals(RedisBridge.this.serverId)) {
                  try {
                     onRemoteInvalidate.accept(UUID.fromString(message.substring(separator + 1)));
                  } catch (IllegalArgumentException var6) {
                  }
               }
            }
         }
      };
      this.subscriberThread = new Thread(() -> {
         while (this.running) {
            try (Jedis jedis = this.pool.getResource()) {
               jedis.subscribe(this.subscriber, "sethomegui:invalidate");
            } catch (Exception var7) {
               if (!this.running) {
                  return;
               }

               this.plugin.getLogger().warning("Se perdio la conexion pub/sub con Redis, reintentando en 5s: " + var7.getMessage());

               try {
                  Thread.sleep(5000L);
               } catch (InterruptedException var4) {
                  Thread.currentThread().interrupt();
                  return;
               }
            }
         }
      }, "SetHomeGUI-Redis-Subscriber");
      this.subscriberThread.setDaemon(true);
      this.subscriberThread.start();
   }

   public String get(UUID uuid) {
      try {
         String var3;
         try (Jedis jedis = this.pool.getResource()) {
            var3 = jedis.get(this.keyPrefix + uuid);
         }

         return var3;
      } catch (Exception var7) {
         this.plugin.getLogger().warning("No se pudo leer de Redis (se usara el backend persistente): " + var7.getMessage());
         return null;
      }
   }

   public void put(UUID uuid, String yaml) {
      try (Jedis jedis = this.pool.getResource()) {
         jedis.setex(this.keyPrefix + uuid, this.cacheSeconds, yaml);
      } catch (Exception var8) {
         this.plugin.getLogger().warning("No se pudo escribir en Redis: " + var8.getMessage());
      }
   }

   public void invalidate(UUID uuid) {
      try (Jedis jedis = this.pool.getResource()) {
         jedis.del(this.keyPrefix + uuid);
         jedis.publish("sethomegui:invalidate", this.serverId + "|" + uuid);
      } catch (Exception var7) {
         this.plugin.getLogger().warning("No se pudo invalidar la cache de Redis: " + var7.getMessage());
      }
   }

   public void shutdown() {
      this.running = false;

      try {
         if (this.subscriber != null && this.subscriber.isSubscribed()) {
            this.subscriber.unsubscribe();
         }
      } catch (Exception var2) {
      }

      if (this.subscriberThread != null) {
         this.subscriberThread.interrupt();
      }

      if (this.pool != null && !this.pool.isClosed()) {
         this.pool.close();
      }
   }
}

package ac.grim.grimac.internal.storage.backend.redis.v2;

import ac.grim.grimac.api.storage.backend.AdminAdapter;
import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.SearchAdapter;
import ac.grim.grimac.api.storage.backend.TxAdapter;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.kind.Counter;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.internal.storage.backend.redis.RedisBackendConfig;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig.Builder;

@ApiStatus.Internal
public final class RedisBackendV2 implements BackendV2 {
   @NotNull
   private final RedisBackendConfig config;
   private Logger logger;
   private JedisPool pool;
   private RedisEntityAdapter entityAdapter;
   private RedisEventStreamAdapter eventStreamAdapter;
   private RedisKVScopedAdapter kvScopedAdapter;
   private RedisCounterAdapter counterAdapter;
   private RedisServerOwnershipAdapter ownershipAdapter;

   public RedisBackendV2(@NotNull RedisBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "redis-v2";
   }

   @NotNull
   @Override
   public ApiVersion apiVersion() {
      return ApiVersion.CURRENT;
   }

   @Override
   public int writerThreads(@NotNull Category<?> category) {
      return this.config.writerThreadsFor(category.id());
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(Capability.KIND_ENTITY, Capability.KIND_EVENT_STREAM, Capability.KIND_KV_SCOPED, Capability.KIND_COUNTER, Capability.ATOMIC_UPSERT);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("redis.clients.jedis.JedisPool");
      } catch (ClassNotFoundException var8) {
         throw new BackendException("Jedis not on the classpath: " + var8.getMessage(), var8);
      }

      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(8);
      Builder b = DefaultJedisClientConfig.builder().database(this.config.database()).timeoutMillis(this.config.timeoutMs());
      if (this.config.user() != null) {
         b.user(this.config.user());
      }

      if (this.config.password() != null) {
         b.password(this.config.password());
      }

      try {
         this.pool = new JedisPool(poolConfig, new HostAndPort(this.config.host(), this.config.port()), b.build());
         Jedis j = this.pool.getResource();

         try {
            j.ping();
         } catch (Throwable var9) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (j != null) {
            j.close();
         }
      } catch (RuntimeException var10) {
         throw new BackendException("failed to init Redis pool", var10);
      }

      String prefix = this.config.keyPrefix();
      this.entityAdapter = new RedisEntityAdapter(this.pool, prefix, this.logger);
      this.eventStreamAdapter = new RedisEventStreamAdapter(this.pool, prefix, this.logger);
      this.kvScopedAdapter = new RedisKVScopedAdapter(this.pool, prefix, this.logger);
      this.counterAdapter = new RedisCounterAdapter(this.pool, prefix, this.logger);
      this.ownershipAdapter = new RedisServerOwnershipAdapter(this.pool, prefix, this.logger);
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      if (this.pool != null) {
         try {
            this.pool.close();
         } catch (RuntimeException var2) {
            throw new BackendException("Redis close failed", var2);
         }
      }
   }

   @NotNull
   @Override
   public <K extends DataKind<?, ?>> Optional<KindAdapter<K>> adapterFor(@NotNull K kind) {
      if (kind instanceof Entity) {
         return Optional.of(this.entityAdapter);
      } else if (kind instanceof EventStream) {
         return Optional.of(this.eventStreamAdapter);
      } else if (kind instanceof KeyValueScoped) {
         return Optional.of(this.kvScopedAdapter);
      } else {
         return kind instanceof Counter ? Optional.of(this.counterAdapter) : Optional.empty();
      }
   }

   @NotNull
   @Override
   public Optional<SearchAdapter> searchAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<TxAdapter> txAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<AdminAdapter> adminAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<ServerOwnershipAdapter> ownershipAdapter() {
      return Optional.ofNullable(this.ownershipAdapter);
   }

   @NotNull
   @Override
   public <X> Optional<X> unwrap(@NotNull Class<X> type) {
      return type.isAssignableFrom(JedisPool.class) && this.pool != null ? Optional.of((X)this.pool) : Optional.empty();
   }
}

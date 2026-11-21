package ac.grim.grimac.internal.storage.backend.mongo.v2;

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
import ac.grim.grimac.internal.storage.backend.mongo.MongoBackendConfig;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class MongoBackendV2 implements BackendV2 {
   @NotNull
   private final MongoBackendConfig config;
   private MongoClient client;
   private MongoDatabase db;
   private Logger logger;
   private MongoEventStreamAdapter eventStreamAdapter;
   private MongoKeyValueScopedAdapter kvScopedAdapter;
   private MongoEntityAdapter entityAdapter;
   private MongoCounterAdapter counterAdapter;
   private MongoServerOwnershipAdapter ownershipAdapter;

   public MongoBackendV2(@NotNull MongoBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "mongo";
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
      return EnumSet.of(
         Capability.KIND_EVENT_STREAM,
         Capability.KIND_KV_SCOPED,
         Capability.KIND_ENTITY,
         Capability.KIND_COUNTER,
         Capability.BINARY_UUID_KEYS,
         Capability.MULTI_WRITER,
         Capability.ATOMIC_UPSERT,
         Capability.EVENT_STREAM_TIMESERIES_NATIVE,
         Capability.EVENT_STREAM_TTL_NATIVE,
         Capability.EVENT_STREAM_RANGE_BY_TIME
      );
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("com.mongodb.client.MongoClients");
      } catch (ClassNotFoundException var4) {
         throw new BackendException("mongodb-driver-sync not on the classpath — shade it into the plugin jar or drop it into server/plugins", var4);
      }

      try {
         this.client = MongoClients.create(this.config.connectionString());
         this.db = this.client.getDatabase(this.config.database());
         this.eventStreamAdapter = new MongoEventStreamAdapter(this.db, this.logger, this.config.batchFlushCap());
         this.kvScopedAdapter = new MongoKeyValueScopedAdapter(this.db, this.logger);
         this.entityAdapter = new MongoEntityAdapter(this.db, this.logger);
         this.counterAdapter = new MongoCounterAdapter(this.db, this.logger);
         this.ownershipAdapter = new MongoServerOwnershipAdapter(this.db, this.logger);
      } catch (RuntimeException var3) {
         throw new BackendException("failed to initialise Mongo backend v2", var3);
      }
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      try {
         if (this.client != null) {
            this.client.close();
         }
      } catch (RuntimeException var2) {
         throw new BackendException("failed to close Mongo client", var2);
      }
   }

   @NotNull
   @Override
   public <K extends DataKind<?, ?>> Optional<KindAdapter<K>> adapterFor(@NotNull K kind) {
      if (kind instanceof EventStream) {
         return Optional.of(this.eventStreamAdapter);
      } else if (kind instanceof KeyValueScoped) {
         return Optional.of(this.kvScopedAdapter);
      } else if (kind instanceof Entity) {
         return Optional.of(this.entityAdapter);
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
      if (type.isInstance(this.db)) {
         return Optional.of((X)this.db);
      } else {
         return type.isInstance(this.client) ? Optional.of((X)this.client) : Optional.empty();
      }
   }
}

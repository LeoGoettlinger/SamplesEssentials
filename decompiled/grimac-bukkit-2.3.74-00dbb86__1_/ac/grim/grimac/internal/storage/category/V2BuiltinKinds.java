package ac.grim.grimac.internal.storage.category;

import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.FieldKind;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.event.CheckCatalogEvent;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.ServerInstanceEvent;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.VerboseSchemaEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.Granularity;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.api.storage.model.CheckCatalogRecord;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.ServerInstanceRecord;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.VerboseSchemaRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.internal.storage.codec.MethodHandleCodecFactory;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApiStatus.Internal
public final class V2BuiltinKinds {
   private static final Codec<byte[]> OPAQUE_BYTES_CODEC = new Codec<byte[]>() {
      private final EncodeShape shape = new EncodeShape(
         "value",
         null,
         List.of(),
         List.of(),
         List.of(),
         List.of(new EncodeShape.FieldDef("value", byte[].class, FieldKind.VALUE, false, null, 0, 0, MergeMode.OVERWRITE, 0L)),
         1
      );

      @NotNull
      @Override
      public Class<byte[]> recordType() {
         return byte[].class;
      }

      @NotNull
      @Override
      public EncodeShape shape() {
         return this.shape;
      }

      @Override
      public int version() {
         return 1;
      }

      public Object indexField(@NotNull byte[] record, @NotNull String fieldName) {
         throw new UnsupportedOperationException("opaque-bytes codec has no introspectable fields; KV adapters write raw bytes");
      }
   };

   private V2BuiltinKinds() {
   }

   @NotNull
   public static EventStream<ViolationEvent, ViolationRecord> violations() {
      return EventStream.<ViolationEvent, ViolationRecord>builder()
         .name("violations")
         .event(ViolationEvent.class, ViolationEvent::new)
         .record(ViolationRecord.class)
         .codec(MethodHandleCodecFactory.get().create(ViolationRecord.class))
         .eventToRecord(V2BuiltinKinds::violationEventToRecord)
         .timestamp("occurred_at")
         .partition("session_id", "player_uuid", "check_id")
         .retention(Duration.ofDays(180L))
         .granularity(Granularity.SECONDS)
         .build();
   }

   @NotNull
   static ViolationRecord violationEventToRecord(@NotNull ViolationEvent e) {
      return new ViolationRecord(
         Objects.requireNonNull(e.id(), "ViolationEvent.id must be set before submit"),
         e.sessionId(),
         e.playerUuid(),
         e.checkId(),
         e.vl(),
         e.occurredEpochMs(),
         e.verboseData(),
         e.verboseFormat()
      );
   }

   @NotNull
   public static Entity<UUID, SessionEvent, SessionRecord> sessions() {
      return Entity.<UUID, SessionEvent, SessionRecord>builder()
         .name("sessions")
         .event(SessionEvent.class, SessionEvent::new)
         .record(SessionRecord.class)
         .codec(MethodHandleCodecFactory.get().create(SessionRecord.class))
         .eventToRecord(V2BuiltinKinds::sessionEventToRecord)
         .id(UUID.class, SessionRecord::sessionId)
         .secondaryIndex(IndexSpec.of("by_player_started", "player_uuid", "-started_at"))
         .secondaryIndex(IndexSpec.of("by_startup_open", "startup_id", "closed_at"))
         .secondaryIndex(IndexSpec.of("by_started", "-started_at"))
         .build();
   }

   @NotNull
   static SessionRecord sessionEventToRecord(@NotNull SessionEvent e) {
      return new SessionRecord(
         e.sessionId(),
         e.playerUuid(),
         e.serverName(),
         e.startedEpochMs(),
         e.lastActivityEpochMs(),
         e.closedAtEpochMs(),
         e.grimVersion(),
         e.clientBrand(),
         e.clientVersion(),
         e.serverVersionString(),
         e.instanceId(),
         e.startupId(),
         List.copyOf(e.sessionBlobs())
      );
   }

   @NotNull
   public static Entity<UUID, PlayerIdentityEvent, PlayerIdentity> players() {
      return Entity.<UUID, PlayerIdentityEvent, PlayerIdentity>builder()
         .name("players")
         .event(PlayerIdentityEvent.class, PlayerIdentityEvent::new)
         .record(PlayerIdentity.class)
         .codec(MethodHandleCodecFactory.get().create(PlayerIdentity.class))
         .eventToRecord(V2BuiltinKinds::playerEventToRecord)
         .id(UUID.class, PlayerIdentity::uuid)
         .secondaryIndex(new IndexSpec("by_name", List.of("current_name"), false, true))
         .build();
   }

   @NotNull
   static PlayerIdentity playerEventToRecord(@NotNull PlayerIdentityEvent e) {
      return new PlayerIdentity(e.uuid(), e.currentName(), e.firstSeenEpochMs(), e.lastSeenEpochMs());
   }

   @NotNull
   public static Entity<String, CheckCatalogEvent, CheckCatalogRecord> checks() {
      return Entity.<String, CheckCatalogEvent, CheckCatalogRecord>builder()
         .name("checks")
         .event(CheckCatalogEvent.class, CheckCatalogEvent::new)
         .record(CheckCatalogRecord.class)
         .codec(MethodHandleCodecFactory.get().create(CheckCatalogRecord.class))
         .eventToRecord(V2BuiltinKinds::checkCatalogEventToRecord)
         .id(String.class, CheckCatalogRecord::stableKey)
         .secondaryIndex(IndexSpec.unique("by_check_id", "check_id"))
         .build();
   }

   @NotNull
   static CheckCatalogRecord checkCatalogEventToRecord(@NotNull CheckCatalogEvent e) {
      String key = e.stableKey();
      if (key == null) {
         throw new IllegalStateException("CheckCatalogEvent.stableKey must be set before submit");
      } else {
         return new CheckCatalogRecord(key, e.checkId(), e.display(), e.description(), e.introducedVersion(), e.introducedAt());
      }
   }

   @NotNull
   public static Entity<String, VerboseSchemaEvent, VerboseSchemaRecord> verboseSchemas() {
      return Entity.<String, VerboseSchemaEvent, VerboseSchemaRecord>builder()
         .name("verbose_schemas")
         .event(VerboseSchemaEvent.class, VerboseSchemaEvent::new)
         .record(VerboseSchemaRecord.class)
         .codec(MethodHandleCodecFactory.get().create(VerboseSchemaRecord.class))
         .eventToRecord(V2BuiltinKinds::verboseSchemaEventToRecord)
         .id(String.class, VerboseSchemaRecord::schemaKey)
         .secondaryIndex(IndexSpec.of("by_flavor", "flavor"))
         .secondaryIndex(IndexSpec.of("by_check_id", "check_id"))
         .build();
   }

   @NotNull
   static VerboseSchemaRecord verboseSchemaEventToRecord(@NotNull VerboseSchemaEvent e) {
      String key = e.schemaKey();
      byte[] layout = e.layout();
      if (key == null) {
         throw new IllegalStateException("VerboseSchemaEvent.schemaKey must be set before submit");
      } else if (e.version() < 1) {
         throw new IllegalStateException("VerboseSchemaEvent.version must be positive before submit");
      } else if (layout == null) {
         throw new IllegalStateException("VerboseSchemaEvent.layout must be set before submit");
      } else {
         return new VerboseSchemaRecord(key, e.flavor(), e.checkId(), e.version(), layout, e.introducedAt());
      }
   }

   @NotNull
   public static Entity<UUID, ServerInstanceEvent, ServerInstanceRecord> instances() {
      return Entity.<UUID, ServerInstanceEvent, ServerInstanceRecord>builder()
         .name("server_instances")
         .event(ServerInstanceEvent.class, ServerInstanceEvent::new)
         .record(ServerInstanceRecord.class)
         .codec(MethodHandleCodecFactory.get().create(ServerInstanceRecord.class))
         .eventToRecord(V2BuiltinKinds::instanceEventToRecord)
         .id(UUID.class, ServerInstanceRecord::instanceId)
         .secondaryIndex(IndexSpec.of("by_server_name", "server_name"))
         .build();
   }

   @NotNull
   public static Entity<UUID, ServerStartupEvent, ServerStartupRecord> serverStartups() {
      return Entity.<UUID, ServerStartupEvent, ServerStartupRecord>builder()
         .name("server_startups")
         .event(ServerStartupEvent.class, ServerStartupEvent::new)
         .record(ServerStartupRecord.class)
         .codec(MethodHandleCodecFactory.get().create(ServerStartupRecord.class))
         .eventToRecord(V2BuiltinKinds::startupEventToRecord)
         .id(UUID.class, ServerStartupRecord::startupId)
         .secondaryIndex(IndexSpec.of("by_instance_open", "instance_id", "closed_at"))
         .secondaryIndex(IndexSpec.of("by_open_heartbeat", "closed_at", "last_heartbeat"))
         .secondaryIndex(IndexSpec.of("by_server_name", "server_name"))
         .build();
   }

   @NotNull
   public static KeyValueScoped<SettingScope, byte[]> settings() {
      return KeyValueScoped.<SettingScope, byte[]>builder().name("settings").scope(SettingScope.class).value(byte[].class, OPAQUE_BYTES_CODEC).build();
   }

   @NotNull
   static ServerInstanceRecord instanceEventToRecord(@NotNull ServerInstanceEvent e) {
      UUID id = e.instanceId();
      String name = e.serverName();
      if (id == null) {
         throw new IllegalStateException("ServerInstanceEvent.instanceId must be set before submit");
      } else if (name == null) {
         throw new IllegalStateException("ServerInstanceEvent.serverName must be set before submit");
      } else {
         return new ServerInstanceRecord(id, name, e.startedEpochMs(), e.lastHeartbeatEpochMs(), e.hostname(), e.grimVersion(), e.drainMode());
      }
   }

   @NotNull
   static ServerStartupRecord startupEventToRecord(@NotNull ServerStartupEvent e) {
      UUID startupId = e.startupId();
      UUID instanceId = e.instanceId();
      String name = e.serverName();
      if (startupId == null) {
         throw new IllegalStateException("ServerStartupEvent.startupId must be set before submit");
      } else if (instanceId == null) {
         throw new IllegalStateException("ServerStartupEvent.instanceId must be set before submit");
      } else if (name == null) {
         throw new IllegalStateException("ServerStartupEvent.serverName must be set before submit");
      } else {
         return new ServerStartupRecord(
            startupId,
            instanceId,
            name,
            e.grimVersion(),
            e.serverVersionString(),
            e.hostname(),
            e.startedEpochMs(),
            e.lastHeartbeatEpochMs(),
            e.closedAtEpochMs(),
            e.closeReason(),
            e.verboseManifest()
         );
      }
   }

   static {
      MethodHandleCodecFactory.installAsDefaultProvider();
   }
}

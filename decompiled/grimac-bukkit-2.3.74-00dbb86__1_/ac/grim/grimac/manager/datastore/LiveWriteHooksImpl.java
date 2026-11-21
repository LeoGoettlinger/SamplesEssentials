package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.checks.StableKeyMapping;
import ac.grim.grimac.internal.storage.identity.PlayerIdentityService;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.anticheat.LogUtil;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LiveWriteHooksImpl implements LiveWriteHooks {
   private final DataStore store;
   private final PlayerIdentityService identityService;
   private final CheckRegistry checkRegistry;
   private final SessionTracker sessionTracker;
   private final Map<String, Integer> checkIdCache = new ConcurrentHashMap<>();
   private final Set<String> missingStableKeyLogged = ConcurrentHashMap.newKeySet();

   public LiveWriteHooksImpl(
      @NotNull DataStore store, @NotNull PlayerIdentityService identityService, @NotNull CheckRegistry checkRegistry, @NotNull SessionTracker sessionTracker
   ) {
      this.store = store;
      this.identityService = identityService;
      this.checkRegistry = checkRegistry;
      this.sessionTracker = sessionTracker;
   }

   @Override
   public void onJoin(@NotNull UUID uuid, @Nullable String name, long now, @NotNull SessionTracker.ClientMeta meta) {
      this.identityService.observe(uuid, name, now);
      this.sessionTracker.observeActivity(uuid, now, meta);
   }

   @Override
   public void onQuit(@NotNull UUID uuid, long now, @NotNull SessionTracker.ClientMeta meta) {
      this.sessionTracker.close(uuid, now, meta);
   }

   @Override
   public void observeBrand(@NotNull UUID uuid, long now, @NotNull SessionTracker.ClientMeta meta) {
      this.sessionTracker.observeActivity(uuid, now, meta);
   }

   @Override
   public void recordFlag(
      @NotNull UUID playerUuid, @NotNull AbstractCheck check, double vl, @Nullable String verbose, long now, @NotNull SessionTracker.ClientMeta meta
   ) {
      UUID sessionId = this.sessionTracker.currentSessionId(playerUuid);
      if (sessionId == null) {
         sessionId = this.sessionTracker.observeActivity(playerUuid, now, meta);
      }

      int checkId = this.resolveCheckId(check);
      UUID sid = sessionId;
      this.store
         .submit(
            Categories.VIOLATION,
            e -> e.sessionId(sid).playerUuid(playerUuid).checkId(checkId).vl(vl).occurredEpochMs(now).verbose(verbose).verboseFormat(VerboseFormat.TEXT)
         );
   }

   @Override
   public void recordFlagData(
      @NotNull UUID playerUuid, @NotNull AbstractCheck check, double vl, @Nullable byte[] verboseData, long now, @NotNull SessionTracker.ClientMeta meta
   ) {
      UUID sessionId = this.sessionTracker.currentSessionId(playerUuid);
      if (sessionId == null) {
         sessionId = this.sessionTracker.observeActivity(playerUuid, now, meta);
      }

      int checkId = this.resolveCheckId(check);
      UUID sid = sessionId;
      byte[] payload = verboseData == null ? null : (byte[])verboseData.clone();
      this.store
         .submit(
            Categories.VIOLATION,
            e -> e.sessionId(sid)
               .playerUuid(playerUuid)
               .checkId(checkId)
               .vl(vl)
               .occurredEpochMs(now)
               .verboseData(payload)
               .verboseFormat(VerboseFormat.STRUCTURED_V1)
         );
   }

   @Override
   public void onJoinFromUserLogin(@NotNull PlatformPlayer player, @NotNull User user, long now) {
      GrimPlayer gp = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(user);
      this.onJoin(player.getUniqueId(), player.getName(), now, LiveWriteHooks.clientMetaFor(user, gp));
   }

   @Override
   public void onQuitFromUserDisconnect(@NotNull User user, @Nullable GrimPlayer grimPlayer, long now) {
      UUID uuid = user.getUUID();
      if (uuid != null) {
         this.onQuit(uuid, now, LiveWriteHooks.clientMetaFor(user, grimPlayer));
      }
   }

   @Override
   public void observeBrandFromCheck(@NotNull GrimPlayer grimPlayer) {
      UUID uuid = grimPlayer.user.getUUID();
      if (uuid != null) {
         this.observeBrand(uuid, System.currentTimeMillis(), LiveWriteHooks.clientMetaFor(grimPlayer.user, grimPlayer));
      }
   }

   @Override
   public void recordFlagFromCheck(@NotNull GrimPlayer player, @NotNull AbstractCheck check, double vl, @Nullable String verbose) {
      try {
         this.recordFlag(player.uuid, check, vl, verbose, System.currentTimeMillis(), SessionTracker.ClientMeta.empty());
      } catch (RuntimeException var7) {
         LogUtil.warn("v1 datastore recordFlag failed: " + var7.getMessage());
      }
   }

   @Override
   public void recordFlagDataFromCheck(@NotNull GrimPlayer player, @NotNull AbstractCheck check, double vl, @Nullable byte[] verboseData) {
      try {
         this.recordFlagData(player.uuid, check, vl, verboseData, System.currentTimeMillis(), SessionTracker.ClientMeta.empty());
      } catch (RuntimeException var7) {
         LogUtil.warn("v1 datastore recordFlag failed: " + var7.getMessage());
      }
   }

   private int resolveCheckId(@NotNull AbstractCheck check) {
      String display = check.getCheckName();
      String key = display.toLowerCase(Locale.ROOT);
      Integer cached = this.checkIdCache.get(key);
      if (cached != null) {
         return cached;
      } else {
         String declaredStable = check.getStableKey();
         String stable;
         if (declaredStable != null && !declaredStable.isEmpty()) {
            stable = declaredStable;
         } else {
            if (this.missingStableKeyLogged.add(key)) {
               LogUtil.warn(
                  "[grim-history] check "
                     + display
                     + " has no stableKey declared; falling back to StableKeyMapping. Populate the @CheckData.stableKey / CheckInfo.stableKey field."
               );
            }

            stable = StableKeyMapping.stableKeyFor(display).orElse(StableKeyMapping.legacyFallback(display));
         }

         String description = check.getDescription();
         String introducedVersion = safePluginVersion();
         int id = this.checkRegistry.intern(stable, display, description, introducedVersion);
         this.checkIdCache.put(key, id);
         return id;
      }
   }

   @Nullable
   private static String safePluginVersion() {
      try {
         return GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
      } catch (RuntimeException var1) {
         return null;
      }
   }
}

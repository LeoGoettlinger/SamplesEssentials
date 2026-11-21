package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@Deprecated(
   forRemoval = true,
   since = "phase0"
)
@ApiStatus.Experimental
public final class Queries {
   private Queries() {
   }

   public static Queries.ListSessionsByPlayer listSessionsByPlayer(UUID player, int pageSize, @Nullable Cursor cursor) {
      return new Queries.ListSessionsByPlayer(player, pageSize, cursor);
   }

   public static Queries.GetSessionById getSessionById(UUID sessionId) {
      return new Queries.GetSessionById(sessionId);
   }

   public static Queries.ListViolationsInSession listViolationsInSession(UUID sessionId, int pageSize, @Nullable Cursor cursor) {
      return new Queries.ListViolationsInSession(sessionId, pageSize, cursor);
   }

   public static Queries.GetPlayerIdentity getPlayerIdentity(UUID uuid) {
      return new Queries.GetPlayerIdentity(uuid);
   }

   public static Queries.GetPlayerIdentityByName getPlayerIdentityByName(String name) {
      return new Queries.GetPlayerIdentityByName(name);
   }

   public static Queries.ListPlayersByNamePrefix listPlayersByNamePrefix(String lowerPrefix, int limit) {
      return new Queries.ListPlayersByNamePrefix(lowerPrefix, limit);
   }

   public static Queries.GetSetting getSetting(SettingScope scope, String scopeKey, String key) {
      return new Queries.GetSetting(scope, scopeKey, key);
   }

   public record GetPlayerIdentity(UUID uuid) implements Query<PlayerIdentity> {
   }

   public record GetPlayerIdentityByName(String name) implements Query<PlayerIdentity> {
   }

   public record GetSessionById(UUID sessionId) implements Query<SessionRecord> {
   }

   public record GetSetting(SettingScope scope, String scopeKey, String key) implements Query<SettingRecord> {
   }

   public record ListPlayersByNamePrefix(String lowerPrefix, int limit) implements Query<PlayerIdentity> {
   }

   public record ListSessionsByPlayer(UUID player, int pageSize, @Nullable Cursor cursor) implements Query<SessionRecord> {
   }

   public record ListViolationsInSession(UUID sessionId, int pageSize, @Nullable Cursor cursor) implements Query<ViolationRecord> {
   }
}

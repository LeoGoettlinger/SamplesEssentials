package su.nightexpress.nightcore.userdata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.db.data.DataCache;
import su.nightexpress.nightcore.db.state.StatefulData;
import su.nightexpress.nightcore.util.LowerCase;

public class UserDataCache implements DataCache<UserData> {
   private final Map<UUID, UserData> byId = new ConcurrentHashMap<>();
   private final Map<String, UserData> byName = new ConcurrentHashMap<>();

   public boolean contains(@NonNull UserData data) {
      return this.contains(data.getId());
   }

   public boolean contains(@NonNull UUID playerId) {
      return this.byId.containsKey(playerId);
   }

   @Override
   public void clear() {
      this.byId.clear();
      this.byName.clear();
   }

   @Override
   public void clearExpired() {
      this.removeIf(StatefulData::isCacheExpired);
   }

   @Override
   public void clearRemoved() {
      this.removeIf(StatefulData::isRemoved);
   }

   @Override
   public void removeIf(@NonNull Predicate<UserData> predicate) {
      this.byId.values().removeIf(predicate);
      this.byName.values().removeIf(predicate);
   }

   public void put(@NonNull UserData data) {
      this.byId.put(data.getId(), data);
      this.byName.put(LowerCase.INTERNAL.apply(data.getName()), data);
   }

   public void remove(@NonNull UserData data) {
      this.byId.remove(data.getId());
      this.byName.remove(LowerCase.INTERNAL.apply(data.getName()));
   }

   @Nullable
   public UserData getById(@NonNull UUID playerId) {
      return this.byId.get(playerId);
   }

   @Nullable
   public UserData getByName(@NonNull String name) {
      return this.byName.get(LowerCase.INTERNAL.apply(name));
   }

   @NonNull
   public Optional<UserData> byId(@NonNull UUID playerId) {
      return Optional.ofNullable(this.getById(playerId));
   }

   @NonNull
   public Optional<UserData> byName(@NonNull String name) {
      return Optional.ofNullable(this.getByName(name));
   }

   @NonNull
   @Override
   public Stream<UserData> streamAll() {
      return this.byId.values().stream();
   }

   @NonNull
   @Override
   public Set<UserData> getAll() {
      return Set.copyOf(this.byId.values());
   }
}

package su.nightexpress.nightcore.user;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.user.cache.UserRepository;
import su.nightexpress.nightcore.user.data.UserDataAccessor;
import su.nightexpress.nightcore.user.data.UserDataSettings;
import su.nightexpress.nightcore.user.listener.UserListener;
import su.nightexpress.nightcore.util.Players;

public abstract class AbstractUserManager<P extends NightPlugin, U extends UserTemplate> extends AbstractManager<P> {
   protected final UserDataAccessor<U> dataAccessor;
   protected final UserRepository<U> repository;
   protected final UserDataSettings settings;

   public AbstractUserManager(@NonNull P plugin, @NonNull UserDataAccessor<U> dataAccessor) {
      super(plugin);
      this.dataAccessor = dataAccessor;
      this.repository = new UserRepository<>();
      this.settings = new UserDataSettings();
   }

   @Override
   protected void onLoad() {
      this.settings.load(this.plugin.getEngineConfig());
      this.addListener(new UserListener<>(this.plugin, this));
      this.addAsyncTask(this::saveDirty, this.settings.getSaveInterval());
      this.addAsyncTask(this.repository::cleanExpired, this.settings.getCacheCleanupInterval());
      this.plugin.runTaskAsync(() -> this.dataAccessor.loadProfiles().forEach(userInfo -> this.repository.addNameIdMapping(userInfo.name(), userInfo.id())));
      this.plugin.onPostLoad(() -> {
         this.loadOnline();
         this.dataAccessor.addSynchronization(this::synchronizeUserData);
      });
   }

   @Override
   protected void onShutdown() {
      this.saveDirty();
      this.repository.clear();
   }

   private void loadOnline() {
      Players.stream().<UUID>map(Entity::getUniqueId).map(this::getOrFetch).filter(Optional::isPresent).map(Optional::get).forEach(this::cachePermanent);
   }

   private void synchronizeUserData(@NonNull U user) {
      if (!this.repository.contains(user.getId())) {
         this.cacheTemporary(user);
      } else {
         this.repository.getById(user.getId()).ifPresent(cached -> {
            if (!cached.isDirty()) {
               this.synchronize(user, (U)cached);
            }
         });
      }
   }

   @NonNull
   protected abstract U create(@NonNull UUID var1, @NonNull String var2, @NonNull InetAddress var3);

   protected abstract void synchronize(@NonNull U var1, @NonNull U var2);

   @NonNull
   public UserDataAccessor<U> getDataAccessor() {
      return this.dataAccessor;
   }

   @NonNull
   public UserRepository<U> getRepository() {
      return this.repository;
   }

   protected void cacheTemporary(@NonNull U user) {
      this.repository.addTemporary(user, this.settings.getCacheLifetime());
   }

   protected void cachePermanent(@NonNull U user) {
      this.repository.addPermanent(user);
   }

   public final void handlePreLogin(@NonNull AsyncPlayerPreLoginEvent event) {
      if (event.getLoginResult() == Result.ALLOWED) {
         UUID uuid = event.getUniqueId();
         if (!this.dataAccessor.isExists(uuid)) {
            U user = this.create(uuid, event.getName(), event.getAddress());
            this.dataAccessor.insert(user);
            this.cacheTemporary(user);
         } else {
            this.getOrFetch(uuid);
         }
      }
   }

   public final void handleJoin(@NonNull PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.repository.getById(player.getUniqueId()).ifPresent(user -> {
         user.setName(player.getName());
         this.handleJoin((U)user);
         this.cachePermanent((U)user);
      });
   }

   public final void handleQuit(@NonNull PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.repository.getById(player.getUniqueId()).ifPresent(user -> {
         this.handleQuit((U)user);
         if (user.isDirty()) {
            this.plugin.runTaskAsync(() -> this.dataAccessor.update((U)user));
            user.markClean();
         } else {
            this.plugin.runTaskAsync(() -> this.dataAccessor.tinyUpdate((U)user));
         }

         this.cacheTemporary((U)user);
      });
   }

   protected void handleJoin(@NonNull U user) {
   }

   protected void handleQuit(@NonNull U user) {
   }

   public void saveDirty() {
      Set<U> users = this.repository.getAll().stream().filter(UserTemplate::isDirty).peek(UserTemplate::markClean).collect(Collectors.toSet());
      if (!users.isEmpty()) {
         this.dataAccessor.update(users);
      }
   }

   @NonNull
   public final U getOrFetch(@NonNull Player player) {
      UUID uuid = player.getUniqueId();
      U byId = this.repository.getById(uuid).orElse(null);
      if (byId != null) {
         return byId;
      } else {
         U fromDb = this.dataAccessor.loadById(uuid).orElse(null);
         if (fromDb != null) {
            this.plugin.warn("User data for '%s' was present, but not loaded.");
            this.cachePermanent(fromDb);
            return fromDb;
         } else {
            InetAddress address = Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress).orElse(null);
            if (address == null) {
               throw new IllegalStateException("%s is not a real player?".formatted(player));
            } else {
               return this.create(uuid, player.getName(), address);
            }
         }
      }
   }

   @NonNull
   public final Optional<U> getOrFetch(@NonNull String name) {
      Player player = Players.getExactPlayer(name);
      if (player != null) {
         return this.getOrFetch(player.getUniqueId());
      } else {
         Optional<U> byName = this.repository.getByName(name);
         if (byName.isPresent()) {
            return byName;
         } else {
            Optional<U> fromDb = this.dataAccessor.loadByName(name);
            if (fromDb.isPresent()) {
               this.cacheTemporary(fromDb.get());
               return fromDb;
            } else {
               return Optional.empty();
            }
         }
      }
   }

   @NonNull
   public final Optional<U> getOrFetch(@NonNull UUID uuid) {
      Optional<U> byName = this.repository.getById(uuid);
      if (byName.isPresent()) {
         return byName;
      } else {
         Optional<U> fromDb = this.dataAccessor.loadById(uuid);
         if (fromDb.isPresent()) {
            this.cacheTemporary(fromDb.get());
            return fromDb;
         } else {
            return Optional.empty();
         }
      }
   }

   @NonNull
   public final CompletableFuture<Optional<U>> loadByIdAsync(@NonNull UUID uuid) {
      return CompletableFuture.supplyAsync(() -> this.getOrFetch(uuid));
   }

   @NonNull
   public final CompletableFuture<Optional<U>> loadByNameAsync(@NonNull String name) {
      return CompletableFuture.supplyAsync(() -> this.getOrFetch(name));
   }

   @NonNull
   public Set<U> getAll() {
      Set<U> users = this.repository.getAll();
      this.dataAccessor.loadAll().stream().filter(user -> !this.repository.contains(user.getId())).forEach(users::add);
      return users;
   }
}

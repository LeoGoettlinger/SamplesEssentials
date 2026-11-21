package com.oheers.fish.database.data.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.execute.DatabaseWorker;
import com.oheers.fish.database.model.user.EmptyUserReport;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ObjIntConsumer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class UserManager implements Listener {
   private final Database database;
   private final DatabaseWorker databaseWorker;
   private final ObjIntConsumer<UUID> joinPreloadHook;
   private final Map<UUID, Integer> userCache;

   public UserManager(Database database, DatabaseWorker databaseWorker, @Nullable ObjIntConsumer<UUID> joinPreloadHook) {
      this.database = database;
      this.databaseWorker = databaseWorker;
      this.joinPreloadHook = joinPreloadHook;
      this.userCache = new ConcurrentHashMap<>();
   }

   @EventHandler
   public void onJoin(@NonNull final PlayerJoinEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      this.databaseWorker.execute(() -> {
         int id = this.loadOrCreateUser(uuid);
         if (id != 0 && this.joinPreloadHook != null) {
            this.joinPreloadHook.accept(uuid, id);
         }
      });
   }

   @EventHandler
   public void onLeave(@NonNull final PlayerQuitEvent event) {
      this.userCache.remove(event.getPlayer().getUniqueId());
   }

   public int getUserId(final UUID uuid) {
      Integer id = this.userCache.get(uuid);
      if (id != null) {
         return id;
      } else {
         id = this.database.getUserId(uuid);
         if (id != 0) {
            this.userCache.put(uuid, id);
         }

         return id;
      }
   }

   public int getCachedUserId(final UUID uuid) {
      return this.userCache.getOrDefault(uuid, 0);
   }

   public CompletableFuture<Integer> getUserIdAsync(final UUID uuid) {
      Integer id = this.userCache.get(uuid);
      return id != null ? CompletableFuture.completedFuture(id) : this.databaseWorker.query(() -> this.getUserId(uuid));
   }

   public void clearCache() {
      this.userCache.clear();
   }

   private int loadOrCreateUser(final UUID uuid) {
      int id = this.database.getUserId(uuid);
      if (id == 0) {
         id = this.database.upsertUserReport(new EmptyUserReport(uuid));
      }

      if (id != 0) {
         this.userCache.putIfAbsent(uuid, id);
      }

      EvenMoreFish.getInstance().debug("User ID: %d UUID: %s".formatted(id, uuid));
      return id;
   }
}

package com.earth2me.essentials.userstorage;

import com.earth2me.essentials.OfflinePlayerStub;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import org.bukkit.entity.Player;

public class ModernUserMap extends CacheLoader<UUID, User> implements IUserMap {
   private final transient IEssentials ess;
   private final transient ModernUUIDCache uuidCache;
   private final transient LoadingCache<UUID, User> userCache;
   private final transient ConcurrentMap<UUID, User> onlineUserCache;
   private final boolean debugPrintStackWithWarn;
   private final long debugMaxWarnsPerType;
   private final boolean debugLogCache;
   private final ConcurrentMap<String, AtomicLong> debugNonPlayerWarnCounts;

   public ModernUserMap(final IEssentials ess) {
      this.ess = ess;
      this.uuidCache = new ModernUUIDCache(ess);
      this.userCache = CacheBuilder.newBuilder()
         .maximumSize(ess.getSettings().getMaxUserCacheCount())
         .expireAfterAccess(ess.getSettings().getMaxUserCacheValueExpiry(), TimeUnit.SECONDS)
         .softValues()
         .build(this);
      String printStackProperty = System.getProperty("net.essentialsx.usermap.print-stack", "false");
      String maxWarnProperty = System.getProperty("net.essentialsx.usermap.max-warns", "10");
      String logCacheProperty = System.getProperty("net.essentialsx.usermap.log-cache", "false");
      this.debugMaxWarnsPerType = NumberUtil.isLong(maxWarnProperty) ? Long.parseLong(maxWarnProperty) : -1L;
      this.debugPrintStackWithWarn = Boolean.parseBoolean(printStackProperty);
      this.debugLogCache = Boolean.parseBoolean(logCacheProperty);
      this.debugNonPlayerWarnCounts = new ConcurrentHashMap<>();
      this.onlineUserCache = new ConcurrentHashMap<>();
   }

   @Override
   public Set<UUID> getAllUserUUIDs() {
      return this.uuidCache.getCachedUUIDs();
   }

   @Override
   public long getCachedCount() {
      return this.userCache.size();
   }

   @Override
   public boolean isCached(final UUID uuid) {
      if (uuid == null) {
         return false;
      } else {
         try {
            return this.userCache.getIfPresent(uuid) != null;
         } catch (Exception var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.WARNING, "Exception while checking if user is cached for " + uuid, (Throwable)var3);
            }

            return false;
         }
      }
   }

   @Override
   public int getUserCount() {
      return this.uuidCache.getCacheSize();
   }

   @Override
   public User getUser(final UUID uuid) {
      if (uuid == null) {
         return null;
      } else {
         try {
            return (User)this.userCache.get(uuid);
         } catch (ExecutionException var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.WARNING, "Exception while getting user for " + uuid, (Throwable)var3);
            }

            return null;
         }
      }
   }

   @Override
   public User getUser(final Player base) {
      User user = this.loadUncachedUser(base);
      this.userCache.put(user.getUUID(), user);
      this.debugLogCache(user);
      return user;
   }

   public ConcurrentMap<UUID, User> getOnlineUserCache() {
      return this.onlineUserCache;
   }

   @Override
   public User getUser(final String name) {
      if (name == null) {
         return null;
      } else {
         User user = this.getUser(this.uuidCache.getCachedUUID(name));
         if (user != null && user.getBase() instanceof OfflinePlayerStub) {
            if (user.getLastAccountName() != null) {
               ((OfflinePlayerStub)user.getBase()).setName(user.getLastAccountName());
            } else {
               ((OfflinePlayerStub)user.getBase()).setName(name);
            }
         }

         return user;
      }
   }

   public void addCachedNpcName(final UUID uuid, final String name) {
      if (uuid != null && name != null) {
         this.uuidCache.updateCache(uuid, name);
      }
   }

   public User load(final UUID uuid) throws Exception {
      User user = this.loadUncachedUser(uuid);
      if (user != null) {
         this.debugLogCache(user);
         return user;
      } else {
         throw new Exception("User not found!");
      }
   }

   @Override
   public User loadUncachedUser(final Player base) {
      if (base == null) {
         return null;
      } else {
         User user = this.getUser(base.getUniqueId());
         if (user == null) {
            this.debugLogUncachedNonPlayer(base);
            user = new User(base, this.ess);
         } else if (!base.equals(user.getBase())) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.INFO, "Essentials updated the underlying Player object for " + user.getUUID());
            }

            user.update(base);
         }

         this.uuidCache.updateCache(user.getUUID(), user.getName());
         return user;
      }
   }

   @Override
   public User loadUncachedUser(final UUID uuid) {
      User user = (User)this.userCache.getIfPresent(uuid);
      if (user != null) {
         return user;
      } else {
         Player player = this.ess.getServer().getPlayer(uuid);
         if (player != null) {
            user = new User(player, this.ess);
            this.uuidCache.updateCache(uuid, player.getName());
            return user;
         } else {
            File userFile = this.getUserFile(uuid);
            if (!userFile.exists()) {
               return null;
            } else {
               Player var8 = new OfflinePlayerStub(uuid, this.ess.getServer());
               user = new User(var8, this.ess);
               String accName = user.getLastAccountName();
               var8.setName(accName);
               this.uuidCache.updateCache(uuid, accName != null && !this.uuidCache.getNameCache().containsKey(accName) ? accName : null);
               return user;
            }
         }
      }
   }

   @Override
   public Map<String, UUID> getNameCache() {
      return this.uuidCache.getNameCache();
   }

   public String getSanitizedName(final String name) {
      return this.uuidCache.getSanitizedName(name);
   }

   public void blockingSave() {
      this.uuidCache.blockingSave();
   }

   public void invalidate(final UUID uuid) {
      this.userCache.invalidate(uuid);
      this.uuidCache.removeCache(uuid);
   }

   public void removeCache(final UUID uuid) {
      this.onlineUserCache.remove(uuid);
   }

   private File getUserFile(final UUID uuid) {
      return new File(new File(this.ess.getDataFolder(), "userdata"), uuid.toString() + ".yml");
   }

   public void shutdown() {
      this.uuidCache.shutdown();
      this.onlineUserCache.clear();
   }

   private void debugLogCache(final User user) {
      if (this.debugLogCache) {
         Throwable throwable = new Throwable();
         this.ess.getLogger().log(Level.INFO, String.format("Caching user %s (%s)", user.getName(), user.getUUID()), throwable);
      }
   }

   private void debugLogUncachedNonPlayer(final Player base) {
      String typeName = base.getClass().getName();
      long count = this.debugNonPlayerWarnCounts.computeIfAbsent(typeName, name -> new AtomicLong(0L)).getAndIncrement();
      if (this.debugMaxWarnsPerType < 0L || count <= this.debugMaxWarnsPerType) {
         Throwable throwable = this.debugPrintStackWithWarn ? new Throwable() : null;
         this.ess
            .getLogger()
            .log(Level.INFO, "Created a User for " + base.getName() + " (" + base.getUniqueId() + ") for non Bukkit type: " + typeName, throwable);
         if (count == this.debugMaxWarnsPerType) {
            this.ess
               .getLogger()
               .log(Level.WARNING, "Essentials will not log any more warnings for " + typeName + ". Please report this to the EssentialsX team.");
         }
      }
   }
}

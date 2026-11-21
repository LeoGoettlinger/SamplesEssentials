package com.sk89q.worldedit.session;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.session.storage.JsonFileSessionStore;
import com.sk89q.worldedit.session.storage.SessionStore;
import com.sk89q.worldedit.session.storage.VoidStore;
import com.sk89q.worldedit.util.concurrency.EvenMoreExecutors;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class SessionManager {
   public static int EXPIRATION_GRACE = 600000;
   private static final int FLUSH_PERIOD = 30000;
   private static final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
      EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 5, "WorldEdit Session Saver - %s")
   );
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Timer timer = new Timer("WorldEdit Session Manager");
   private final WorldEdit worldEdit;
   private final Map<UUID, SessionManager.SessionHolder> sessions = new HashMap<>();
   private SessionStore store = new VoidStore();

   public SessionManager(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
      worldEdit.getEventBus().register(this);
      this.timer.schedule(new SessionManager.SessionTracker(), 30000L, 30000L);
   }

   public synchronized boolean contains(SessionOwner owner) {
      Preconditions.checkNotNull(owner);
      return this.sessions.containsKey(this.getKey(owner));
   }

   @Nullable
   public synchronized LocalSession findByName(String name) {
      Preconditions.checkNotNull(name);

      for (SessionManager.SessionHolder holder : this.sessions.values()) {
         String test = holder.key.getName();
         if (name.equals(test)) {
            return holder.session;
         }
      }

      return null;
   }

   @Nullable
   public synchronized LocalSession getIfPresent(SessionOwner owner) {
      Preconditions.checkNotNull(owner);
      SessionManager.SessionHolder stored = this.sessions.get(this.getKey(owner));
      if (stored != null) {
         if (stored.sessionIdle && stored.key.isActive()) {
            stored.sessionIdle = false;
         }

         return stored.session;
      } else {
         return null;
      }
   }

   public synchronized LocalSession get(SessionOwner owner) {
      Preconditions.checkNotNull(owner);
      LocalSession session = this.getIfPresent(owner);
      LocalConfiguration config = this.worldEdit.getConfiguration();
      SessionKey sessionKey = owner.getSessionKey();
      if (session == null) {
         try {
            session = this.store.load(this.getKey(sessionKey));
            session.postLoad();
         } catch (IOException var6) {
            LOGGER.warn("Failed to load saved session", var6);
            session = new LocalSession();
         }

         Request.request().setSession(session);
         session.setConfiguration(config);
         session.setBlockChangeLimit(config.defaultChangeLimit);
         session.setTimeout(config.calculationTimeout);
         this.sessions.put(this.getKey(owner), new SessionManager.SessionHolder(sessionKey, session));
      }

      if (this.shouldBoundLimit(owner, "worldedit.limit.unrestricted", session.getBlockChangeLimit(), config.maxChangeLimit)) {
         session.setBlockChangeLimit(config.maxChangeLimit);
      }

      if (this.shouldBoundLimit(owner, "worldedit.timeout.unrestricted", session.getTimeout(), config.maxCalculationTimeout)) {
         session.setTimeout(config.maxCalculationTimeout);
      }

      session.setUseInventory(
         config.useInventory
            && (
               !config.useInventoryOverride
                  || !owner.hasPermission("worldedit.inventory.unrestricted")
                     && (!config.useInventoryCreativeOverride || owner instanceof Player && ((Player)owner).getGameMode() != GameModes.CREATIVE)
            )
      );
      if (!(owner instanceof Locatable)) {
         session.setPlaceAtPos1(true);
      }

      return session;
   }

   private boolean shouldBoundLimit(SessionOwner owner, String permission, int currentLimit, int maxLimit) {
      return maxLimit <= -1 ? false : (currentLimit < 0 || currentLimit > maxLimit) && !owner.hasPermission(permission);
   }

   private void setDefaultWand(String sessionItem, String configItem, LocalSession session, Tool wand) throws InvalidToolBindException {
      ItemType wandItem = null;
      if (sessionItem != null) {
         wandItem = ItemTypes.get(sessionItem);
      }

      if (wandItem == null) {
         wandItem = ItemTypes.get(configItem);
      }

      if (wandItem != null) {
         session.setTool(wandItem, wand);
      }
   }

   private ListenableFuture<?> commit(final Map<SessionKey, LocalSession> sessions) {
      Preconditions.checkNotNull(sessions);
      return sessions.isEmpty() ? Futures.immediateFuture(sessions) : executorService.submit(() -> {
         Exception exception = null;

         for (Entry<SessionKey, LocalSession> entry : sessions.entrySet()) {
            SessionKey key = entry.getKey();
            if (key.isPersistent()) {
               try {
                  this.store.save(this.getKey(key), entry.getValue());
               } catch (IOException var7) {
                  LOGGER.warn("Failed to write session for UUID " + this.getKey(key), var7);
                  exception = var7;
               }
            }
         }

         if (exception != null) {
            throw exception;
         } else {
            return sessions;
         }
      });
   }

   protected UUID getKey(SessionOwner owner) {
      return this.getKey(owner.getSessionKey());
   }

   protected UUID getKey(SessionKey key) {
      return key.getUniqueId();
   }

   public synchronized void remove(SessionOwner owner) {
      Preconditions.checkNotNull(owner);
      this.sessions.remove(this.getKey(owner));
   }

   public synchronized void unload() {
      this.clear();
      this.timer.cancel();
   }

   public synchronized void clear() {
      this.saveChangedSessions();
      this.sessions.clear();
   }

   private synchronized void saveChangedSessions() {
      long now = System.currentTimeMillis();
      Iterator<SessionManager.SessionHolder> it = this.sessions.values().iterator();
      Map<SessionKey, LocalSession> saveQueue = new HashMap<>();

      while (it.hasNext()) {
         SessionManager.SessionHolder stored = it.next();
         if (stored.key.isActive()) {
            stored.lastActive = now;
            if (stored.session.compareAndResetDirty()) {
            }
         } else if (now - stored.lastActive > EXPIRATION_GRACE) {
            if (stored.session.compareAndResetDirty()) {
               saveQueue.put(stored.key, stored.session);
            }

            it.remove();
         }
      }

      if (!saveQueue.isEmpty()) {
         this.commit(saveQueue);
      }
   }

   @Subscribe
   public void onConfigurationLoad(ConfigurationLoadEvent event) {
      LocalConfiguration config = event.getConfiguration();
      Path dir = config.getWorkingDirectoryPath().resolve("sessions");
      this.store = new JsonFileSessionStore(dir);
   }

   @Subscribe
   public void onSessionIdle(final SessionIdleEvent event) {
      SessionManager.SessionHolder holder = this.sessions.get(this.getKey(event.getKey()));
      if (holder != null && !holder.sessionIdle) {
         holder.sessionIdle = true;
         LocalSession session = holder.session;
         session.onIdle();
      }
   }

   private static final class SessionHolder {
      private final SessionKey key;
      private final LocalSession session;
      private long lastActive = System.currentTimeMillis();
      private boolean sessionIdle = false;

      private SessionHolder(SessionKey key, LocalSession session) {
         this.key = key;
         this.session = session;
      }
   }

   private class SessionTracker extends TimerTask {
      @Override
      public void run() {
         synchronized (SessionManager.this) {
            SessionManager.this.saveChangedSessions();
         }
      }
   }
}

package su.nightexpress.nightcore.database;

import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;

@Deprecated
public abstract class AbstractUser<P extends NightCorePlugin> implements DataUser {
   protected final P plugin;
   protected final UUID uuid;
   protected String name;
   protected long dateCreated;
   protected long lastOnline;
   protected long cachedUntil;
   protected long autoSaveIn;
   protected long nextSyncIn;

   public AbstractUser(@NonNull P plugin, @NonNull UUID uuid, @NonNull String name, long dateCreated, long lastOnline) {
      this.plugin = plugin;
      this.uuid = uuid;
      this.name = name;
      this.setDateCreated(dateCreated);
      this.setLastOnline(lastOnline);
      this.setCachedUntil(-1L);
   }

   @Override
   public void onLoad() {
   }

   @Override
   public void onUnload() {
   }

   private long getTimestamp(double seconds) {
      return seconds < 0.0 ? -1L : System.currentTimeMillis() + (long)(1000.0 * seconds);
   }

   @Override
   public boolean isCacheExpired() {
      return this.getCachedUntil() > 0L && System.currentTimeMillis() >= this.getCachedUntil();
   }

   @Override
   public boolean isAutoSaveReady() {
      return this.getAutoSaveIn() > 0L && System.currentTimeMillis() >= this.getAutoSaveIn();
   }

   @Override
   public boolean isSyncReady() {
      return this.getNextSyncIn() >= 0L && System.currentTimeMillis() >= this.getNextSyncIn();
   }

   @Override
   public void cancelAutoSave() {
      this.setAutoSaveIn(-1.0);
   }

   @Override
   public void cancelSynchronization() {
      this.setNextSyncIn(-1);
   }

   @Override
   public long getCachedUntil() {
      return this.cachedUntil;
   }

   @Override
   public void setCachedUntil(long cachedUntil) {
      this.cachedUntil = cachedUntil;
   }

   @Override
   public long getAutoSaveIn() {
      return this.autoSaveIn;
   }

   @Override
   public void setAutoSaveIn(double seconds) {
      this.autoSaveIn = this.getTimestamp(seconds);
   }

   @Override
   public long getNextSyncIn() {
      return this.nextSyncIn;
   }

   @Override
   public void setNextSyncIn(int seconds) {
      this.nextSyncIn = this.getTimestamp(seconds);
   }

   @NonNull
   @Override
   public final UUID getId() {
      return this.uuid;
   }

   @NonNull
   @Override
   public final String getName() {
      return this.name;
   }

   @Override
   public void setName(String name) {
      this.name = name;
   }

   @Override
   public final long getDateCreated() {
      return this.dateCreated;
   }

   @Override
   public final void setDateCreated(long dateCreated) {
      this.dateCreated = dateCreated;
   }

   @Override
   public final long getLastOnline() {
      return this.lastOnline;
   }

   @Override
   public final void setLastOnline(long lastOnline) {
      this.lastOnline = lastOnline;
   }

   @Override
   public final boolean isOnline() {
      return this.getPlayer() != null;
   }

   @NonNull
   @Override
   public final OfflinePlayer getOfflinePlayer() {
      return this.plugin.getServer().getOfflinePlayer(this.getId());
   }

   @Nullable
   @Override
   public final Player getPlayer() {
      return this.plugin.getServer().getPlayer(this.getId());
   }

   @Override
   public String toString() {
      return "AbstractUser [uuid=" + this.uuid + ", name=" + this.name + ", lastOnline=" + this.lastOnline + "]";
   }
}

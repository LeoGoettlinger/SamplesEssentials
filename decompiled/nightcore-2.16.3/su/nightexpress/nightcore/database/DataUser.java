package su.nightexpress.nightcore.database;

import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Deprecated
public interface DataUser {
   void onLoad();

   void onUnload();

   boolean isCacheExpired();

   boolean isAutoSaveReady();

   boolean isSyncReady();

   long getCachedUntil();

   long getAutoSaveIn();

   long getNextSyncIn();

   void cancelAutoSave();

   void cancelSynchronization();

   void setCachedUntil(long var1);

   void setAutoSaveIn(double var1);

   void setNextSyncIn(int var1);

   @NonNull
   UUID getId();

   @NonNull
   String getName();

   void setName(String var1);

   long getDateCreated();

   void setDateCreated(long var1);

   long getLastOnline();

   void setLastOnline(long var1);

   boolean isOnline();

   @NonNull
   OfflinePlayer getOfflinePlayer();

   @Nullable
   Player getPlayer();
}

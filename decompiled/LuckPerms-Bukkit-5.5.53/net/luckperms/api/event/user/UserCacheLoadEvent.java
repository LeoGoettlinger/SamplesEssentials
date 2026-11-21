package net.luckperms.api.event.user;

import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserCacheLoadEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   User getUser();

   @Param(1)
   @NonNull
   CachedDataManager getLoadedData();
}

package net.luckperms.api.platform;

import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PlayerAdapter<T> {
   @NonNull
   User getUser(@NonNull T var1);

   @NonNull
   ImmutableContextSet getContext(@NonNull T var1);

   @NonNull
   QueryOptions getQueryOptions(@NonNull T var1);

   @NonNull
   default CachedPermissionData getPermissionData(@NonNull T player) {
      return this.getUser(player).getCachedData().getPermissionData(this.getQueryOptions(player));
   }

   @NonNull
   default CachedMetaData getMetaData(@NonNull T player) {
      return this.getUser(player).getCachedData().getMetaData(this.getQueryOptions(player));
   }
}

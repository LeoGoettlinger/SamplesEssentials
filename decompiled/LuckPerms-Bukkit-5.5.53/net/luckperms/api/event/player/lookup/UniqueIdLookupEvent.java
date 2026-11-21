package net.luckperms.api.event.player.lookup;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.ResultEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UniqueIdLookupEvent extends LuckPermsEvent, ResultEvent<UUID> {
   @Param(0)
   @NonNull
   String getUsername();

   default void setUniqueId(@Nullable UUID uniqueId) {
      this.result().set(uniqueId);
   }
}

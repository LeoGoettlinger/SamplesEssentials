package net.luckperms.api.event.player.lookup;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.ResultEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UsernameLookupEvent extends LuckPermsEvent, ResultEvent<String> {
   @Param(0)
   @NonNull
   UUID getUniqueId();

   default void setUsername(@Nullable String username) {
      this.result().set(username);
   }
}

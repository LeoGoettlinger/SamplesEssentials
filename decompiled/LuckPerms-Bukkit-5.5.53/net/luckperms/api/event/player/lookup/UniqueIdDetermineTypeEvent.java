package net.luckperms.api.event.player.lookup;

import java.util.Objects;
import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.ResultEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UniqueIdDetermineTypeEvent extends LuckPermsEvent, ResultEvent<String> {
   String TYPE_AUTHENTICATED = "authenticated";
   String TYPE_UNAUTHENTICATED = "unauthenticated";
   String TYPE_NPC = "npc";
   String TYPE_UNKNOWN = "unknown";

   @Param(0)
   @NonNull
   UUID getUniqueId();

   @NonNull
   default String getType() {
      return this.result().get();
   }

   default void setType(@NonNull String type) {
      Objects.requireNonNull(type, "type");
      this.result().set(type);
   }
}

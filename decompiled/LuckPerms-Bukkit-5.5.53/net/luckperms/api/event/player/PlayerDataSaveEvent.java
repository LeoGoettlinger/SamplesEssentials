package net.luckperms.api.event.player;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.PlayerSaveResult;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PlayerDataSaveEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   UUID getUniqueId();

   @Param(1)
   @NonNull
   String getUsername();

   @Param(2)
   @NonNull
   PlayerSaveResult getResult();
}

package net.luckperms.api.event.track;

import java.util.List;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.cause.DeletionCause;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface TrackDeleteEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   String getTrackName();

   @Param(1)
   @NonNull
   List<String> getExistingData();

   @Param(2)
   @NonNull
   DeletionCause getCause();
}

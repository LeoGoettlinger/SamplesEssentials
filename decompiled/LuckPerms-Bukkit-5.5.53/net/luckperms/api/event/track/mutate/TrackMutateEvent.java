package net.luckperms.api.event.track.mutate;

import java.util.List;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.track.Track;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface TrackMutateEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   Track getTrack();

   @Param(1)
   @NonNull
   @Unmodifiable
   List<String> getStateBefore();

   @Param(2)
   @NonNull
   @Unmodifiable
   List<String> getStateAfter();
}

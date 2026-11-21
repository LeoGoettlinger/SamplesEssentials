package net.luckperms.api.event.track.mutate;

import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface TrackAddGroupEvent extends TrackMutateEvent {
   @Param(3)
   @NonNull
   String getGroup();
}

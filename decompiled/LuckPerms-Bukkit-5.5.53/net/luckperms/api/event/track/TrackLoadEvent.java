package net.luckperms.api.event.track;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.track.Track;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface TrackLoadEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   Track getTrack();
}

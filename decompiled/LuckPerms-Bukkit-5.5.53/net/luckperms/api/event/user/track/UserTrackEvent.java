package net.luckperms.api.event.user.track;

import java.util.Optional;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.Sourced;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.Track;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserTrackEvent extends LuckPermsEvent, Sourced {
   @Param(0)
   @NonNull
   Track getTrack();

   @Param(1)
   @NonNull
   User getUser();

   @NonNull
   TrackAction getAction();

   @Param(2)
   @NonNull
   Optional<String> getGroupFrom();

   @Param(3)
   @NonNull
   Optional<String> getGroupTo();
}

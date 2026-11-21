package net.luckperms.api.event.user.track;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserDemoteEvent extends UserTrackEvent {
   @NonNull
   @Override
   default TrackAction getAction() {
      return TrackAction.DEMOTION;
   }
}

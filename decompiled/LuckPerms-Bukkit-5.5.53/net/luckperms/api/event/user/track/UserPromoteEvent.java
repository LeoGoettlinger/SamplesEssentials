package net.luckperms.api.event.user.track;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserPromoteEvent extends UserTrackEvent {
   @NonNull
   @Override
   default TrackAction getAction() {
      return TrackAction.PROMOTION;
   }
}

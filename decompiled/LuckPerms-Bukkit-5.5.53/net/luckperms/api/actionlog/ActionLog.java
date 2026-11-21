package net.luckperms.api.actionlog;

import java.util.SortedSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

@Deprecated
public interface ActionLog {
   @NonNull
   @Unmodifiable
   SortedSet<Action> getContent();

   @NonNull
   @Unmodifiable
   SortedSet<Action> getContent(@NonNull UUID var1);

   @NonNull
   @Unmodifiable
   SortedSet<Action> getUserHistory(@NonNull UUID var1);

   @NonNull
   @Unmodifiable
   SortedSet<Action> getGroupHistory(@NonNull String var1);

   @NonNull
   @Unmodifiable
   SortedSet<Action> getTrackHistory(@NonNull String var1);
}

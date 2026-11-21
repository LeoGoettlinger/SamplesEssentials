package su.nightexpress.nightcore.bridge.scheduler;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface AdaptedScheduler {
   void cancelTasks();

   @NonNull
   AdaptedTask runTask(@NonNull Runnable var1);

   @Nullable
   AdaptedTask runTask(@NonNull Entity var1, @NonNull Runnable var2);

   @NonNull
   AdaptedTask runTask(@NonNull Location var1, @NonNull Runnable var2);

   @NonNull
   AdaptedTask runTask(@NonNull Chunk var1, @NonNull Runnable var2);

   @NonNull
   AdaptedTask runTaskAsync(@NonNull Runnable var1);

   @NonNull
   AdaptedTask runTaskLater(@NonNull Runnable var1, long var2);

   @NonNull
   AdaptedTask runTaskLaterAsync(@NonNull Runnable var1, long var2);

   @NonNull
   AdaptedTask runTaskTimer(@NonNull Runnable var1, long var2, long var4);

   @NonNull
   AdaptedTask runTaskTimerAsync(@NonNull Runnable var1, long var2, long var4);
}

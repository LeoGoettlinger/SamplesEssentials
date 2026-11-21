package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.type.ImplementationType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WrappedScheduler {
   @NotNull
   ImplementationType getImplementationType();

   @NotNull
   Plugin getPlugin();

   void cancelAllTasks();

   void cancelTask(@NotNull WrappedTask var1);

   @NotNull
   WrappedTask runTask(@NotNull Runnable var1);

   @NotNull
   WrappedTask runTaskAsynchronously(@NotNull Runnable var1);

   @Nullable
   WrappedTask runTaskAtEntity(@NotNull Entity var1, @NotNull Runnable var2);

   @NotNull
   WrappedTask runTaskAtLocation(@NotNull Location var1, @NotNull Runnable var2);

   @NotNull
   WrappedTask runTaskTimer(@NotNull Runnable var1, long var2, long var4);

   @NotNull
   WrappedTask runTaskTimerAsynchronously(@NotNull Runnable var1, long var2, long var4);

   @Nullable
   WrappedTask runTaskTimerAtEntity(@NotNull Entity var1, @NotNull Runnable var2, long var3, long var5);

   @NotNull
   WrappedTask runTaskTimerAtLocation(@NotNull Location var1, @NotNull Runnable var2, long var3, long var5);

   @NotNull
   WrappedTask runTaskLater(@NotNull Runnable var1, long var2);

   @NotNull
   WrappedTask runTaskLaterAsynchronously(@NotNull Runnable var1, long var2);

   @Nullable
   WrappedTask runTaskLaterAtEntity(@NotNull Entity var1, @NotNull Runnable var2, long var3);

   @NotNull
   WrappedTask runTaskLaterAtLocation(@NotNull Location var1, @NotNull Runnable var2, long var3);
}

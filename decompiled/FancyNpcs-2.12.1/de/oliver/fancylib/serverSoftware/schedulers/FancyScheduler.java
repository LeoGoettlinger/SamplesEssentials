package de.oliver.fancylib.serverSoftware.schedulers;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface FancyScheduler {
   @NotNull
   FancyScheduler runTask(Location var1, Runnable var2);

   @NotNull
   FancyScheduler runTaskAsynchronously(Runnable var1);

   @NotNull
   FancyScheduler runTaskLater(Location var1, long var2, Runnable var4);

   @NotNull
   FancyScheduler runTaskLaterAsynchronously(long var1, Runnable var3);

   @NotNull
   FancyScheduler runTaskTimer(Location var1, long var2, long var4, Runnable var6);

   @NotNull
   FancyScheduler runTaskTimerAsynchronously(long var1, long var3, Runnable var5);

   void cancel();
}

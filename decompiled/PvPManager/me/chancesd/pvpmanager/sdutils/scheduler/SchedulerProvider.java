package me.chancesd.pvpmanager.sdutils.scheduler;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface SchedulerProvider {
   void runPlatformAsync(Runnable var1);

   void runPlatformAsyncLater(Runnable var1, long var2);

   void runPlatformAsyncTimer(Runnable var1, long var2, long var4);

   SDTask runTask(Runnable var1);

   void runTask(Runnable var1, World var2, int var3, int var4);

   SDTask runTask(Runnable var1, Entity var2);

   void runTaskLater(Runnable var1, long var2);

   void runTaskLater(Runnable var1, Entity var2, long var3);

   SDTask runTaskTimer(Runnable var1, Entity var2, long var3, long var5);

   void executeConsoleCommand(String var1);

   void executePlayerCommand(Player var1, String var2);

   boolean isPrimaryThread();

   boolean isServerStopping();

   CompletableFuture<Boolean> teleport(Entity var1, @NotNull Location var2);

   void cancelAllTasks();
}

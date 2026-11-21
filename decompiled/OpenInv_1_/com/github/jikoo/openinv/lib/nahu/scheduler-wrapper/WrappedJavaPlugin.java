package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedJavaPlugin extends JavaPlugin {
   @NotNull
   public abstract WrappedScheduler getScheduler();
}

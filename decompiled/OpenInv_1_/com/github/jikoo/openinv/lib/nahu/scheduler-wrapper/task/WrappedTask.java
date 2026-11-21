package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface WrappedTask {
   void cancel();

   boolean isCancelled();

   @NotNull
   Plugin getOwningPlugin();
}

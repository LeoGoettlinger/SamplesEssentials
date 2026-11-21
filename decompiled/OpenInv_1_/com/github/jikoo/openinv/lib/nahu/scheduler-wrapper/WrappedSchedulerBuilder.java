package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.bukkit.BukkitWrappedScheduler;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.implementation.folia.FoliaWrappedScheduler;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.type.ImplementationType;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class WrappedSchedulerBuilder {
   private Plugin plugin;
   private ImplementationType implementationType = ImplementationType.find();

   private WrappedSchedulerBuilder() {
   }

   @NotNull
   public WrappedSchedulerBuilder plugin(@NotNull Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin);
      return this;
   }

   @NotNull
   public WrappedSchedulerBuilder implementationType(@NotNull ImplementationType implementationType) {
      this.implementationType = Objects.requireNonNull(implementationType);
      return this;
   }

   @NotNull
   public WrappedScheduler build() {
      Objects.requireNonNull(this.plugin, "Plugin cannot be null!");
      Objects.requireNonNull(this.implementationType, "Implementation type cannot be null!");
      Preconditions.checkArgument(!this.implementationType.isUnknown(), "Implementation type cannot be unknown!");

      return (WrappedScheduler)(switch (this.implementationType) {
         case FOLIA -> new FoliaWrappedScheduler(this.plugin);
         default -> new BukkitWrappedScheduler(this.plugin);
      });
   }

   @NotNull
   public static WrappedSchedulerBuilder builder() {
      return new WrappedSchedulerBuilder();
   }

   @Deprecated(
      forRemoval = true
   )
   @NotNull
   public static ImplementationType find() {
      return ImplementationType.find();
   }
}

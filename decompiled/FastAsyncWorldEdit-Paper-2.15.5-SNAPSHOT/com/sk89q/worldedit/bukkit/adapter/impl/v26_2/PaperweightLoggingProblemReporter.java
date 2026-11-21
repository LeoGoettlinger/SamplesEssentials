package com.sk89q.worldedit.bukkit.adapter.impl.v26_2;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.Collector;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.util.ProblemReporter.Problem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class PaperweightLoggingProblemReporter implements ProblemReporter, AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Collector delegate = new Collector();
   private final Supplier<String> contextSupplier;

   static <T> T with(Supplier<String> contextSupplier, Function<ProblemReporter, T> consumer) {
      Object var3;
      try (PaperweightLoggingProblemReporter problemReporter = new PaperweightLoggingProblemReporter(contextSupplier)) {
         var3 = consumer.apply(problemReporter);
      }

      return (T)var3;
   }

   PaperweightLoggingProblemReporter(Supplier<String> contextSupplier) {
      this.contextSupplier = contextSupplier;
   }

   public ProblemReporter forChild(PathElement child) {
      return this.delegate.forChild(child);
   }

   public void report(Problem problem) {
      this.delegate.report(problem);
   }

   @Override
   public void close() {
      if (!this.delegate.isEmpty()) {
         LOGGER.warn("Problems were reported during {}: {}", this.contextSupplier.get(), this.delegate.getTreeReport());
      }
   }
}

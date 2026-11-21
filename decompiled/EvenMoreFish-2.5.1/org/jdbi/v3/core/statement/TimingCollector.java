package org.jdbi.v3.core.statement;

@FunctionalInterface
@Deprecated(
   since = "3.2.0",
   forRemoval = true
)
public interface TimingCollector {
   @Deprecated(
      since = "3.2.0",
      forRemoval = true
   )
   TimingCollector NOP_TIMING_COLLECTOR = (ns, ctx) -> {};

   @Deprecated(
      since = "3.2.0",
      forRemoval = true
   )
   void collect(long elapsedNs, StatementContext ctx);
}

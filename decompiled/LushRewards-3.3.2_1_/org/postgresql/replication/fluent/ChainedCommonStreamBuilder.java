package org.postgresql.replication.fluent;

import java.util.concurrent.TimeUnit;
import org.postgresql.replication.LogSequenceNumber;

public interface ChainedCommonStreamBuilder<T extends ChainedCommonStreamBuilder<T>> {
   T withSlotName(String var1);

   T withStatusInterval(int var1, TimeUnit var2);

   T withStartPosition(LogSequenceNumber var1);
}

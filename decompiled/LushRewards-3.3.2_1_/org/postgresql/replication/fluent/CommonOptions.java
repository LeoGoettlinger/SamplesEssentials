package org.postgresql.replication.fluent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.LogSequenceNumber;

public interface CommonOptions {
   @Nullable
   String getSlotName();

   LogSequenceNumber getStartLSNPosition();

   int getStatusInterval();
}

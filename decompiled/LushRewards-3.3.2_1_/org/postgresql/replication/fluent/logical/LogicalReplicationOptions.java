package org.postgresql.replication.fluent.logical;

import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.replication.fluent.CommonOptions;

public interface LogicalReplicationOptions extends CommonOptions {
   @Nullable
   @Override
   String getSlotName();

   Properties getSlotOptions();
}

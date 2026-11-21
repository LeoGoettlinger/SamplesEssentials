package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;

public interface ChainedCreateReplicationSlotBuilder {
   ChainedLogicalCreateSlotBuilder logical();

   ChainedPhysicalCreateSlotBuilder physical();
}

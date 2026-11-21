package org.postgresql.replication.fluent;

import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.postgresql.replication.fluent.physical.ChainedPhysicalStreamBuilder;

public interface ChainedStreamBuilder {
   ChainedLogicalStreamBuilder logical();

   ChainedPhysicalStreamBuilder physical();
}

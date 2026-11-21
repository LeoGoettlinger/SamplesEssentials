package org.postgresql.replication.fluent.logical;

import org.postgresql.replication.fluent.ChainedCommonCreateSlotBuilder;

public interface ChainedLogicalCreateSlotBuilder extends ChainedCommonCreateSlotBuilder<ChainedLogicalCreateSlotBuilder> {
   ChainedLogicalCreateSlotBuilder withOutputPlugin(String var1);
}

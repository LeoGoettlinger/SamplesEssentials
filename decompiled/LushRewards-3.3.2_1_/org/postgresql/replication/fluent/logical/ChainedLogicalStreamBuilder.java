package org.postgresql.replication.fluent.logical;

import java.sql.SQLException;
import java.util.Properties;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedCommonStreamBuilder;

public interface ChainedLogicalStreamBuilder extends ChainedCommonStreamBuilder<ChainedLogicalStreamBuilder> {
   PGReplicationStream start() throws SQLException;

   ChainedLogicalStreamBuilder withSlotOption(String var1, boolean var2);

   ChainedLogicalStreamBuilder withSlotOption(String var1, int var2);

   ChainedLogicalStreamBuilder withSlotOption(String var1, String var2);

   ChainedLogicalStreamBuilder withSlotOptions(Properties var1);
}

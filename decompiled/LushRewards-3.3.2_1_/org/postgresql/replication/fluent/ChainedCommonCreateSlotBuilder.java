package org.postgresql.replication.fluent;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.postgresql.replication.ReplicationSlotInfo;

public interface ChainedCommonCreateSlotBuilder<T extends ChainedCommonCreateSlotBuilder<T>> {
   T withSlotName(String var1);

   T withTemporaryOption() throws SQLFeatureNotSupportedException;

   ReplicationSlotInfo make() throws SQLException;
}

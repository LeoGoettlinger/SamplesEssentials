package org.postgresql.replication.fluent.logical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;

public interface StartLogicalReplicationCallback {
   PGReplicationStream start(LogicalReplicationOptions var1) throws SQLException;
}

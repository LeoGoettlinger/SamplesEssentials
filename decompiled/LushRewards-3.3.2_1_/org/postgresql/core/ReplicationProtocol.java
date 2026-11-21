package org.postgresql.core;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.logical.LogicalReplicationOptions;
import org.postgresql.replication.fluent.physical.PhysicalReplicationOptions;

public interface ReplicationProtocol {
   PGReplicationStream startLogical(LogicalReplicationOptions var1) throws SQLException;

   PGReplicationStream startPhysical(PhysicalReplicationOptions var1) throws SQLException;
}

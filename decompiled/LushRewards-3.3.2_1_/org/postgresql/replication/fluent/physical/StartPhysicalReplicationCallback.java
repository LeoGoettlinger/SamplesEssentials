package org.postgresql.replication.fluent.physical;

import java.sql.SQLException;
import org.postgresql.replication.PGReplicationStream;

public interface StartPhysicalReplicationCallback {
   PGReplicationStream start(PhysicalReplicationOptions var1) throws SQLException;
}

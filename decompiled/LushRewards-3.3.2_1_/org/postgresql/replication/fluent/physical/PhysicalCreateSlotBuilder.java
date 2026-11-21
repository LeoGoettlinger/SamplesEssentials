package org.postgresql.replication.fluent.physical;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.core.BaseConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.ReplicationSlotInfo;
import org.postgresql.replication.ReplicationType;
import org.postgresql.replication.fluent.AbstractCreateSlotBuilder;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.internal.Nullness;

public class PhysicalCreateSlotBuilder extends AbstractCreateSlotBuilder<ChainedPhysicalCreateSlotBuilder> implements ChainedPhysicalCreateSlotBuilder {
   public PhysicalCreateSlotBuilder(BaseConnection connection) {
      super(connection);
   }

   protected ChainedPhysicalCreateSlotBuilder self() {
      return this;
   }

   @Override
   public ReplicationSlotInfo make() throws SQLException {
      if (this.slotName != null && !this.slotName.isEmpty()) {
         Statement statement = this.connection.createStatement();
         ResultSet result = null;
         ReplicationSlotInfo slotInfo = null;

         try {
            String sql = String.format("CREATE_REPLICATION_SLOT %s %s PHYSICAL", this.slotName, this.temporaryOption ? "TEMPORARY" : "");
            statement.execute(sql);
            result = statement.getResultSet();
            if (result == null || !result.next()) {
               throw new PSQLException(GT.tr("{0} returned no results"), PSQLState.OBJECT_NOT_IN_STATE);
            }

            slotInfo = new ReplicationSlotInfo(
               Nullness.castNonNull(result.getString("slot_name")),
               ReplicationType.PHYSICAL,
               LogSequenceNumber.valueOf(Nullness.castNonNull(result.getString("consistent_point"))),
               result.getString("snapshot_name"),
               result.getString("output_plugin")
            );
         } finally {
            if (result != null) {
               result.close();
            }

            statement.close();
         }

         return slotInfo;
      } else {
         throw new IllegalArgumentException("Replication slotName can't be null");
      }
   }
}

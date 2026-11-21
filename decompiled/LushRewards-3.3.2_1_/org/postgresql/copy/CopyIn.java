package org.postgresql.copy;

import java.sql.SQLException;
import org.postgresql.util.ByteStreamWriter;

public interface CopyIn extends CopyOperation {
   void writeToCopy(byte[] var1, int var2, int var3) throws SQLException;

   void writeToCopy(ByteStreamWriter var1) throws SQLException;

   void flushCopy() throws SQLException;

   long endCopy() throws SQLException;
}

package org.postgresql.core.v3;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.ParameterList;

interface V3ParameterList extends ParameterList {
   void checkAllParametersSet() throws SQLException;

   void convertFunctionOutParameters();

   @Nullable
   SimpleParameterList[] getSubparams();

   @Nullable
   int[] getParamTypes();

   @Nullable
   byte[] getFlags();

   @Nullable
   byte[][] getEncoding();
}

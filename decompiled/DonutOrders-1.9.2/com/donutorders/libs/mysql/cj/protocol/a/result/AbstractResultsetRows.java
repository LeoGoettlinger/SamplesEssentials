package com.donutorders.libs.mysql.cj.protocol.a.result;

import com.donutorders.libs.mysql.cj.protocol.ColumnDefinition;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.protocol.ResultsetRow;
import com.donutorders.libs.mysql.cj.protocol.ResultsetRows;
import com.donutorders.libs.mysql.cj.protocol.ResultsetRowsOwner;
import com.donutorders.libs.mysql.cj.protocol.a.NativePacketPayload;

public abstract class AbstractResultsetRows implements ResultsetRows {
   protected static final int BEFORE_START_OF_ROWS = -1;
   protected ColumnDefinition metadata;
   protected int currentPositionInFetchedRows = -1;
   protected boolean wasEmpty = false;
   protected ResultsetRowsOwner owner;
   protected ProtocolEntityFactory<ResultsetRow, NativePacketPayload> rowFactory;

   @Override
   public void setOwner(ResultsetRowsOwner rs) {
      this.owner = rs;
   }

   @Override
   public ResultsetRowsOwner getOwner() {
      return this.owner;
   }

   @Override
   public void setMetadata(ColumnDefinition columnDefinition) {
      this.metadata = columnDefinition;
   }

   @Override
   public ColumnDefinition getMetadata() {
      return this.metadata;
   }

   @Override
   public boolean wasEmpty() {
      return this.wasEmpty;
   }
}

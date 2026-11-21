package github.nighter.smartspawner.libs.mariadb.message.server;

import github.nighter.smartspawner.libs.mariadb.client.Column;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.message.ServerMessage;
import java.util.Objects;

public class ColumnDefinitionPacket implements Column, ServerMessage {
   protected final int charset;
   protected final long columnLength;
   protected final DataType dataType;
   protected final byte decimals;
   protected final byte[] extTypeName;
   protected final byte[] extTypeFormat;
   private final ReadableByteBuf buf;
   private final int flags;
   private final int[] stringPos;
   private final boolean useAliasAsName;

   public ColumnDefinitionPacket(
      ReadableByteBuf buf,
      int charset,
      long columnLength,
      DataType dataType,
      byte decimals,
      int flags,
      int[] stringPos,
      byte[] extTypeName,
      byte[] extTypeFormat,
      boolean useAliasAsName
   ) {
      this.buf = buf;
      this.charset = charset;
      this.columnLength = columnLength;
      this.dataType = dataType;
      this.decimals = decimals;
      this.flags = flags;
      this.stringPos = stringPos;
      this.extTypeName = extTypeName;
      this.extTypeFormat = extTypeFormat;
      this.useAliasAsName = useAliasAsName;
   }

   protected ColumnDefinitionPacket(ColumnDefinitionPacket prev, boolean useAliasAsName) {
      this.buf = prev.buf;
      this.charset = prev.charset;
      this.columnLength = prev.columnLength;
      this.dataType = prev.dataType;
      this.decimals = prev.decimals;
      this.flags = prev.flags;
      this.stringPos = prev.stringPos;
      this.extTypeName = prev.extTypeName;
      this.extTypeFormat = prev.extTypeFormat;
      this.useAliasAsName = useAliasAsName;
   }

   @Override
   public String getCatalog() {
      this.buf.pos(0);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public String getSchema() {
      this.buf.pos(this.stringPos[0]);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public String getTableAlias() {
      this.buf.pos(this.stringPos[1]);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public String getTable() {
      this.buf.pos(this.stringPos[this.useAliasAsName ? 1 : 2]);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public String getColumnAlias() {
      this.buf.pos(this.stringPos[3]);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public String getColumnName() {
      this.buf.pos(this.stringPos[4]);
      return this.buf.readString(this.buf.readIntLengthEncodedNotNull());
   }

   @Override
   public long getColumnLength() {
      return this.columnLength;
   }

   @Override
   public DataType getType() {
      return this.dataType;
   }

   @Override
   public byte getDecimals() {
      return this.decimals;
   }

   @Override
   public boolean isSigned() {
      return (this.flags & 32) == 0;
   }

   @Override
   public int getDisplaySize() {
      return (int)this.columnLength;
   }

   @Override
   public boolean isPrimaryKey() {
      return (this.flags & 2) > 0;
   }

   @Override
   public boolean isAutoIncrement() {
      return (this.flags & 512) > 0;
   }

   @Override
   public boolean hasDefault() {
      return (this.flags & 4096) == 0;
   }

   @Override
   public boolean isBinary() {
      return this.charset == 63;
   }

   @Override
   public int getFlags() {
      return this.flags;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ColumnDefinitionPacket that = (ColumnDefinitionPacket)o;
         return this.charset == that.charset
            && this.columnLength == that.columnLength
            && this.dataType == that.dataType
            && this.decimals == that.decimals
            && this.flags == that.flags;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.charset, this.columnLength, this.dataType, this.decimals, this.flags);
   }
}

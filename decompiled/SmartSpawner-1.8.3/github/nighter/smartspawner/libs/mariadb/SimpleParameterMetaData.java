package github.nighter.smartspawner.libs.mariadb;

import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import java.sql.SQLException;

public class SimpleParameterMetaData implements java.sql.ParameterMetaData {
   private final int paramCount;
   private final ExceptionFactory exceptionFactory;

   protected SimpleParameterMetaData(ExceptionFactory exceptionFactory, int paramCount) {
      this.exceptionFactory = exceptionFactory;
      this.paramCount = paramCount;
   }

   @Override
   public int getParameterCount() {
      return this.paramCount;
   }

   private void checkIndex(int index) throws SQLException {
      if (index < 1 || index > this.paramCount) {
         throw this.exceptionFactory.create(String.format("Wrong index position. Is %s but must be in 1-%s range", index, this.paramCount));
      }
   }

   @Override
   public int isNullable(int idx) throws SQLException {
      this.checkIndex(idx);
      return 1;
   }

   @Override
   public boolean isSigned(int idx) throws SQLException {
      this.checkIndex(idx);
      return true;
   }

   @Override
   public int getPrecision(int idx) throws SQLException {
      this.checkIndex(idx);
      throw this.exceptionFactory.create("Unknown parameter metadata precision");
   }

   @Override
   public int getScale(int idx) throws SQLException {
      this.checkIndex(idx);
      throw this.exceptionFactory.create("Unknown parameter metadata scale");
   }

   @Override
   public int getParameterType(int idx) throws SQLException {
      this.checkIndex(idx);
      throw this.exceptionFactory.create("Getting parameter type metadata is not supported", "0A000", -1);
   }

   @Override
   public String getParameterTypeName(int idx) throws SQLException {
      this.checkIndex(idx);
      throw this.exceptionFactory.create("Unknown parameter metadata type name");
   }

   @Override
   public String getParameterClassName(int idx) throws SQLException {
      this.checkIndex(idx);
      throw this.exceptionFactory.create("Unknown parameter metadata class name", "0A000");
   }

   @Override
   public int getParameterMode(int idx) throws SQLException {
      this.checkIndex(idx);
      return 1;
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (this.isWrapperFor(iface)) {
         return iface.cast(this);
      } else {
         throw new SQLException("The receiver is not a wrapper for " + iface.getName());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }
}

package github.nighter.smartspawner.libs.mariadb.codec;

import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.DataType;
import github.nighter.smartspawner.libs.mariadb.client.socket.Writer;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Parameter<T> implements github.nighter.smartspawner.libs.mariadb.client.util.Parameter {
   public static final Parameter<?> NULL_PARAMETER = new Parameter(null, null) {
      @Override
      public int getBinaryEncodeType() {
         return DataType.VARCHAR.get();
      }

      @Override
      public boolean isNull() {
         return true;
      }

      @Override
      public int getApproximateTextProtocolLength() {
         return 4;
      }
   };
   protected final Codec<T> codec;
   protected final T value;
   protected final Long length;

   public Parameter(Codec<T> codec, T value) {
      this.codec = codec;
      this.value = value;
      this.length = null;
   }

   public Parameter(Codec<T> codec, T value, Long length) {
      this.codec = codec;
      this.value = value;
      this.length = length;
   }

   @Override
   public void encodeText(Writer encoder, Context context) throws IOException, SQLException {
      if (this.value == null) {
         encoder.writeAscii("null");
      } else {
         this.codec.encodeText(encoder, context, this.value, null, this.length);
      }
   }

   @Override
   public int getApproximateTextProtocolLength() {
      return this.value == null ? 4 : this.codec.getApproximateTextProtocolLength(this.value, this.length);
   }

   @Override
   public void encodeBinary(Writer encoder, Context context) throws IOException, SQLException {
      this.codec.encodeBinary(encoder, context, this.value, null, this.length);
   }

   @Override
   public void encodeLongData(Writer encoder) throws IOException, SQLException {
      this.codec.encodeLongData(encoder, this.value, this.length);
   }

   @Override
   public byte[] encodeData() throws IOException, SQLException {
      return this.codec.encodeData(this.value, this.length);
   }

   @Override
   public boolean canEncodeLongData() {
      return this.codec.canEncodeLongData();
   }

   @Override
   public int getBinaryEncodeType() {
      return this.codec.getBinaryEncodeType();
   }

   @Override
   public boolean isNull() {
      return this.value == null;
   }

   @Override
   public String bestEffortStringValue(Context context) {
      if (this.isNull()) {
         return "null";
      } else if (this.codec.canEncodeLongData()) {
         Type it = this.codec.getClass().getGenericInterfaces()[0];
         ParameterizedType parameterizedType = (ParameterizedType)it;
         Type typeParameter = parameterizedType.getActualTypeArguments()[0];
         return "<" + typeParameter + ">";
      } else {
         try {
            Writer writer = new Writer(null, 0, 16777215, null, null);
            this.codec.encodeText(writer, context, this.value, null, this.length);
            return new String(writer.buf(), 4, writer.pos() - 4, StandardCharsets.UTF_8);
         } catch (Throwable var5) {
            return null;
         }
      }
   }
}

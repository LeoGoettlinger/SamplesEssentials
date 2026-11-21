package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanProperty;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.ContextualSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.TokenBuffer;

public class UUIDSerializer extends StdScalarSerializer<UUID> implements ContextualSerializer {
   static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
   protected final Boolean _asBinary;

   public UUIDSerializer() {
      this(null);
   }

   protected UUIDSerializer(Boolean asBinary) {
      super(UUID.class);
      this._asBinary = asBinary;
   }

   public boolean isEmpty(SerializerProvider prov, UUID value) {
      return value.getLeastSignificantBits() == 0L && value.getMostSignificantBits() == 0L;
   }

   @Override
   public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
      JsonFormat.Value format = this.findFormatOverrides(serializers, property, this.handledType());
      Boolean asBinary = null;
      if (format != null) {
         JsonFormat.Shape shape = format.getShape();
         if (shape == JsonFormat.Shape.BINARY) {
            asBinary = true;
         } else if (shape == JsonFormat.Shape.STRING) {
            asBinary = false;
         }
      }

      return !Objects.equals(asBinary, this._asBinary) ? new UUIDSerializer(asBinary) : this;
   }

   public void serialize(UUID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      if (this._writeAsBinary(gen)) {
         gen.writeBinary(_asBytes(value));
      } else {
         char[] ch = new char[36];
         long msb = value.getMostSignificantBits();
         _appendInt((int)(msb >> 32), ch, 0);
         ch[8] = '-';
         int i = (int)msb;
         _appendShort(i >>> 16, ch, 9);
         ch[13] = '-';
         _appendShort(i, ch, 14);
         ch[18] = '-';
         long lsb = value.getLeastSignificantBits();
         _appendShort((int)(lsb >>> 48), ch, 19);
         ch[23] = '-';
         _appendShort((int)(lsb >>> 32), ch, 24);
         _appendInt((int)lsb, ch, 28);
         gen.writeString(ch, 0, 36);
      }
   }

   protected boolean _writeAsBinary(JsonGenerator g) {
      return this._asBinary != null ? this._asBinary : !(g instanceof TokenBuffer) && g.canWriteBinaryNatively();
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      this.visitStringFormat(visitor, typeHint, JsonValueFormat.UUID);
   }

   private static void _appendInt(int bits, char[] ch, int offset) {
      _appendShort(bits >> 16, ch, offset);
      _appendShort(bits, ch, offset + 4);
   }

   private static void _appendShort(int bits, char[] ch, int offset) {
      ch[offset] = HEX_CHARS[bits >> 12 & 15];
      ch[++offset] = HEX_CHARS[bits >> 8 & 15];
      ch[++offset] = HEX_CHARS[bits >> 4 & 15];
      ch[++offset] = HEX_CHARS[bits & 15];
   }

   private static final byte[] _asBytes(UUID uuid) {
      byte[] buffer = new byte[16];
      long hi = uuid.getMostSignificantBits();
      long lo = uuid.getLeastSignificantBits();
      _appendInt((int)(hi >> 32), buffer, 0);
      _appendInt((int)hi, buffer, 4);
      _appendInt((int)(lo >> 32), buffer, 8);
      _appendInt((int)lo, buffer, 12);
      return buffer;
   }

   private static final void _appendInt(int value, byte[] buffer, int offset) {
      buffer[offset] = (byte)(value >> 24);
      buffer[++offset] = (byte)(value >> 16);
      buffer[++offset] = (byte)(value >> 8);
      buffer[++offset] = (byte)value;
   }
}

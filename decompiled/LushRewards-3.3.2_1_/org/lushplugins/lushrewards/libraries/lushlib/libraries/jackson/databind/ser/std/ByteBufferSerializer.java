package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.std;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializerProvider;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ByteBufferBackedInputStream;

public class ByteBufferSerializer extends StdScalarSerializer<ByteBuffer> {
   public ByteBufferSerializer() {
      super(ByteBuffer.class);
   }

   public void serialize(ByteBuffer bbuf, JsonGenerator gen, SerializerProvider provider) throws IOException {
      if (bbuf.hasArray()) {
         int pos = bbuf.position();
         gen.writeBinary(bbuf.array(), bbuf.arrayOffset() + pos, bbuf.limit() - pos);
      } else {
         ByteBuffer copy = bbuf.asReadOnlyBuffer();
         if (copy.position() > 0) {
            ((Buffer)copy).rewind();
         }

         InputStream in = new ByteBufferBackedInputStream(copy);
         gen.writeBinary(in, copy.remaining());
         in.close();
      }
   }

   @Override
   public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
      JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
      if (v2 != null) {
         v2.itemsFormat(JsonFormatTypes.INTEGER);
      }
   }
}

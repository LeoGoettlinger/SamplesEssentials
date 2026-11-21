package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.FieldKind;
import ac.grim.grimac.internal.storage.codec.MethodHandleCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.bson.Document;

@ApiStatus.Internal
public final class BsonTsCodecImpl<R> extends MethodHandleCodec<R> implements BsonTsCodec<R> {
   private static final String META_KEY = "meta";
   @NotNull
   private final FieldKind[] kinds;

   public BsonTsCodecImpl(@NotNull Class<R> recordType, @NotNull EncodeShape shape) {
      super(recordType, shape);
      int n = shape.fields().size();
      this.kinds = new FieldKind[n];

      for (int i = 0; i < n; i++) {
         this.kinds[i] = shape.fields().get(i).kind();
      }

      if (shape.timestampField() == null) {
         throw new IllegalArgumentException("BsonTsCodec requires a @Timestamp field on " + recordType.getName());
      }
   }

   @NotNull
   public String timestampField() {
      return Objects.requireNonNull(this.shape().timestampField());
   }

   @Override
   public void encode(@NotNull R record, @NotNull BsonWriter w) {
      w.writeStartDocument();
      w.writeInt32("_v", this.version());

      for (int i = 0; i < this.fieldNames.length; i++) {
         if (this.kinds[i] != FieldKind.PARTITION) {
            this.writeField(record, i, w, this.kinds[i] == FieldKind.TIMESTAMP);
         }
      }

      w.writeName("meta");
      w.writeStartDocument();

      for (int ix = 0; ix < this.fieldNames.length; ix++) {
         if (this.kinds[ix] == FieldKind.PARTITION) {
            this.writeField(record, ix, w, false);
         }
      }

      w.writeEndDocument();
      w.writeEndDocument();
   }

   private void writeField(@NotNull R record, int i, @NotNull BsonWriter w, boolean asDate) {
      String name = this.fieldNames[i];
      switch (this.typeTags[i]) {
         case INT:
            w.writeInt32(name, this.readInt(record, i));
            break;
         case LONG:
            long vxxx = this.readLong(record, i);
            if (asDate) {
               w.writeDateTime(name, vxxx);
            } else {
               w.writeInt64(name, vxxx);
            }
            break;
         case DOUBLE:
         case FLOAT:
            w.writeDouble(name, this.readDouble(record, i));
            break;
         case ENUM:
            w.writeInt32(name, this.readEnumOrdinal(record, i));
            break;
         case BOOLEAN:
            Boolean vxx = (Boolean)this.readRef(record, i);
            if (vxx != null) {
               w.writeBoolean(name, vxx);
            } else if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
            break;
         case STRING:
            String s = (String)this.readRef(record, i);
            if (s != null) {
               w.writeString(name, s);
            } else if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
            break;
         case BYTES:
            byte[] b = (byte[])this.readRef(record, i);
            if (b != null) {
               w.writeBinaryData(name, new BsonBinary(b));
            } else if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
            break;
         case UUID:
            UUID u = (UUID)this.readRef(record, i);
            if (u != null) {
               w.writeBinaryData(name, BsonBinaries.uuidBinary(u));
            } else if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
            break;
         case FLOAT_ARRAY:
            float[] vx = (float[])this.readRef(record, i);
            if (vx != null) {
               BsonBinaries.writeFloatArray(name, vx, w);
            } else if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
            break;
         case NESTED_SEALED:
            Object v = this.readRef(record, i);
            if (v != null) {
               throw new UnsupportedOperationException("NESTED_SEALED encoding not yet implemented (field " + name + "); ships with Phase 6");
            }

            if (!this.nullable[i]) {
               this.throwMissingNonNullable(i);
            }
      }
   }

   private void throwMissingNonNullable(int i) {
      throw new IllegalStateException("null value for non-nullable field " + this.fieldNames[i] + " on " + this.recordType().getName());
   }

   @NotNull
   @Override
   public R decode(@NotNull Document source) {
      Document meta = (Document)source.get("meta", Document.class);
      Object[] args = this.freshArgsArray();

      for (int i = 0; i < this.fieldNames.length; i++) {
         Document src = this.kinds[i] == FieldKind.PARTITION ? meta : source;
         Object raw = src == null ? null : src.get(this.fieldNames[i]);
         args[this.recordPositionAt(i)] = BsonDecodeHelper.decodeField(this, i, raw);
      }

      return this.construct(args);
   }
}

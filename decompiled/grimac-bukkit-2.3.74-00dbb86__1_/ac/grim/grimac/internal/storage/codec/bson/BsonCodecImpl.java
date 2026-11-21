package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.internal.storage.codec.MethodHandleCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.bson.Document;

@ApiStatus.Internal
public final class BsonCodecImpl<R> extends MethodHandleCodec<R> implements BsonCodec<R> {
   public static final String VERSION_FIELD = "_v";

   public BsonCodecImpl(@NotNull Class<R> recordType, @NotNull EncodeShape shape) {
      super(recordType, shape);
   }

   @Override
   public void encode(@NotNull R record, @NotNull BsonWriter w) {
      w.writeStartDocument();
      this.encodeFields(record, w);
      w.writeEndDocument();
   }

   @Override
   public void encodeFields(@NotNull R record, @NotNull BsonWriter w) {
      w.writeInt32("_v", this.version());
      int n = this.fieldNames.length;

      for (int i = 0; i < n; i++) {
         this.writeField(record, i, w);
      }
   }

   @Override
   public Object readField(@NotNull R record, int fieldIndex) {
      return super.readField(record, fieldIndex);
   }

   private void writeField(@NotNull R record, int i, @NotNull BsonWriter w) {
      String name = this.fieldNames[i];
      switch (this.typeTags[i]) {
         case INT:
            w.writeInt32(name, this.readInt(record, i));
            break;
         case LONG:
            w.writeInt64(name, this.readLong(record, i));
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
      int n = this.fieldNames.length;
      Object[] args = this.freshArgsArray();

      for (int i = 0; i < n; i++) {
         Object raw = source.get(this.fieldNames[i]);
         args[this.recordPositionAt(i)] = BsonDecodeHelper.decodeField(this, i, raw);
      }

      return this.construct(args);
   }

   @NotNull
   @Override
   public R decodeFromValues(@NotNull Object[] argsByShapeIndex) {
      if (argsByShapeIndex.length != this.fieldNames.length) {
         throw new IllegalArgumentException("argsByShapeIndex length " + argsByShapeIndex.length + " != shape.fields() count " + this.fieldNames.length);
      } else {
         Object[] args = this.freshArgsArray();

         for (int i = 0; i < this.fieldNames.length; i++) {
            args[this.recordPositionAt(i)] = this.requireFieldOrDefault(i, argsByShapeIndex[i]);
         }

         return this.construct(args);
      }
   }
}

package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import org.bson.BsonBinaryWriter;
import org.bson.RawBsonDocument;
import org.bson.io.BasicOutputBuffer;

@ApiStatus.Internal
final class MongoBsonBuffer {
   private static final int INITIAL_CAPACITY = 256;
   @NotNull
   private final BasicOutputBuffer buf = new BasicOutputBuffer(256);

   @NotNull
   BsonBinaryWriter writer() {
      this.buf.truncateToPosition(0);
      return new BsonBinaryWriter(this.buf);
   }

   @NotNull
   RawBsonDocument snapshot() {
      return new RawBsonDocument(this.buf.toByteArray());
   }
}

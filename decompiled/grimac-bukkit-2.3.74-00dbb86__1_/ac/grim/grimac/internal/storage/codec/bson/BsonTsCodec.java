package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import org.bson.BsonWriter;
import org.bson.Document;

@ApiStatus.Internal
public interface BsonTsCodec<R> extends Codec<R> {
   void encode(@NotNull R var1, @NotNull BsonWriter var2);

   @NotNull
   R decode(@NotNull Document var1);
}

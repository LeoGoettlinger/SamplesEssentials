package su.nightexpress.nightcore.util.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.DataResult.Success;
import java.lang.reflect.Method;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.nightexpress.nightcore.util.Reflex;

public class NbtSerializer {
   private static final Method CREATE_SERIALIZATION_CONTEXT = Reflex.safeMethod(
      NbtUtil.CLS_HOLDER_LOOKUP_PROVIDER, "createSerializationContext", "a", DynamicOps.class
   );
   private static final Method ENCODER_START = Reflex.safeMethod(Encoder.class, "encodeStart", DynamicOps.class, Object.class);
   private static final Method DECODER_PARSE = Reflex.safeMethod(Decoder.class, "parse", DynamicOps.class, Object.class);
   private static final Codec<?> ITEM_STACK_CODEC = (Codec<?>)Reflex.getFieldValue(NbtUtil.CLS_NMS_ITEM_STACK, "CODEC", "b");
   private static final Logger LOGGER = LoggerFactory.getLogger(NbtSerializer.class);

   @NonNull
   public static Object createSerializationContext() {
      Object context = Reflex.invokeMethod(CREATE_SERIALIZATION_CONTEXT, NbtUtil.REGISTRY_ACCESS, NbtOps.INSTANCE);
      if (context == null) {
         throw new IllegalStateException("Could not create serialization context");
      } else {
         return context;
      }
   }

   @Nullable
   public static Object decodeItemStack(@NonNull Object compoundTag) {
      Object context = createSerializationContext();
      DataResult<?> result = (DataResult<?>)Reflex.invokeMethod(DECODER_PARSE, ITEM_STACK_CODEC, context, compoundTag);
      return result == null ? null : result.resultOrPartial(itemId -> LOGGER.error("Could not decode ItemStack from tag: {}", itemId)).orElse(null);
   }

   @NonNull
   public static Object encodeItemStack(@NonNull Object nmsStack) {
      Object context = createSerializationContext();
      return Reflex.safeInvoke(ENCODER_START, ITEM_STACK_CODEC, context, nmsStack)
         .filter(Success.class::isInstance)
         .map(Success.class::cast)
         .map(DataResult::getOrThrow)
         .orElseThrow(() -> new IllegalStateException("Could not encode ItemStack into CompoundTag"));
   }
}

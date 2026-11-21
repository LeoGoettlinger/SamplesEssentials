package de.tr7zw.nbtapi.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.utils.nmsmappings.ClassWrapper;
import de.tr7zw.nbtapi.utils.nmsmappings.MojangToMapping;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public class NBTJsonUtil {
   public static JsonElement itemStackToJson(ItemStack itemStack) {
      try {
         Codec<Object> itemStackCodec = (Codec<Object>)ClassWrapper.NMS_ITEMSTACK
            .getClazz()
            .getField(MojangToMapping.getMapping().get("net.minecraft.world.item.ItemStack#CODEC"))
            .get(null);
         Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, itemStack);
         DataResult<JsonElement> result = itemStackCodec.encode(stack, JsonOps.INSTANCE, (JsonElement)JsonOps.INSTANCE.emptyMap());
         Optional<JsonElement> opt = (Optional<JsonElement>)result.getClass().getMethod("result").invoke(result);
         return opt.orElse(null);
      } catch (Exception var5) {
         throw new NbtApiException("Error trying to get Json of an ItemStack.", var5);
      }
   }
}

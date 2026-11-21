package com.github.retrooper.packetevents.util.adventure;

import com.github.retrooper.packetevents.protocol.nbt.codec.NBTCodec;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.adventure.serializer.gson.BackwardCompatUtil;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonDataComponentValue;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public final class AdventureConversionInjector {
   private AdventureConversionInjector() {
   }

   public static void inject() {
      if (BackwardCompatUtil.IS_4_17_0_OR_NEWER) {
         try {
            Class<?> conversionImpl = Class.forName("net.kyori.adventure.text.event.DataComponentValueConversionImpl");
            Class<?> registeredConversion = Class.forName("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$RegisteredConversion");
            Class<?> conversionCache = Class.forName("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$ConversionCache");
            BiFunction<Key, NbtTagHolder, GsonDataComponentValue> converter = (key, holder) -> GsonDataComponentValue.gsonDataComponentValue(
               NBTCodec.nbtToJson(holder.getTag(), false)
            );
            Constructor<?> conversionImplCtor = conversionImpl.getDeclaredConstructor(Class.class, Class.class, BiFunction.class);
            conversionImplCtor.setAccessible(true);
            DataComponentValueConverterRegistry.Conversion<NbtTagHolder, GsonDataComponentValue> conversionImplInst = (DataComponentValueConverterRegistry.Conversion<NbtTagHolder, GsonDataComponentValue>)conversionImplCtor.newInstance(
               NbtTagHolder.class, GsonDataComponentValue.class, converter
            );
            Constructor<?> registeredConversionCtor = registeredConversion.getDeclaredConstructor(
               Key.class, DataComponentValueConverterRegistry.Conversion.class
            );
            registeredConversionCtor.setAccessible(true);
            Key providerKey = Key.key("packetevents:hack/nbt2gson");
            Object registeredConversionInst = registeredConversionCtor.newInstance(providerKey, conversionImplInst);
            Map<Class<?>, Object> vals = new ConcurrentHashMap<>(1);
            vals.put(GsonDataComponentValue.class, registeredConversionInst);
            Map<Object, Object> cache = (Map<Object, Object>)Reflection.getField(conversionCache, "CACHE").get(null);
            cache.put(NbtTagHolder.class, vals);
         } catch (ReflectiveOperationException var11) {
            throw new RuntimeException("Failed to inject into conversion cache", var11);
         }
      }
   }

   public static void uninject() {
      if (BackwardCompatUtil.IS_4_17_0_OR_NEWER) {
         try {
            Class<?> conversionCache = Class.forName("net.kyori.adventure.text.event.DataComponentValueConverterRegistry$ConversionCache");
            Map<Object, Object> cache = (Map<Object, Object>)Reflection.getField(conversionCache, "CACHE").get(null);
            cache.remove(NbtTagHolder.class);
         } catch (ReflectiveOperationException var2) {
            throw new RuntimeException("Failed to uninject from conversion cache", var2);
         }
      }
   }
}

package com.earth2me.essentials.libs.kyori.adventure.platform.bukkit;

import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.serializer.ComponentSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public final class MinecraftComponentSerializer implements ComponentSerializer<Component, Component, Object> {
   private static final MinecraftComponentSerializer INSTANCE = new MinecraftComponentSerializer();
   @Nullable
   private static final Class<?> CLASS_JSON_DESERIALIZER = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonDeserializer"));
   @Nullable
   private static final Class<?> CLASS_JSON_ELEMENT = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonElement"));
   @Nullable
   private static final Class<?> CLASS_JSON_OPS = MinecraftReflection.findClass("com.mo".concat("jang.serialization.JsonOps"));
   @Nullable
   private static final Class<?> CLASS_JSON_PARSER = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonParser"));
   @Nullable
   private static final Class<?> CLASS_CHAT_COMPONENT = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.Component")
   );
   @Nullable
   private static final Class<?> CLASS_COMPONENT_SERIALIZATION = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("network.chat.ComponentSerialization")
   );
   @Nullable
   private static final Class<?> CLASS_CRAFT_REGISTRY = MinecraftReflection.findCraftClass("CraftRegistry");
   @Nullable
   private static final Class<?> CLASS_HOLDERLOOKUP_PROVIDER = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("core.HolderLookup$Provider"), MinecraftReflection.findMcClassName("core.HolderLookup$a")
   );
   @Nullable
   private static final Class<?> CLASS_REGISTRY_ACCESS = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("core.IRegistryCustom"), MinecraftReflection.findMcClassName("core.RegistryAccess")
   );
   @Nullable
   private static final MethodHandle PARSE_JSON = MinecraftReflection.findMethod(CLASS_JSON_PARSER, "parse", CLASS_JSON_ELEMENT, String.class);
   @Nullable
   private static final MethodHandle GET_REGISTRY = MinecraftReflection.findStaticMethod(CLASS_CRAFT_REGISTRY, "getMinecraftRegistry", CLASS_REGISTRY_ACCESS);
   private static final AtomicReference<RuntimeException> INITIALIZATION_ERROR = new AtomicReference<>(new UnsupportedOperationException());
   private static final Object JSON_OPS_INSTANCE;
   private static final Object JSON_PARSER_INSTANCE;
   private static final Object MC_TEXT_GSON;
   private static final Object REGISTRY_ACCESS;
   private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE;
   private static final MethodHandle TEXT_SERIALIZER_SERIALIZE;
   private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE_TREE;
   private static final MethodHandle TEXT_SERIALIZER_SERIALIZE_TREE;
   private static final MethodHandle COMPONENTSERIALIZATION_CODEC_ENCODE;
   private static final MethodHandle COMPONENTSERIALIZATION_CODEC_DECODE;
   private static final MethodHandle CREATE_SERIALIZATION_CONTEXT;
   private static final boolean SUPPORTED;

   public static boolean isSupported() {
      return SUPPORTED;
   }

   @NotNull
   public static MinecraftComponentSerializer get() {
      return INSTANCE;
   }

   @NotNull
   @Override
   public Component deserialize(@NotNull final Object input) {
      if (!SUPPORTED) {
         throw (RuntimeException)INITIALIZATION_ERROR.get();
      } else {
         try {
            Object element;
            if (TEXT_SERIALIZER_SERIALIZE_TREE != null) {
               element = (Object)TEXT_SERIALIZER_SERIALIZE_TREE.invoke((Object)input);
            } else {
               if (MC_TEXT_GSON == null) {
                  if (COMPONENTSERIALIZATION_CODEC_ENCODE != null && CREATE_SERIALIZATION_CONTEXT != null) {
                     Object serializationContext = (Object)CREATE_SERIALIZATION_CONTEXT.bindTo(REGISTRY_ACCESS).invoke((Object)JSON_OPS_INSTANCE);
                     Object result = (Object)COMPONENTSERIALIZATION_CODEC_ENCODE.invoke((Object)input, (Object)serializationContext, (Void)null);
                     Method getOrThrow = result.getClass().getMethod("getOrThrow", Function.class);
                     Object jsonElement = getOrThrow.invoke(result, RuntimeException::new);
                     return (Component)BukkitComponentSerializer.gson().serializer().fromJson(jsonElement.toString(), Component.class);
                  }

                  return BukkitComponentSerializer.gson().deserialize((String)TEXT_SERIALIZER_SERIALIZE.invoke((Object)input));
               }

               element = ((Gson)MC_TEXT_GSON).toJsonTree(input);
            }

            return (Component)BukkitComponentSerializer.gson().serializer().fromJson(element.toString(), Component.class);
         } catch (Throwable var7) {
            throw new UnsupportedOperationException(var7);
         }
      }
   }

   @NotNull
   @Override
   public Object serialize(@NotNull final Component component) {
      if (!SUPPORTED) {
         throw (RuntimeException)INITIALIZATION_ERROR.get();
      } else if (TEXT_SERIALIZER_DESERIALIZE_TREE == null && MC_TEXT_GSON == null) {
         try {
            if (COMPONENTSERIALIZATION_CODEC_DECODE != null && CREATE_SERIALIZATION_CONTEXT != null) {
               Object serializationContext = (Object)CREATE_SERIALIZATION_CONTEXT.bindTo(REGISTRY_ACCESS).invoke((Object)JSON_OPS_INSTANCE);
               Object result = (Object)COMPONENTSERIALIZATION_CODEC_DECODE.invoke(
                  (Object)serializationContext, (JsonElement)BukkitComponentSerializer.gson().serializeToTree(component)
               );
               Method getOrThrow = result.getClass().getMethod("getOrThrow", Function.class);
               Object pair = getOrThrow.invoke(result, RuntimeException::new);
               Method getFirst = pair.getClass().getMethod("getFirst");
               return getFirst.invoke(pair);
            } else {
               return (Object)TEXT_SERIALIZER_DESERIALIZE.invoke((String)BukkitComponentSerializer.gson().serialize(component));
            }
         } catch (Throwable var7) {
            throw new UnsupportedOperationException(var7);
         }
      } else {
         JsonElement json = BukkitComponentSerializer.gson().serializer().toJsonTree(component);

         try {
            if (TEXT_SERIALIZER_DESERIALIZE_TREE != null) {
               Object unRelocatedJsonElement = (Object)PARSE_JSON.invoke((Object)JSON_PARSER_INSTANCE, (String)json.toString());
               return (Object)TEXT_SERIALIZER_DESERIALIZE_TREE.invoke((Object)unRelocatedJsonElement);
            } else {
               return ((Gson)MC_TEXT_GSON).fromJson(json, CLASS_CHAT_COMPONENT);
            }
         } catch (Throwable var8) {
            throw new UnsupportedOperationException(var8);
         }
      }
   }

   static {
      Object gson = null;
      Object jsonOpsInstance = null;
      Object jsonParserInstance = null;
      Object registryAccessInstance = null;
      MethodHandle textSerializerDeserialize = null;
      MethodHandle textSerializerSerialize = null;
      MethodHandle textSerializerDeserializeTree = null;
      MethodHandle textSerializerSerializeTree = null;
      MethodHandle codecEncode = null;
      MethodHandle codecDecode = null;
      MethodHandle createContext = null;

      try {
         if (CLASS_JSON_OPS != null) {
            Field instanceField = CLASS_JSON_OPS.getField("INSTANCE");
            instanceField.setAccessible(true);
            jsonOpsInstance = instanceField.get(null);
         }

         if (CLASS_JSON_PARSER != null) {
            jsonParserInstance = CLASS_JSON_PARSER.getDeclaredConstructor().newInstance();
         }

         if (CLASS_CHAT_COMPONENT != null) {
            Object registryAccess = GET_REGISTRY != null ? (Object)GET_REGISTRY.invoke() : null;
            registryAccessInstance = registryAccess;
            Class<?> chatSerializerClass = Arrays.stream(CLASS_CHAT_COMPONENT.getClasses()).filter(c -> {
               if (CLASS_JSON_DESERIALIZER != null) {
                  return CLASS_JSON_DESERIALIZER.isAssignableFrom((Class<?>)c);
               } else {
                  for (Class<?> itf : c.getInterfaces()) {
                     if (itf.getSimpleName().equals("JsonDeserializer")) {
                        return true;
                     }
                  }

                  return false;
               }
            }).findAny().orElse(MinecraftReflection.findNmsClass("ChatSerializer"));
            if (chatSerializerClass != null) {
               Field gsonField = Arrays.stream(chatSerializerClass.getDeclaredFields())
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> mx.getType().equals(Gson.class))
                  .findFirst()
                  .orElse(null);
               if (gsonField != null) {
                  gsonField.setAccessible(true);
                  gson = gsonField.get(null);
               }
            }

            List<Class<?>> candidates = new ArrayList<>();
            if (chatSerializerClass != null) {
               candidates.add(chatSerializerClass);
            }

            candidates.addAll(Arrays.asList(CLASS_CHAT_COMPONENT.getClasses()));

            for (Class<?> serializerClass : candidates) {
               Method[] declaredMethods = serializerClass.getDeclaredMethods();
               Method deserialize = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getReturnType()))
                  .filter(mx -> mx.getParameterCount() == 1 && mx.getParameterTypes()[0].equals(String.class))
                  .min(Comparator.comparing(Method::getName))
                  .orElse(null);
               Method serialize = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> mx.getReturnType().equals(String.class))
                  .filter(mx -> mx.getParameterCount() == 1 && CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getParameterTypes()[0]))
                  .findFirst()
                  .orElse(null);
               Method deserializeTree = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getReturnType()))
                  .filter(mx -> mx.getParameterCount() == 1 && mx.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                  .findFirst()
                  .orElse(null);
               Method serializeTree = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> mx.getReturnType().equals(CLASS_JSON_ELEMENT))
                  .filter(mx -> mx.getParameterCount() == 1 && CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getParameterTypes()[0]))
                  .findFirst()
                  .orElse(null);
               Method deserializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getReturnType()))
                  .filter(mx -> mx.getParameterCount() == 2)
                  .filter(mx -> mx.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                  .filter(mx -> mx.getParameterTypes()[1].isInstance(registryAccess))
                  .findFirst()
                  .orElse(null);
               Method serializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                  .filter(mx -> Modifier.isStatic(mx.getModifiers()))
                  .filter(mx -> mx.getReturnType().equals(CLASS_JSON_ELEMENT))
                  .filter(mx -> mx.getParameterCount() == 2)
                  .filter(mx -> CLASS_CHAT_COMPONENT.isAssignableFrom(mx.getParameterTypes()[0]))
                  .filter(mx -> mx.getParameterTypes()[1].isInstance(registryAccess))
                  .findFirst()
                  .orElse(null);
               if (deserialize != null) {
                  textSerializerDeserialize = MinecraftReflection.lookup().unreflect(deserialize);
               }

               if (serialize != null) {
                  textSerializerSerialize = MinecraftReflection.lookup().unreflect(serialize);
               }

               if (deserializeTree != null) {
                  textSerializerDeserializeTree = MinecraftReflection.lookup().unreflect(deserializeTree);
               } else if (deserializeTreeWithRegistryAccess != null) {
                  deserializeTreeWithRegistryAccess.setAccessible(true);
                  textSerializerDeserializeTree = MethodHandles.insertArguments(
                     MinecraftReflection.lookup().unreflect(deserializeTreeWithRegistryAccess), 1, registryAccess
                  );
               }

               if (serializeTree != null) {
                  textSerializerSerializeTree = MinecraftReflection.lookup().unreflect(serializeTree);
               } else if (serializeTreeWithRegistryAccess != null) {
                  serializeTreeWithRegistryAccess.setAccessible(true);
                  textSerializerSerializeTree = MethodHandles.insertArguments(
                     MinecraftReflection.lookup().unreflect(serializeTreeWithRegistryAccess), 1, registryAccess
                  );
               }
            }

            if (registryAccess != null && CLASS_HOLDERLOOKUP_PROVIDER != null) {
               for (Method m : CLASS_HOLDERLOOKUP_PROVIDER.getDeclaredMethods()) {
                  m.setAccessible(true);
                  if (m.getParameterCount() == 1
                     && m.getParameterTypes()[0].getSimpleName().equals("DynamicOps")
                     && m.getReturnType().getSimpleName().contains("RegistryOps")) {
                     createContext = MinecraftReflection.lookup().unreflect(m);
                     break;
                  }
               }
            }

            if (CLASS_COMPONENT_SERIALIZATION != null) {
               for (Field f : CLASS_COMPONENT_SERIALIZATION.getDeclaredFields()) {
                  if (Modifier.isStatic(f.getModifiers()) && f.getType().getSimpleName().equals("Codec")) {
                     f.setAccessible(true);
                     Object codecInstance = f.get(null);
                     Class<?> codecClass = codecInstance.getClass();

                     for (Method mx : codecClass.getDeclaredMethods()) {
                        if (mx.getName().equals("decode")) {
                           codecDecode = MinecraftReflection.lookup().unreflect(mx).bindTo(codecInstance);
                        } else if (mx.getName().equals("encode")) {
                           codecEncode = MinecraftReflection.lookup().unreflect(mx).bindTo(codecInstance);
                        }
                     }
                     break;
                  }
               }
            }
         }
      } catch (Throwable var24) {
         INITIALIZATION_ERROR.set(new UnsupportedOperationException("Error occurred during initialization", var24));
      }

      MC_TEXT_GSON = gson;
      JSON_OPS_INSTANCE = jsonOpsInstance;
      JSON_PARSER_INSTANCE = jsonParserInstance;
      TEXT_SERIALIZER_DESERIALIZE = textSerializerDeserialize;
      TEXT_SERIALIZER_SERIALIZE = textSerializerSerialize;
      TEXT_SERIALIZER_DESERIALIZE_TREE = textSerializerDeserializeTree;
      TEXT_SERIALIZER_SERIALIZE_TREE = textSerializerSerializeTree;
      COMPONENTSERIALIZATION_CODEC_ENCODE = codecEncode;
      COMPONENTSERIALIZATION_CODEC_DECODE = codecDecode;
      CREATE_SERIALIZATION_CONTEXT = createContext;
      REGISTRY_ACCESS = registryAccessInstance;
      SUPPORTED = MC_TEXT_GSON != null
         || TEXT_SERIALIZER_DESERIALIZE != null && TEXT_SERIALIZER_SERIALIZE != null
         || TEXT_SERIALIZER_DESERIALIZE_TREE != null && TEXT_SERIALIZER_SERIALIZE_TREE != null
         || COMPONENTSERIALIZATION_CODEC_ENCODE != null
            && COMPONENTSERIALIZATION_CODEC_DECODE != null
            && CREATE_SERIALIZATION_CONTEXT != null
            && JSON_OPS_INSTANCE != null;
   }
}

package ac.grim.grimac.shaded.kyori.adventure.platform.bukkit;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.serializer.ComponentSerializer;
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

@ApiStatus.Experimental
public final class MinecraftComponentSerializer implements ComponentSerializer<Component, Component, Object> {
   private static final MinecraftComponentSerializer INSTANCE = new MinecraftComponentSerializer();
   @Nullable
   private static final Class<?> CLASS_JSON_DESERIALIZER = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonDeserializer"));
   @Nullable
   private static final Class<?> CLASS_JSON_ELEMENT = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonElement"));
   @Nullable
   private static final Class<?> CLASS_JSON_PARSER = MinecraftReflection.findClass("com.goo".concat("gle.gson.JsonParser"));
   @Nullable
   private static final Class<?> CLASS_CHAT_COMPONENT = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.Component")
   );
   @Nullable
   private static final Class<?> CLASS_CRAFT_REGISTRY = MinecraftReflection.findCraftClass("CraftRegistry");
   @Nullable
   private static final Class<?> CLASS_REGISTRY_ACCESS = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("core.IRegistryCustom"), MinecraftReflection.findMcClassName("core.RegistryAccess")
   );
   @Nullable
   private static final MethodHandle PARSE_JSON = MinecraftReflection.findMethod(CLASS_JSON_PARSER, "parse", CLASS_JSON_ELEMENT, String.class);
   @Nullable
   private static final MethodHandle GET_REGISTRY = MinecraftReflection.findStaticMethod(CLASS_CRAFT_REGISTRY, "getMinecraftRegistry", CLASS_REGISTRY_ACCESS);
   private static final AtomicReference<RuntimeException> INITIALIZATION_ERROR = new AtomicReference<>(new UnsupportedOperationException());
   private static final Object JSON_PARSER_INSTANCE;
   private static final Object MC_TEXT_GSON;
   private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE;
   private static final MethodHandle TEXT_SERIALIZER_SERIALIZE;
   private static final MethodHandle TEXT_SERIALIZER_DESERIALIZE_TREE;
   private static final MethodHandle TEXT_SERIALIZER_SERIALIZE_TREE;
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
                  return BukkitComponentSerializer.gson().deserialize((String)TEXT_SERIALIZER_SERIALIZE.invoke((Object)input));
               }

               element = ((Gson)MC_TEXT_GSON).toJsonTree(input);
            }

            return (Component)BukkitComponentSerializer.gson().serializer().fromJson(element.toString(), Component.class);
         } catch (Throwable var3) {
            throw new UnsupportedOperationException(var3);
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
            return (Object)TEXT_SERIALIZER_DESERIALIZE.invoke((String)BukkitComponentSerializer.gson().serialize(component));
         } catch (Throwable var4) {
            throw new UnsupportedOperationException(var4);
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
         } catch (Throwable var5) {
            throw new UnsupportedOperationException(var5);
         }
      }
   }

   static {
      Object gson = null;
      Object jsonParserInstance = null;
      MethodHandle textSerializerDeserialize = null;
      MethodHandle textSerializerSerialize = null;
      MethodHandle textSerializerDeserializeTree = null;
      MethodHandle textSerializerSerializeTree = null;

      try {
         if (CLASS_JSON_PARSER != null) {
            jsonParserInstance = CLASS_JSON_PARSER.getDeclaredConstructor().newInstance();
         }

         if (CLASS_CHAT_COMPONENT != null) {
            Object registryAccess = GET_REGISTRY != null ? (Object)GET_REGISTRY.invoke() : null;
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
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> m.getType().equals(Gson.class))
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
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                  .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(String.class))
                  .min(Comparator.comparing(Method::getName))
                  .orElse(null);
               Method serialize = Arrays.stream(declaredMethods)
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> m.getReturnType().equals(String.class))
                  .filter(m -> m.getParameterCount() == 1 && CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                  .findFirst()
                  .orElse(null);
               Method deserializeTree = Arrays.stream(declaredMethods)
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                  .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                  .findFirst()
                  .orElse(null);
               Method serializeTree = Arrays.stream(declaredMethods)
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> m.getReturnType().equals(CLASS_JSON_ELEMENT))
                  .filter(m -> m.getParameterCount() == 1 && CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                  .findFirst()
                  .orElse(null);
               Method deserializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getReturnType()))
                  .filter(m -> m.getParameterCount() == 2)
                  .filter(m -> m.getParameterTypes()[0].equals(CLASS_JSON_ELEMENT))
                  .filter(m -> m.getParameterTypes()[1].isInstance(registryAccess))
                  .findFirst()
                  .orElse(null);
               Method serializeTreeWithRegistryAccess = Arrays.stream(declaredMethods)
                  .filter(m -> Modifier.isStatic(m.getModifiers()))
                  .filter(m -> m.getReturnType().equals(CLASS_JSON_ELEMENT))
                  .filter(m -> m.getParameterCount() == 2)
                  .filter(m -> CLASS_CHAT_COMPONENT.isAssignableFrom(m.getParameterTypes()[0]))
                  .filter(m -> m.getParameterTypes()[1].isInstance(registryAccess))
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
         }
      } catch (Throwable var18) {
         INITIALIZATION_ERROR.set(new UnsupportedOperationException("Error occurred during initialization", var18));
      }

      MC_TEXT_GSON = gson;
      JSON_PARSER_INSTANCE = jsonParserInstance;
      TEXT_SERIALIZER_DESERIALIZE = textSerializerDeserialize;
      TEXT_SERIALIZER_SERIALIZE = textSerializerSerialize;
      TEXT_SERIALIZER_DESERIALIZE_TREE = textSerializerDeserializeTree;
      TEXT_SERIALIZER_SERIALIZE_TREE = textSerializerSerializeTree;
      SUPPORTED = MC_TEXT_GSON != null
         || TEXT_SERIALIZER_DESERIALIZE != null && TEXT_SERIALIZER_SERIALIZE != null
         || TEXT_SERIALIZER_DESERIALIZE_TREE != null && TEXT_SERIALIZER_SERIALIZE_TREE != null;
   }
}

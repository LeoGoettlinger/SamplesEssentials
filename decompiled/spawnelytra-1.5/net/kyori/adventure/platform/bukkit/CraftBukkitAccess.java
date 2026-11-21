package net.kyori.adventure.platform.bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.platform.facet.Knob;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

final class CraftBukkitAccess {
   @Nullable
   static final Class<?> CLASS_CHAT_COMPONENT = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.IChatBaseComponent"),
      MinecraftReflection.findMcClassName("network.chat.Component")
   );
   @Nullable
   static final Class<?> CLASS_REGISTRY = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("IRegistry"),
      MinecraftReflection.findMcClassName("core.IRegistry"),
      MinecraftReflection.findMcClassName("core.Registry")
   );
   @Nullable
   static final Class<?> CLASS_SERVER_LEVEL = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("server.level.WorldServer"), MinecraftReflection.findMcClassName("server.level.ServerLevel")
   );
   @Nullable
   static final Class<?> CLASS_LEVEL = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("world.level.World"), MinecraftReflection.findMcClassName("world.level.Level")
   );
   @Nullable
   static final Class<?> CLASS_REGISTRY_ACCESS = MinecraftReflection.findClass(
      MinecraftReflection.findMcClassName("core.IRegistryCustom"), MinecraftReflection.findMcClassName("core.RegistryAccess")
   );
   @Nullable
   static final Class<?> CLASS_RESOURCE_KEY = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("resources.ResourceKey"));
   @Nullable
   static final Class<?> CLASS_RESOURCE_LOCATION = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("MinecraftKey"),
      MinecraftReflection.findMcClassName("resources.MinecraftKey"),
      MinecraftReflection.findMcClassName("resources.ResourceLocation")
   );
   @Nullable
   static final Class<?> CLASS_NMS_ENTITY = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("Entity"), MinecraftReflection.findMcClassName("world.entity.Entity")
   );
   @Nullable
   static final Class<?> CLASS_BUILT_IN_REGISTRIES = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("core.registries.BuiltInRegistries"));
   @Nullable
   static final Class<?> CLASS_HOLDER = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("core.Holder"));
   @Nullable
   static final Class<?> CLASS_WRITABLE_REGISTRY = MinecraftReflection.findClass(
      MinecraftReflection.findNmsClassName("IRegistryWritable"),
      MinecraftReflection.findMcClassName("core.IRegistryWritable"),
      MinecraftReflection.findMcClassName("core.WritableRegistry")
   );
   @Nullable
   static final MethodHandle NEW_RESOURCE_LOCATION;

   private CraftBukkitAccess() {
   }

   static {
      MethodHandle newResourceLocation = MinecraftReflection.findConstructor(CLASS_RESOURCE_LOCATION, String.class, String.class);
      if (newResourceLocation == null) {
         newResourceLocation = MinecraftReflection.searchMethod(
            CLASS_RESOURCE_LOCATION, 9, "fromNamespaceAndPath", CLASS_RESOURCE_LOCATION, String.class, String.class
         );
      }

      NEW_RESOURCE_LOCATION = newResourceLocation;
   }

   static final class Book_1_20_5 {
      static final Class<?> CLASS_CRAFT_ITEMSTACK = MinecraftReflection.findCraftClass("inventory.CraftItemStack");
      static final Class<?> CLASS_MC_ITEMSTACK = MinecraftReflection.findMcClass("world.item.ItemStack");
      static final Class<?> CLASS_MC_DATA_COMPONENT_TYPE = MinecraftReflection.findMcClass("core.component.DataComponentType");
      static final Class<?> CLASS_MC_BOOK_CONTENT = MinecraftReflection.findMcClass("world.item.component.WrittenBookContent");
      static final Class<?> CLASS_MC_FILTERABLE = MinecraftReflection.findMcClass("server.network.Filterable");
      static final Class<?> CLASS_CRAFT_REGISTRY = MinecraftReflection.findCraftClass("CraftRegistry");
      static final MethodHandle CREATE_FILTERABLE = MinecraftReflection.searchMethod(CLASS_MC_FILTERABLE, 9, "passThrough", CLASS_MC_FILTERABLE, Object.class);
      static final MethodHandle GET_REGISTRY = MinecraftReflection.findStaticMethod(
         CLASS_CRAFT_REGISTRY, "getMinecraftRegistry", CraftBukkitAccess.CLASS_REGISTRY, CraftBukkitAccess.CLASS_RESOURCE_KEY
      );
      static final MethodHandle CREATE_REGISTRY_KEY = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_RESOURCE_KEY, 9, "createRegistryKey", CraftBukkitAccess.CLASS_RESOURCE_KEY, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      static final MethodHandle NEW_BOOK_CONTENT = MinecraftReflection.findConstructor(
         CLASS_MC_BOOK_CONTENT, CLASS_MC_FILTERABLE, String.class, int.class, List.class, boolean.class
      );
      static final MethodHandle REGISTRY_GET_OPTIONAL = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "getOptional", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      static final Class<?> CLASS_ENUM_HAND = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("EnumHand"),
         MinecraftReflection.findMcClassName("world.EnumHand"),
         MinecraftReflection.findMcClassName("world.InteractionHand")
      );
      static final Object HAND_MAIN = MinecraftReflection.findEnum(CLASS_ENUM_HAND, "MAIN_HAND", 0);
      static final MethodHandle MC_ITEMSTACK_SET = MinecraftReflection.searchMethod(
         CLASS_MC_ITEMSTACK, 1, "set", Object.class, CLASS_MC_DATA_COMPONENT_TYPE, Object.class
      );
      static final MethodHandle CRAFT_ITEMSTACK_NMS_COPY = MinecraftReflection.findStaticMethod(
         CLASS_CRAFT_ITEMSTACK, "asNMSCopy", CLASS_MC_ITEMSTACK, ItemStack.class
      );
      static final MethodHandle CRAFT_ITEMSTACK_CRAFT_MIRROR = MinecraftReflection.findStaticMethod(
         CLASS_CRAFT_ITEMSTACK, "asCraftMirror", CLASS_CRAFT_ITEMSTACK, CLASS_MC_ITEMSTACK
      );
      static final Object WRITTEN_BOOK_COMPONENT_TYPE;
      static final Class<?> PACKET_OPEN_BOOK = MinecraftReflection.findClass(
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutOpenBook"),
         MinecraftReflection.findMcClassName("network.protocol.game.ClientboundOpenBookPacket")
      );
      static final MethodHandle NEW_PACKET_OPEN_BOOK = MinecraftReflection.findConstructor(PACKET_OPEN_BOOK, CLASS_ENUM_HAND);

      static boolean isSupported() {
         return WRITTEN_BOOK_COMPONENT_TYPE != null
            && CREATE_FILTERABLE != null
            && NEW_BOOK_CONTENT != null
            && CRAFT_ITEMSTACK_NMS_COPY != null
            && MC_ITEMSTACK_SET != null
            && CRAFT_ITEMSTACK_CRAFT_MIRROR != null
            && NEW_PACKET_OPEN_BOOK != null
            && HAND_MAIN != null;
      }

      static {
         Object componentTypeRegistry = null;
         Object componentType = null;

         try {
            if (GET_REGISTRY != null && CREATE_REGISTRY_KEY != null && CraftBukkitAccess.NEW_RESOURCE_LOCATION != null && REGISTRY_GET_OPTIONAL != null) {
               Object registryKey = (Object)CREATE_REGISTRY_KEY.invoke(
                  (Object)(Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION.invoke((String)"minecraft", (String)"data_component_type")
               );

               try {
                  componentTypeRegistry = (Object)GET_REGISTRY.invoke((Object)registryKey);
               } catch (Exception var4) {
               }

               if (componentTypeRegistry != null) {
                  componentType = (Optional)REGISTRY_GET_OPTIONAL.invoke(
                        (Object)componentTypeRegistry,
                        (Object)(Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION.invoke((String)"minecraft", (String)"written_book_content")
                     )
                     .orElse(null);
               }
            }
         } catch (Throwable var5) {
            Knob.logError(var5, "Failed to initialize Book_1_20_5 CraftBukkit facet");
         }

         WRITTEN_BOOK_COMPONENT_TYPE = componentType;
      }
   }

   static final class Chat1_19_3 {
      @Nullable
      static final MethodHandle RESOURCE_KEY_CREATE = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_RESOURCE_KEY,
         9,
         "create",
         CraftBukkitAccess.CLASS_RESOURCE_KEY,
         CraftBukkitAccess.CLASS_RESOURCE_KEY,
         CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      @Nullable
      static final MethodHandle SERVER_PLAYER_GET_LEVEL = MinecraftReflection.searchMethod(
         CraftBukkitFacet.CRAFT_PLAYER_GET_HANDLE.type().returnType(), 1, "getLevel", CraftBukkitAccess.CLASS_SERVER_LEVEL
      );
      @Nullable
      static final MethodHandle SERVER_LEVEL_GET_REGISTRY_ACCESS = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_SERVER_LEVEL, 1, "registryAccess", CraftBukkitAccess.CLASS_REGISTRY_ACCESS
      );
      @Nullable
      static final MethodHandle LEVEL_GET_REGISTRY_ACCESS = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_LEVEL, 1, "registryAccess", CraftBukkitAccess.CLASS_REGISTRY_ACCESS
      );
      @Nullable
      static final MethodHandle ACTUAL_GET_REGISTRY_ACCESS = SERVER_LEVEL_GET_REGISTRY_ACCESS == null
         ? LEVEL_GET_REGISTRY_ACCESS
         : SERVER_LEVEL_GET_REGISTRY_ACCESS;
      @Nullable
      static final MethodHandle REGISTRY_ACCESS_GET_REGISTRY_OPTIONAL = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY_ACCESS, 1, "registry", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_KEY
      );
      @Nullable
      static final MethodHandle REGISTRY_GET_OPTIONAL = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "getOptional", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      @Nullable
      static final MethodHandle REGISTRY_GET_HOLDER = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "getHolder", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      @Nullable
      static final MethodHandle REGISTRY_GET_ID = MinecraftReflection.searchMethod(CraftBukkitAccess.CLASS_REGISTRY, 1, "getId", int.class, Object.class);
      @Nullable
      static final MethodHandle DISGUISED_CHAT_PACKET_CONSTRUCTOR;
      @Nullable
      static final MethodHandle CHAT_TYPE_BOUND_NETWORK_CONSTRUCTOR;
      @Nullable
      static final MethodHandle CHAT_TYPE_BOUND_CONSTRUCTOR;
      static final Object CHAT_TYPE_RESOURCE_KEY;

      private Chat1_19_3() {
      }

      static boolean isSupported() {
         return ACTUAL_GET_REGISTRY_ACCESS != null
            && REGISTRY_ACCESS_GET_REGISTRY_OPTIONAL != null
            && REGISTRY_GET_OPTIONAL != null
            && (CHAT_TYPE_BOUND_NETWORK_CONSTRUCTOR != null || CHAT_TYPE_BOUND_CONSTRUCTOR != null)
            && DISGUISED_CHAT_PACKET_CONSTRUCTOR != null
            && CHAT_TYPE_RESOURCE_KEY != null;
      }

      static {
         MethodHandle boundNetworkConstructor = null;
         MethodHandle boundConstructor = null;
         MethodHandle disguisedChatPacketConstructor = null;
         Object chatTypeResourceKey = null;

         try {
            Class<?> classChatTypeBoundNetwork = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("network.chat.ChatType$BoundNetwork"));
            if (classChatTypeBoundNetwork == null) {
               Class<?> parentClass = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("network.chat.ChatMessageType"));
               if (parentClass != null) {
                  for (Class<?> childClass : parentClass.getClasses()) {
                     boundNetworkConstructor = MinecraftReflection.findConstructor(
                        childClass, int.class, CraftBukkitAccess.CLASS_CHAT_COMPONENT, CraftBukkitAccess.CLASS_CHAT_COMPONENT
                     );
                     if (boundNetworkConstructor != null) {
                        classChatTypeBoundNetwork = childClass;
                        break;
                     }
                  }
               }
            }

            Class<?> classChatTypeBound = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("network.chat.ChatType$BoundNetwork"));
            if (classChatTypeBound == null) {
               Class<?> parentClass = MinecraftReflection.findClass(MinecraftReflection.findMcClassName("network.chat.ChatMessageType"));
               if (parentClass != null) {
                  for (Class<?> childClassx : parentClass.getClasses()) {
                     boundConstructor = MinecraftReflection.findConstructor(
                        childClassx, CraftBukkitAccess.CLASS_HOLDER, CraftBukkitAccess.CLASS_CHAT_COMPONENT, Optional.class
                     );
                     if (boundConstructor != null) {
                        classChatTypeBound = childClassx;
                        break;
                     }
                  }
               }
            }

            Class<?> disguisedChatPacketClass = MinecraftReflection.findClass(
               MinecraftReflection.findMcClassName("network.protocol.game.ClientboundDisguisedChatPacket")
            );
            if (disguisedChatPacketClass != null) {
               if (classChatTypeBoundNetwork != null) {
                  disguisedChatPacketConstructor = MinecraftReflection.findConstructor(
                     disguisedChatPacketClass, CraftBukkitAccess.CLASS_CHAT_COMPONENT, classChatTypeBoundNetwork
                  );
               } else if (classChatTypeBound != null) {
                  disguisedChatPacketConstructor = MinecraftReflection.findConstructor(
                     disguisedChatPacketClass, CraftBukkitAccess.CLASS_CHAT_COMPONENT, classChatTypeBound
                  );
               }
            }

            if (CraftBukkitAccess.NEW_RESOURCE_LOCATION != null && RESOURCE_KEY_CREATE != null) {
               MethodHandle createRegistryKey = MinecraftReflection.searchMethod(
                  CraftBukkitAccess.CLASS_RESOURCE_KEY, 9, "createRegistryKey", CraftBukkitAccess.CLASS_RESOURCE_KEY, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
               );
               if (createRegistryKey != null) {
                  chatTypeResourceKey = (Object)createRegistryKey.invoke(
                     (Object)(Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION.invoke((String)"minecraft", (String)"chat_type")
                  );
               }
            }
         } catch (Throwable var11) {
            Knob.logError(var11, "Failed to initialize 1.19.3 chat support");
         }

         DISGUISED_CHAT_PACKET_CONSTRUCTOR = disguisedChatPacketConstructor;
         CHAT_TYPE_BOUND_NETWORK_CONSTRUCTOR = boundNetworkConstructor;
         CHAT_TYPE_BOUND_CONSTRUCTOR = boundConstructor;
         CHAT_TYPE_RESOURCE_KEY = chatTypeResourceKey;
      }
   }

   static final class EntitySound {
      @Nullable
      static final Class<?> CLASS_CLIENTBOUND_ENTITY_SOUND = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("PacketPlayOutEntitySound"),
         MinecraftReflection.findMcClassName("network.protocol.game.PacketPlayOutEntitySound"),
         MinecraftReflection.findMcClassName("network.protocol.game.ClientboundSoundEntityPacket")
      );
      @Nullable
      static final Class<?> CLASS_SOUND_SOURCE = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("SoundCategory"),
         MinecraftReflection.findMcClassName("sounds.SoundCategory"),
         MinecraftReflection.findMcClassName("sounds.SoundSource")
      );
      @Nullable
      static final Class<?> CLASS_SOUND_EVENT = MinecraftReflection.findClass(
         MinecraftReflection.findNmsClassName("SoundEffect"),
         MinecraftReflection.findMcClassName("sounds.SoundEffect"),
         MinecraftReflection.findMcClassName("sounds.SoundEvent")
      );
      @Nullable
      static final MethodHandle SOUND_SOURCE_GET_NAME;

      private EntitySound() {
      }

      static boolean isSupported() {
         return SOUND_SOURCE_GET_NAME != null;
      }

      static {
         MethodHandle soundSourceGetName = null;
         if (CLASS_SOUND_SOURCE != null) {
            for (Method method : CLASS_SOUND_SOURCE.getDeclaredMethods()) {
               if (method.getReturnType().equals(String.class)
                  && method.getParameterCount() == 0
                  && !"name".equals(method.getName())
                  && Modifier.isPublic(method.getModifiers())) {
                  try {
                     soundSourceGetName = MinecraftReflection.lookup().unreflect(method);
                  } catch (IllegalAccessException var6) {
                  }
                  break;
               }
            }
         }

         SOUND_SOURCE_GET_NAME = soundSourceGetName;
      }
   }

   static final class EntitySound_1_19_3 {
      @Nullable
      static final MethodHandle REGISTRY_GET_OPTIONAL = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "getOptional", Optional.class, CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      @Nullable
      static final MethodHandle REGISTRY_WRAP_AS_HOLDER = MinecraftReflection.searchMethod(
         CraftBukkitAccess.CLASS_REGISTRY, 1, "wrapAsHolder", CraftBukkitAccess.CLASS_HOLDER, Object.class
      );
      @Nullable
      static final MethodHandle SOUND_EVENT_CREATE_VARIABLE_RANGE = MinecraftReflection.searchMethod(
         CraftBukkitAccess.EntitySound.CLASS_SOUND_EVENT,
         9,
         "createVariableRangeEvent",
         CraftBukkitAccess.EntitySound.CLASS_SOUND_EVENT,
         CraftBukkitAccess.CLASS_RESOURCE_LOCATION
      );
      @Nullable
      static final MethodHandle NEW_CLIENTBOUND_ENTITY_SOUND = MinecraftReflection.findConstructor(
         CraftBukkitAccess.EntitySound.CLASS_CLIENTBOUND_ENTITY_SOUND,
         CraftBukkitAccess.CLASS_HOLDER,
         CraftBukkitAccess.EntitySound.CLASS_SOUND_SOURCE,
         CraftBukkitAccess.CLASS_NMS_ENTITY,
         float.class,
         float.class,
         long.class
      );
      @Nullable
      static final Object SOUND_EVENT_REGISTRY;

      private EntitySound_1_19_3() {
      }

      static boolean isSupported() {
         return NEW_CLIENTBOUND_ENTITY_SOUND != null
            && SOUND_EVENT_REGISTRY != null
            && CraftBukkitAccess.NEW_RESOURCE_LOCATION != null
            && REGISTRY_GET_OPTIONAL != null
            && REGISTRY_WRAP_AS_HOLDER != null
            && SOUND_EVENT_CREATE_VARIABLE_RANGE != null;
      }

      static {
         Object soundEventRegistry = null;

         try {
            Field soundEventRegistryField = MinecraftReflection.findField(
               CraftBukkitAccess.CLASS_BUILT_IN_REGISTRIES, CraftBukkitAccess.CLASS_REGISTRY, "SOUND_EVENT"
            );
            if (soundEventRegistryField != null) {
               soundEventRegistry = soundEventRegistryField.get(null);
            } else if (CraftBukkitAccess.CLASS_BUILT_IN_REGISTRIES != null && REGISTRY_GET_OPTIONAL != null && CraftBukkitAccess.NEW_RESOURCE_LOCATION != null) {
               Object rootRegistry = null;

               for (Field field : CraftBukkitAccess.CLASS_BUILT_IN_REGISTRIES.getDeclaredFields()) {
                  int mask = 26;
                  if ((field.getModifiers() & 26) == 26 && field.getType().equals(CraftBukkitAccess.CLASS_WRITABLE_REGISTRY)) {
                     field.setAccessible(true);
                     rootRegistry = field.get(null);
                     break;
                  }
               }

               if (rootRegistry != null) {
                  soundEventRegistry = (Optional)REGISTRY_GET_OPTIONAL.invoke(
                        (Object)rootRegistry, (Object)(Object)CraftBukkitAccess.NEW_RESOURCE_LOCATION.invoke((String)"minecraft", (String)"sound_event")
                     )
                     .orElse(null);
               }
            }
         } catch (Throwable var8) {
            Knob.logError(var8, "Failed to initialize EntitySound_1_19_3 CraftBukkit facet");
         }

         SOUND_EVENT_REGISTRY = soundEventRegistry;
      }
   }
}

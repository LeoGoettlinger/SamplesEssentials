package de.oliver.fancysitula.versions.v1_21_9.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;
import de.oliver.fancysitula.api.utils.reflections.ReflectionUtils;
import de.oliver.fancysitula.versions.v1_21_9.utils.VanillaPlayerAdapter;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.ChickenVariant;
import net.minecraft.world.entity.animal.CowVariant;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.animal.MushroomCow.Variant;
import net.minecraft.world.entity.animal.armadillo.Armadillo.ArmadilloState;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.sniffer.Sniffer.State;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.inventory.ItemStack;

public class ClientboundSetEntityDataPacketImpl extends FS_ClientboundSetEntityDataPacket {
   private static final ConcurrentMap<String, Class<?>> ENTITY_CLASS_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, net.minecraft.network.syncher.EntityDataAccessor<Object>> ENTITY_ACCESSOR_CACHE = new ConcurrentHashMap<>();

   public ClientboundSetEntityDataPacketImpl(int entityId, List<FS_ClientboundSetEntityDataPacket.EntityData> entityData) {
      super(entityId, entityData);
   }

   private static Class<?> getEntityClassCached(String className) throws ClassNotFoundException {
      Class<?> cached = ENTITY_CLASS_CACHE.get(className);
      if (cached != null) {
         return cached;
      } else {
         ClassLoader classLoader = MinecraftServer.class.getClassLoader();
         Class<?> resolved = classLoader.loadClass(className);
         ENTITY_CLASS_CACHE.put(className, resolved);
         return resolved;
      }
   }

   private static net.minecraft.network.syncher.EntityDataAccessor<Object> getAccessorCached(String entityClassName, String fieldName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
      String key = entityClassName + "#" + fieldName;
      net.minecraft.network.syncher.EntityDataAccessor<Object> cached = ENTITY_ACCESSOR_CACHE.get(key);
      if (cached != null) {
         return cached;
      } else {
         Class<?> entityClass = getEntityClassCached(entityClassName);
         net.minecraft.network.syncher.EntityDataAccessor<Object> accessor = ReflectionUtils.getStaticField(entityClass, fieldName);
         ENTITY_ACCESSOR_CACHE.put(key, accessor);
         return accessor;
      }
   }

   @Override
   public Object createPacket() {
      List<DataValue<?>> dataValues = new ArrayList<>();

      for (FS_ClientboundSetEntityDataPacket.EntityData data : this.entityData) {
         try {
            String entityClassName = data.getAccessor().entityClassName();
            String accessorFieldName = data.getAccessor().accessorFieldName();
            net.minecraft.network.syncher.EntityDataAccessor<Object> accessor = getAccessorCached(entityClassName, accessorFieldName);
            Object vanillaValue = data.getValue();
            if (data.getValue() != null) {
               if (data.getValue() instanceof Component c) {
                  vanillaValue = PaperAdventure.asVanilla(c);
               }

               if (data.getValue() instanceof Optional<?> opt) {
                  if (opt.isPresent() && opt.get() instanceof Component c) {
                     vanillaValue = Optional.of(PaperAdventure.asVanilla(c));
                  } else {
                     vanillaValue = Optional.empty();
                  }
               }

               if (data.getValue() instanceof ItemStack i) {
                  vanillaValue = net.minecraft.world.item.ItemStack.fromBukkitCopy(i);
               }

               if (data.getValue() instanceof BlockState b) {
                  vanillaValue = ((CraftBlockState)b).getHandle();
               }

               if (data.getValue() instanceof String s && accessorFieldName.equals("DATA_STATE") && entityClassName.contains("Armadillo")) {
                  try {
                     vanillaValue = ArmadilloState.valueOf(s.toUpperCase());
                  } catch (IllegalArgumentException var20) {
                     vanillaValue = ArmadilloState.IDLE;
                  }
               }

               if (data.getValue() instanceof String s && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Wolf")) {
                  String variantKey = s.startsWith("wolf:") ? s.substring("wolf:".length()) : s;
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<WolfVariant> registry = server.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
                  Reference<WolfVariant> holder = (Reference<WolfVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Cat")) {
                  String variantKey = sx.startsWith("cat:") ? sx.substring("cat:".length()) : sx;
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<CatVariant> registry = server.registryAccess().lookupOrThrow(Registries.CAT_VARIANT);
                  Reference<CatVariant> holder = (Reference<CatVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sxx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Frog")) {
                  String variantKey = sxx.startsWith("frog:") ? sxx.substring("frog:".length()) : sxx;
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<FrogVariant> registry = server.registryAccess().lookupOrThrow(Registries.FROG_VARIANT);
                  Reference<FrogVariant> holder = (Reference<FrogVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sxxx
                  && accessorFieldName.equals("DATA_VARIANT_ID")
                  && entityClassName.contains("Cow")
                  && !entityClassName.contains("Mushroom")) {
                  String variantKey = sxxx.startsWith("minecraft:") ? sxxx : "minecraft:" + sxxx.toLowerCase();
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<CowVariant> registry = server.registryAccess().lookupOrThrow(Registries.COW_VARIANT);
                  Reference<CowVariant> holder = (Reference<CowVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sxxxx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Chicken")) {
                  String variantKey = sxxxx.startsWith("minecraft:") ? sxxxx : "minecraft:" + sxxxx.toLowerCase();
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<ChickenVariant> registry = server.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT);
                  Reference<ChickenVariant> holder = (Reference<ChickenVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sxxxxx
                  && accessorFieldName.equals("DATA_VARIANT_ID")
                  && entityClassName.contains("Pig")
                  && !entityClassName.contains("Piglin")) {
                  String variantKey = sxxxxx.startsWith("minecraft:") ? sxxxxx : "minecraft:" + sxxxxx.toLowerCase();
                  ResourceLocation loc = ResourceLocation.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<PigVariant> registry = server.registryAccess().lookupOrThrow(Registries.PIG_VARIANT);
                  Reference<PigVariant> holder = (Reference<PigVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_STATE") && entityClassName.contains("Sniffer")) {
                  try {
                     vanillaValue = State.valueOf(sxxxxxx.toUpperCase());
                  } catch (IllegalArgumentException var19) {
                     vanillaValue = State.IDLING;
                  }
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_TYPE") && entityClassName.contains("MushroomCow")) {
                  try {
                     vanillaValue = Variant.valueOf(sxxxxxx.toUpperCase());
                  } catch (IllegalArgumentException var18) {
                     vanillaValue = Variant.RED;
                  }
               }

               if (data.getValue() instanceof Byte b && accessorFieldName.equals("DATA_SPELL_CASTING_ID") && entityClassName.contains("SpellcasterIllager")) {
                  vanillaValue = b;
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_VILLAGER_DATA") && entityClassName.contains("Villager")) {
                  String typeKey = "minecraft:plains";
                  String profKey = "minecraft:none";
                  int level = 1;

                  for (String part : sxxxxxx.split(",")) {
                     part = part.trim();
                     if (part.startsWith("type:")) {
                        typeKey = part.substring("type:".length());
                     } else if (part.startsWith("profession:")) {
                        profKey = part.substring("profession:".length());
                     } else if (part.startsWith("level:")) {
                        try {
                           level = Integer.parseInt(part.substring("level:".length()));
                        } catch (NumberFormatException var17) {
                        }
                     }
                  }

                  ResourceLocation typeLoc = ResourceLocation.parse(typeKey);
                  ResourceLocation profLoc = ResourceLocation.parse(profKey);
                  Reference<VillagerType> typeHolder = (Reference<VillagerType>)BuiltInRegistries.VILLAGER_TYPE.get(typeLoc).orElse(null);
                  Reference<VillagerProfession> profHolder = (Reference<VillagerProfession>)BuiltInRegistries.VILLAGER_PROFESSION.get(profLoc).orElse(null);
                  if (typeHolder != null && profHolder != null) {
                     vanillaValue = new VillagerData(typeHolder, profHolder, level);
                  }
               }

               dataValues.add(DataValue.create(accessor, vanillaValue));
            }
         } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException var21) {
            var21.printStackTrace();
         }
      }

      return new ClientboundSetEntityDataPacket(this.entityId, dataValues);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}

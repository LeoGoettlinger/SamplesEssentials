package de.oliver.fancysitula.versions.v1_21_11.packets;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;
import de.oliver.fancysitula.api.utils.FS_GameProfile;
import de.oliver.fancysitula.api.utils.reflections.ReflectionUtils;
import de.oliver.fancysitula.versions.v1_21_11.utils.GameProfileImpl;
import de.oliver.fancysitula.versions.v1_21_11.utils.VanillaPlayerAdapter;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.armadillo.Armadillo.ArmadilloState;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.cow.MushroomCow.Variant;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.sniffer.Sniffer.State;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin.Patch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.ResolvableProfile.Static;
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

   private static String translateFieldName(String entityClassName, String fieldName) {
      return fieldName.equals("DATA_REMAINING_ANGER_TIME") ? "DATA_ANGER_END_TIME" : fieldName;
   }

   private static String translateClassName(String entityClassName, String fieldName) {
      return !entityClassName.equals("net.minecraft.world.entity.player.Player")
            || !fieldName.equals("DATA_PLAYER_MODE_CUSTOMISATION") && !fieldName.equals("DATA_PLAYER_MAIN_HAND")
         ? entityClassName
         : "net.minecraft.world.entity.Avatar";
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
            String originalFieldName = data.getAccessor().accessorFieldName();
            String accessorFieldName = translateFieldName(data.getAccessor().entityClassName(), originalFieldName);
            String entityClassName = translateClassName(data.getAccessor().entityClassName(), originalFieldName);
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
                  } catch (IllegalArgumentException var21) {
                     vanillaValue = ArmadilloState.IDLE;
                  }
               }

               if (data.getValue() instanceof String s && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Wolf")) {
                  String variantKey = s.startsWith("wolf:") ? s.substring("wolf:".length()) : s;
                  Identifier loc = Identifier.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry<WolfVariant> registry = server.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT);
                  Reference<WolfVariant> holder = (Reference<WolfVariant>)registry.get(loc).orElse(null);
                  if (holder != null) {
                     vanillaValue = holder;
                  }
               }

               if (data.getValue() instanceof String sx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Cat")) {
                  String variantKey = sx.startsWith("cat:") ? sx.substring("cat:".length()) : sx;
                  Identifier loc = Identifier.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry registry = server.registryAccess().lookupOrThrow(Registries.CAT_VARIANT);
                  Optional optHolder = registry.get(loc);
                  if (optHolder.isPresent()) {
                     vanillaValue = optHolder.get();
                  }
               }

               if (data.getValue() instanceof String sxx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Frog")) {
                  String variantKey = sxx.startsWith("frog:") ? sxx.substring("frog:".length()) : sxx;
                  Identifier loc = Identifier.parse(variantKey);
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
                  Identifier loc = Identifier.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry registry = server.registryAccess().lookupOrThrow(Registries.COW_VARIANT);
                  Optional optHolder = registry.get(loc);
                  if (optHolder.isPresent()) {
                     vanillaValue = optHolder.get();
                  }
               }

               if (data.getValue() instanceof String sxxxx && accessorFieldName.equals("DATA_VARIANT_ID") && entityClassName.contains("Chicken")) {
                  String variantKey = sxxxx.startsWith("minecraft:") ? sxxxx : "minecraft:" + sxxxx.toLowerCase();
                  Identifier loc = Identifier.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry registry = server.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT);
                  Optional optHolder = registry.get(loc);
                  if (optHolder.isPresent()) {
                     vanillaValue = optHolder.get();
                  }
               }

               if (data.getValue() instanceof String sxxxxx
                  && accessorFieldName.equals("DATA_VARIANT_ID")
                  && entityClassName.contains("Pig")
                  && !entityClassName.contains("Piglin")) {
                  String variantKey = sxxxxx.startsWith("minecraft:") ? sxxxxx : "minecraft:" + sxxxxx.toLowerCase();
                  Identifier loc = Identifier.parse(variantKey);
                  MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
                  Registry registry = server.registryAccess().lookupOrThrow(Registries.PIG_VARIANT);
                  Optional optHolder = registry.get(loc);
                  if (optHolder.isPresent()) {
                     vanillaValue = optHolder.get();
                  }
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_STATE") && entityClassName.contains("Sniffer")) {
                  try {
                     vanillaValue = State.valueOf(sxxxxxx.toUpperCase());
                  } catch (IllegalArgumentException var20) {
                     vanillaValue = State.IDLING;
                  }
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_TYPE") && entityClassName.contains("MushroomCow")) {
                  try {
                     vanillaValue = Variant.valueOf(sxxxxxx.toUpperCase());
                  } catch (IllegalArgumentException var19) {
                     vanillaValue = Variant.RED;
                  }
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
                        } catch (NumberFormatException var18) {
                        }
                     }
                  }

                  Identifier typeLoc = Identifier.parse(typeKey);
                  Identifier profLoc = Identifier.parse(profKey);
                  Reference<VillagerType> typeHolder = (Reference<VillagerType>)BuiltInRegistries.VILLAGER_TYPE.get(typeLoc).orElse(null);
                  Reference<VillagerProfession> profHolder = (Reference<VillagerProfession>)BuiltInRegistries.VILLAGER_PROFESSION.get(profLoc).orElse(null);
                  if (typeHolder != null && profHolder != null) {
                     vanillaValue = new VillagerData(typeHolder, profHolder, level);
                  }
               }

               if (data.getValue() instanceof FS_GameProfile profile && accessorFieldName.equals("DATA_PROFILE")) {
                  GameProfile authProfile = GameProfileImpl.asVanilla(profile);
                  if (profile.hasTextureAssets()) {
                     Optional<ResourceTexture> bodyTexture = Optional.empty();
                     Optional<ResourceTexture> capeTexture = Optional.empty();
                     Optional<ResourceTexture> elytraTexture = Optional.empty();
                     Optional<PlayerModelType> modelType = Optional.empty();
                     if (profile.getSkinTextureAsset() != null) {
                        bodyTexture = Optional.of(new ResourceTexture(Identifier.parse(profile.getSkinTextureAsset())));
                     }

                     if (profile.getCapeTextureAsset() != null) {
                        capeTexture = Optional.of(new ResourceTexture(Identifier.parse(profile.getCapeTextureAsset())));
                     }

                     if (profile.getElytraTextureAsset() != null) {
                        elytraTexture = Optional.of(new ResourceTexture(Identifier.parse(profile.getElytraTextureAsset())));
                     }

                     if (profile.getModelType() != null) {
                        modelType = Optional.of("SLIM".equalsIgnoreCase(profile.getModelType()) ? PlayerModelType.SLIM : PlayerModelType.WIDE);
                     }

                     Patch skinPatch = Patch.create(bodyTexture, capeTexture, elytraTexture, modelType);
                     vanillaValue = new Static(Either.left(authProfile), skinPatch);
                  } else {
                     vanillaValue = ResolvableProfile.createResolved(authProfile);
                  }
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_PLAYER_MAIN_HAND")) {
                  vanillaValue = sxxxxxx.equalsIgnoreCase("LEFT") ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
               }

               if (data.getValue() instanceof String sxxxxxx && accessorFieldName.equals("DATA_POSE")) {
                  String var64 = sxxxxxx.toUpperCase();

                  vanillaValue = switch (var64) {
                     case "STANDING" -> Pose.STANDING;
                     case "FALL_FLYING" -> Pose.FALL_FLYING;
                     case "SLEEPING" -> Pose.SLEEPING;
                     case "SWIMMING" -> Pose.SWIMMING;
                     case "SPIN_ATTACK" -> Pose.SPIN_ATTACK;
                     case "CROUCHING", "SNEAKING" -> Pose.CROUCHING;
                     case "LONG_JUMPING" -> Pose.LONG_JUMPING;
                     case "DYING" -> Pose.DYING;
                     case "CROAKING" -> Pose.CROAKING;
                     case "USING_TONGUE" -> Pose.USING_TONGUE;
                     case "SITTING" -> Pose.SITTING;
                     case "ROARING" -> Pose.ROARING;
                     case "SNIFFING" -> Pose.SNIFFING;
                     case "EMERGING" -> Pose.EMERGING;
                     case "DIGGING" -> Pose.DIGGING;
                     case "SLIDING" -> Pose.SLIDING;
                     case "SHOOTING" -> Pose.SHOOTING;
                     case "INHALING" -> Pose.INHALING;
                     default -> Pose.STANDING;
                  };
               }

               dataValues.add(DataValue.create(accessor, vanillaValue));
            }
         } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException var22) {
            var22.printStackTrace();
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

   static {
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Entity", Entity.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.LivingEntity", LivingEntity.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Mob", Mob.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.AgeableMob", AgeableMob.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Interaction", Interaction.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.player.Player", Player.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.decoration.ArmorStand", ArmorStand.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Display", Display.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Display$TextDisplay", TextDisplay.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Display$BlockDisplay", BlockDisplay.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Display$ItemDisplay", ItemDisplay.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Cow", Cow.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Chicken", Chicken.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Pig", Pig.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.MushroomCow", MushroomCow.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Cat", Cat.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Bee", Bee.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Fox", Fox.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Panda", Panda.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Parrot", Parrot.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.PolarBear", PolarBear.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Rabbit", Rabbit.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Sheep", Sheep.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.Turtle", Turtle.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.cow.Cow", Cow.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.cow.MushroomCow", MushroomCow.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.chicken.Chicken", Chicken.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.pig.Pig", Pig.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.feline.Cat", Cat.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.bee.Bee", Bee.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.fox.Fox", Fox.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.panda.Panda", Panda.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.parrot.Parrot", Parrot.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.rabbit.Rabbit", Rabbit.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.sheep.Sheep", Sheep.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.fish.TropicalFish", TropicalFish.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.equine.Llama", Llama.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.equine.Horse", Horse.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.frog.Frog", Frog.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.wolf.Wolf", Wolf.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.armadillo.Armadillo", Armadillo.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.sniffer.Sniffer", Sniffer.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.axolotl.Axolotl", Axolotl.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.camel.Camel", Camel.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.animal.goat.Goat", Goat.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.monster.SpellcasterIllager", SpellcasterIllager.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.monster.Creeper", Creeper.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.raid.Raider", Raider.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.npc.Villager", Villager.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.Avatar", Avatar.class);
      ENTITY_CLASS_CACHE.put("net.minecraft.world.entity.decoration.Mannequin", Mannequin.class);
   }
}

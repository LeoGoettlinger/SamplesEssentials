package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityGuardian;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHorse;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySizeable;
import ac.grim.grimac.utils.data.packetentity.PacketEntityTrackXRot;
import ac.grim.grimac.utils.math.GrimMath;
import lombok.Generated;

public final class BoundingBoxSize {
   public static float getWidth(GrimPlayer player, PacketEntity packetEntity) {
      float width = getWidthMinusBaby(player, packetEntity);
      return width * (packetEntity.isBaby ? getBabyScaleFactor(player, packetEntity) : 1.0F);
   }

   private static float getWidthMinusBaby(GrimPlayer player, PacketEntity packetEntity) {
      EntityType type = packetEntity.getType();
      if (type == EntityTypes.AXOLOTL) {
         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby) {
            return 0.375F;
         } else {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) && packetEntity.isBaby ? 0.5F : 0.75F;
         }
      } else if (type == EntityTypes.PANDA) {
         return 1.3F;
      } else if (type == EntityTypes.BAT
         || type == EntityTypes.PARROT
         || type == EntityTypes.COD
         || type == EntityTypes.EVOKER_FANGS
         || type == EntityTypes.TROPICAL_FISH
         || type == EntityTypes.FROG
         || type == EntityTypes.COPPER_GOLEM) {
         return 0.5F;
      } else if (type == EntityTypes.ARMADILLO
         || type == EntityTypes.BEE
         || type == EntityTypes.PUFFERFISH
         || type == EntityTypes.SALMON
         || type == EntityTypes.SNOW_GOLEM
         || type == EntityTypes.CAVE_SPIDER) {
         return 0.7F;
      } else if (type == EntityTypes.WITHER_SKELETON) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.7F : 0.72F;
      } else if (type == EntityTypes.WITHER_SKULL || type == EntityTypes.SHULKER_BULLET) {
         return 0.3125F;
      } else if (type == EntityTypes.HOGLIN || type == EntityTypes.ZOGLIN) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 0.75F : 1.3964844F;
      } else if (type == EntityTypes.SKELETON_HORSE
         || type == EntityTypes.ZOMBIE_HORSE
         || type == EntityTypes.HORSE
         || type == EntityTypes.DONKEY
         || type == EntityTypes.MULE) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 1.3964844F : 1.4F;
      } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.BOAT)) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 1.375F : 1.5F;
      } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_NAUTILUS)) {
         return 0.875F;
      } else if (type == EntityTypes.HAPPY_GHAST) {
         return 4.0F;
      } else if (type != EntityTypes.CHICKEN
         && type != EntityTypes.ENDERMITE
         && type != EntityTypes.SILVERFISH
         && type != EntityTypes.VEX
         && type != EntityTypes.TADPOLE) {
         if (type == EntityTypes.RABBIT) {
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
               return packetEntity.isBaby ? 0.24F : 0.49F;
            } else {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.4F : 0.6F;
            }
         } else if (type == EntityTypes.FOX && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby) {
            return 0.36F;
         } else if (type == EntityTypes.GOAT && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby) {
            return 0.45F;
         } else if (type == EntityTypes.CREAKING
            || type == EntityTypes.STRIDER
            || type == EntityTypes.COW
            || type == EntityTypes.SHEEP
            || type == EntityTypes.MOOSHROOM
            || type == EntityTypes.PIG
            || type == EntityTypes.LLAMA
            || type == EntityTypes.DOLPHIN
            || type == EntityTypes.WITHER
            || type == EntityTypes.TRADER_LLAMA
            || type == EntityTypes.WARDEN
            || type == EntityTypes.GOAT) {
            return 0.9F;
         } else if (type == EntityTypes.PHANTOM) {
            return packetEntity instanceof PacketEntitySizeable sizeable ? 0.9F + sizeable.size * 0.2F : 1.5F;
         } else if (packetEntity instanceof PacketEntityGuardian guardian) {
            return guardian.isElder ? 1.9975F : 0.85F;
         } else if (type == EntityTypes.END_CRYSTAL) {
            return 2.0F;
         } else if (type == EntityTypes.ENDER_DRAGON) {
            return 16.0F;
         } else if (type == EntityTypes.FIREBALL) {
            return 1.0F;
         } else if (type == EntityTypes.GHAST) {
            return 4.0F;
         } else if (type == EntityTypes.GIANT) {
            return 3.6F;
         } else if (type == EntityTypes.GUARDIAN) {
            return 0.85F;
         } else if (type == EntityTypes.IRON_GOLEM) {
            return 1.4F;
         } else if (type == EntityTypes.SULFUR_CUBE && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)) {
            return packetEntity instanceof PacketEntitySizeable sizeable ? 0.49F * sizeable.size : 0.49F;
         } else if (type == EntityTypes.MAGMA_CUBE) {
            if (packetEntity instanceof PacketEntitySizeable sizeable) {
               float size = sizeable.size;
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)
                  ? 0.52F * size
                  : (
                     player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                        ? 2.04F * (0.255F * size)
                        : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 0.51000005F * size : 0.6F * size)
                  );
            } else {
               return 0.98F;
            }
         } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.MINECART_ABSTRACT)) {
            return 0.98F;
         } else if (type == EntityTypes.PLAYER || type == EntityTypes.MANNEQUIN) {
            return 0.6F;
         } else if (type == EntityTypes.POLAR_BEAR) {
            return 1.4F;
         } else if (type == EntityTypes.RAVAGER) {
            return 1.95F;
         } else if (type == EntityTypes.SHULKER) {
            return 1.0F;
         } else if (type != EntityTypes.SLIME && (!player.getClientVersion().isOlderThan(ClientVersion.V_26_2) || type != EntityTypes.SULFUR_CUBE)) {
            if (type == EntityTypes.SMALL_FIREBALL) {
               return 0.3125F;
            } else if (type == EntityTypes.SPIDER) {
               return 1.4F;
            } else if (type == EntityTypes.SQUID || type == EntityTypes.GLOW_SQUID) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) && packetEntity.isBaby
                  ? 0.5F
                  : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.8F : 0.95F);
            } else if (type == EntityTypes.TURTLE) {
               return 1.2F;
            } else if (type == EntityTypes.ALLAY) {
               return 0.35F;
            } else if (type == EntityTypes.SNIFFER) {
               return 1.9F;
            } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.CAMEL)) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 0.95F : 1.7F;
            } else if (type == EntityTypes.WIND_CHARGE) {
               return 0.3125F;
            } else if (type == EntityTypes.ARMOR_STAND) {
               return 0.5F;
            } else if (type == EntityTypes.FALLING_BLOCK) {
               return 0.98F;
            } else if (type == EntityTypes.FIREWORK_ROCKET) {
               return 0.25F;
            } else {
               return (
                        EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_PIGLIN)
                           || type == EntityTypes.DROWNED
                           || type == EntityTypes.HUSK
                           || type == EntityTypes.ZOMBIE
                           || type == EntityTypes.VILLAGER
                           || type == EntityTypes.ZOMBIE_VILLAGER
                           || type == EntityTypes.ZOMBIFIED_PIGLIN
                     )
                     && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)
                     && packetEntity.isBaby
                  ? 0.49F
                  : 0.6F;
            }
         } else if (packetEntity instanceof PacketEntitySizeable sizeable) {
            float size = sizeable.size;
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)
               ? 0.52F * size
               : (
                  player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                     ? 2.04F * (0.255F * size)
                     : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 0.51000005F * size : 0.6F * size)
               );
         } else {
            return 0.3125F;
         }
      } else {
         return type == EntityTypes.CHICKEN && packetEntity.isBaby && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) ? 0.3F : 0.4F;
      }
   }

   public static Vector3d getRidingOffsetFromVehicle(PacketEntity entity, GrimPlayer player) {
      SimpleCollisionBox box = entity.getPossibleCollisionBoxes();
      double x = (box.maxX + box.minX) / 2.0;
      double y = box.minY;
      double z = (box.maxZ + box.minZ) / 2.0;
      if (entity instanceof PacketEntityTrackXRot xRotEntity) {
         if (EntityTypes.isTypeInstanceOf(entity.getType(), EntityTypes.BOAT)) {
            float f = 0.0F;
            float f1 = (float)(getPassengerRidingOffset(player, entity) - 0.35F);
            if (!entity.passengers.isEmpty()) {
               int i = entity.passengers.indexOf(player.compensatedEntities.self);
               if (i == 0) {
                  f = 0.2F;
               } else if (i == 1) {
                  f = -0.6F;
               }
            }

            Vector3d vec3 = new Vector3d(f, 0.0, 0.0);
            vec3 = yRot(GrimMath.radians(-xRotEntity.interpYaw) - (float) (Math.PI / 2), vec3);
            return new Vector3d(x + vec3.x, y + f1, z + vec3.z);
         }

         if (entity.getType() == EntityTypes.LLAMA) {
            float f = player.trigHandler.cos(GrimMath.radians(xRotEntity.interpYaw));
            float f1 = player.trigHandler.sin(GrimMath.radians(xRotEntity.interpYaw));
            return new Vector3d(x + 0.3F * f1, y + getPassengerRidingOffset(player, entity) - 0.35F, z + 0.3F * f);
         }

         if (entity.getType() == EntityTypes.CHICKEN) {
            float f = player.trigHandler.sin(GrimMath.radians(xRotEntity.interpYaw));
            float f1 = player.trigHandler.cos(GrimMath.radians(xRotEntity.interpYaw));
            y += player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) ? (entity.isBaby ? 0.375 : 0.7) : getHeight(player, entity) * 0.5F;
            return new Vector3d(x + 0.1F * f, y - 0.35F, z - 0.1F * f1);
         }
      }

      return new Vector3d(x, y + getPassengerRidingOffset(player, entity) - 0.35F, z);
   }

   private static Vector3d yRot(float yaw, Vector3d start) {
      double cos = (float)Math.cos(yaw);
      double sin = (float)Math.sin(yaw);
      return new Vector3d(start.x * cos + start.z * sin, start.y, start.z * cos - start.x * sin);
   }

   public static float getHeight(GrimPlayer player, PacketEntity packetEntity) {
      float height = getHeightMinusBaby(player, packetEntity);
      return height * (packetEntity.isBaby ? getBabyScaleFactor(player, packetEntity) : 1.0F);
   }

   public static double getMyRidingOffset(PacketEntity packetEntity) {
      EntityType type = packetEntity.getType();
      if (type == EntityTypes.PIGLIN || type == EntityTypes.ZOMBIFIED_PIGLIN || type == EntityTypes.ZOMBIE) {
         return packetEntity.isBaby ? -0.05 : -0.45;
      } else if (type == EntityTypes.SKELETON) {
         return -0.6;
      } else if (type == EntityTypes.ENDERMITE || type == EntityTypes.SILVERFISH) {
         return 0.1;
      } else if (type == EntityTypes.EVOKER
         || type == EntityTypes.ILLUSIONER
         || type == EntityTypes.PILLAGER
         || type == EntityTypes.RAVAGER
         || type == EntityTypes.VINDICATOR
         || type == EntityTypes.WITCH) {
         return -0.45;
      } else if (type == EntityTypes.PLAYER || type == EntityTypes.MANNEQUIN) {
         return -0.35;
      } else {
         return EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_ANIMAL) ? 0.14 : 0.0;
      }
   }

   public static double getPassengerRidingOffset(GrimPlayer player, PacketEntity packetEntity) {
      if (packetEntity instanceof PacketEntityHorse) {
         return getHeight(player, packetEntity) * 0.75 - 0.25;
      } else {
         EntityType type = packetEntity.getType();
         if (EntityTypes.isTypeInstanceOf(type, EntityTypes.MINECART_ABSTRACT)) {
            return 0.0;
         } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.BOAT)) {
            return -0.1;
         } else if (type == EntityTypes.HAPPY_GHAST) {
            return 0.5;
         } else if (type != EntityTypes.HOGLIN && type != EntityTypes.ZOGLIN) {
            if (type == EntityTypes.LLAMA) {
               return getHeight(player, packetEntity) * 0.67;
            } else if (type == EntityTypes.PIGLIN) {
               return getHeight(player, packetEntity) * 0.92;
            } else if (type == EntityTypes.RAVAGER) {
               return 2.1;
            } else if (type == EntityTypes.SKELETON) {
               return getHeight(player, packetEntity) * 0.75 - 0.1875;
            } else if (type == EntityTypes.SPIDER) {
               return getHeight(player, packetEntity) * 0.5;
            } else if (type == EntityTypes.STRIDER) {
               return packetEntity.isBaby && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)
                  ? 0.65625
                  : getHeight(player, packetEntity) - 0.19;
            } else {
               return getHeight(player, packetEntity) * 0.75;
            }
         } else {
            return packetEntity.isBaby && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)
               ? 0.875
               : getHeight(player, packetEntity) - (packetEntity.isBaby ? 0.2 : 0.15);
         }
      }
   }

   private static float getHeightMinusBaby(GrimPlayer player, PacketEntity packetEntity) {
      EntityType type = packetEntity.getType();
      if (type == EntityTypes.ARMADILLO) {
         return 0.65F;
      } else if (type == EntityTypes.AXOLOTL) {
         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby) {
            return 0.21F;
         } else {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) && packetEntity.isBaby ? 0.25F : 0.42F;
         }
      } else if (type == EntityTypes.BEE || type == EntityTypes.DOLPHIN || type == EntityTypes.ALLAY) {
         return 0.6F;
      } else if (type == EntityTypes.EVOKER_FANGS || type == EntityTypes.VEX) {
         return 0.8F;
      } else if (type == EntityTypes.SQUID || type == EntityTypes.GLOW_SQUID) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) && packetEntity.isBaby
            ? 0.63F
            : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.8F : 0.95F);
      } else if (type == EntityTypes.PARROT || type == EntityTypes.BAT || type == EntityTypes.PIG || type == EntityTypes.SPIDER) {
         return 0.9F;
      } else if (type == EntityTypes.WITHER_SKULL || type == EntityTypes.SHULKER_BULLET) {
         return 0.3125F;
      } else if (type == EntityTypes.BLAZE) {
         return 1.8F;
      } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.BOAT)) {
         return 0.5625F;
      } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_NAUTILUS)) {
         return 0.95F;
      } else if (type == EntityTypes.HAPPY_GHAST) {
         return 4.0F;
      } else if (type == EntityTypes.CAT) {
         return 0.7F;
      } else if (type == EntityTypes.CAVE_SPIDER) {
         return 0.5F;
      } else if (type == EntityTypes.FROG) {
         return 0.55F;
      } else if (type == EntityTypes.CHICKEN) {
         return packetEntity.isBaby && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) ? 0.4F : 0.7F;
      } else if (type == EntityTypes.HOGLIN || type == EntityTypes.ZOGLIN) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 0.85F : 1.4F;
      } else if (type == EntityTypes.COW) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 1.4F : 1.3F;
      } else if (type == EntityTypes.STRIDER) {
         return 1.7F;
      } else if (type == EntityTypes.CREEPER) {
         return 1.7F;
      } else if (type == EntityTypes.DONKEY) {
         return 1.5F;
      } else if (packetEntity instanceof PacketEntityGuardian guardian) {
         return guardian.isElder ? 1.9975F : 0.85F;
      } else if (type == EntityTypes.ENDERMAN || type == EntityTypes.WARDEN) {
         return 2.9F;
      } else if (type == EntityTypes.ENDERMITE || type == EntityTypes.COD) {
         return 0.3F;
      } else if (type == EntityTypes.END_CRYSTAL) {
         return 2.0F;
      } else if (type == EntityTypes.ENDER_DRAGON) {
         return 8.0F;
      } else if (type == EntityTypes.FIREBALL) {
         return 1.0F;
      } else if (type == EntityTypes.FOX) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 0.42F : 0.7F;
      } else if (type == EntityTypes.GHAST) {
         return 4.0F;
      } else if (type == EntityTypes.GIANT) {
         return 12.0F;
      } else if (type == EntityTypes.GUARDIAN) {
         return 0.85F;
      } else if (type == EntityTypes.HORSE) {
         return 1.6F;
      } else if (type == EntityTypes.IRON_GOLEM) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 2.7F : 2.9F;
      } else if (type == EntityTypes.CREAKING) {
         return 2.7F;
      } else if (type == EntityTypes.LLAMA || type == EntityTypes.TRADER_LLAMA) {
         return 1.87F;
      } else if (type == EntityTypes.TROPICAL_FISH) {
         return 0.4F;
      } else if (type == EntityTypes.SULFUR_CUBE && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)) {
         return packetEntity instanceof PacketEntitySizeable sizeable ? 0.49F * sizeable.size : 0.49F;
      } else if (type == EntityTypes.MAGMA_CUBE) {
         if (packetEntity instanceof PacketEntitySizeable sizeable) {
            float size = sizeable.size;
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)
               ? 0.52F * size
               : (
                  player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                     ? 2.04F * (0.255F * size)
                     : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 0.51000005F * size : 0.6F * size)
               );
         } else {
            return 0.7F;
         }
      } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.MINECART_ABSTRACT)) {
         return 0.7F;
      } else if (type == EntityTypes.MULE) {
         return 1.6F;
      } else if (type == EntityTypes.MOOSHROOM) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 1.4F : 1.3F;
      } else if (type == EntityTypes.OCELOT) {
         return 0.7F;
      } else if (type == EntityTypes.PANDA) {
         return 1.25F;
      } else if (type == EntityTypes.PHANTOM) {
         return packetEntity instanceof PacketEntitySizeable sizeable ? 0.5F + sizeable.size * 0.1F : 1.8F;
      } else if (type == EntityTypes.PLAYER || type == EntityTypes.MANNEQUIN) {
         return 1.8F;
      } else if (type == EntityTypes.POLAR_BEAR) {
         return 1.4F;
      } else if (type == EntityTypes.PUFFERFISH) {
         return 0.7F;
      } else if (type == EntityTypes.RABBIT) {
         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
            return packetEntity.isBaby ? 0.4F : 0.6F;
         } else {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.5F : 0.7F;
         }
      } else if (type == EntityTypes.RAVAGER) {
         return 2.2F;
      } else if (type == EntityTypes.SALMON) {
         return 0.4F;
      } else if (type != EntityTypes.SHEEP && type != EntityTypes.GOAT) {
         if (type == EntityTypes.SHULKER) {
            return 2.0F;
         } else if (type == EntityTypes.SILVERFISH) {
            return 0.3F;
         } else if (type == EntityTypes.SKELETON) {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 1.99F : 1.95F;
         } else if (type == EntityTypes.SKELETON_HORSE) {
            return 1.6F;
         } else if (type != EntityTypes.SLIME && (!player.getClientVersion().isOlderThan(ClientVersion.V_26_2) || type != EntityTypes.SULFUR_CUBE)) {
            if (type == EntityTypes.SMALL_FIREBALL) {
               return 0.3125F;
            } else if (type == EntityTypes.SNOW_GOLEM) {
               return 1.9F;
            } else if (type == EntityTypes.STRAY) {
               return 1.99F;
            } else if (type == EntityTypes.TURTLE) {
               return 0.4F;
            } else if (type == EntityTypes.WITHER) {
               return 3.5F;
            } else if (type == EntityTypes.WITHER_SKELETON) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 2.4F : 2.535F;
            } else if (type == EntityTypes.WOLF) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.85F : 0.8F;
            } else if (type == EntityTypes.ZOMBIE_HORSE) {
               return 1.6F;
            } else if (type == EntityTypes.TADPOLE) {
               return 0.3F;
            } else if (type == EntityTypes.SNIFFER) {
               return 1.75F;
            } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.CAMEL)) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 1.4F : 2.375F;
            } else if (type == EntityTypes.BREEZE) {
               return 1.77F;
            } else if (type == EntityTypes.BOGGED) {
               return 1.99F;
            } else if (type == EntityTypes.PARCHED) {
               return 1.99F;
            } else if (type == EntityTypes.WIND_CHARGE) {
               return 0.3125F;
            } else if (type == EntityTypes.ARMOR_STAND) {
               return 1.975F;
            } else if (type == EntityTypes.FALLING_BLOCK) {
               return 0.98F;
            } else if (type == EntityTypes.VILLAGER && player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
               return 1.8F;
            } else if (type == EntityTypes.FIREWORK_ROCKET) {
               return 0.25F;
            } else if (type == EntityTypes.COPPER_GOLEM) {
               return 1.0F;
            } else if ((
                  EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_PIGLIN)
                     || type == EntityTypes.DROWNED
                     || type == EntityTypes.HUSK
                     || type == EntityTypes.ZOMBIE
                     || type == EntityTypes.ZOMBIE_VILLAGER
                     || type == EntityTypes.VILLAGER
                     || type == EntityTypes.ZOMBIFIED_PIGLIN
               )
               && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)
               && packetEntity.isBaby) {
               return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) ? 0.98F : 0.99F;
            } else {
               return 1.95F;
            }
         } else if (packetEntity instanceof PacketEntitySizeable sizeable) {
            float size = sizeable.size;
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)
               ? 0.52F * size
               : (
                  player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                     ? 2.04F * (0.255F * size)
                     : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 0.51000005F * size : 0.6F * size)
               );
         } else {
            return 0.3125F;
         }
      } else {
         return type == EntityTypes.GOAT && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && packetEntity.isBaby ? 0.65F : 1.3F;
      }
   }

   private static float getBabyScaleFactor(GrimPlayer player, PacketEntity packetEntity) {
      EntityType type = packetEntity.getType();
      if (type == EntityTypes.TURTLE) {
         return 0.3F;
      } else if (type == EntityTypes.HAPPY_GHAST) {
         return 0.2375F;
      } else if (type == EntityTypes.DOLPHIN) {
         return 0.65F;
      } else if (type == EntityTypes.ARMADILLO) {
         return 0.6F;
      } else if (!player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)
         || type != EntityTypes.HOGLIN
            && type != EntityTypes.ZOGLIN
            && type != EntityTypes.FOX
            && type != EntityTypes.GOAT
            && !EntityTypes.isTypeInstanceOf(type, EntityTypes.CAMEL)) {
         if (type == EntityTypes.GOAT && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
            return 0.55F;
         } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.CAMEL)) {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1) ? 0.6F : 0.45F;
         } else if (EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_HORSE) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)) {
            return 0.7F;
         } else {
            return !player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_1)
                  || type != EntityTypes.CHICKEN
                     && type != EntityTypes.SQUID
                     && type != EntityTypes.GLOW_SQUID
                     && type != EntityTypes.AXOLOTL
                     && type != EntityTypes.RABBIT
                     && type != EntityTypes.ZOMBIE
                     && type != EntityTypes.DROWNED
                     && type != EntityTypes.HUSK
                     && type != EntityTypes.VILLAGER
                     && type != EntityTypes.ZOMBIE_VILLAGER
                     && type != EntityTypes.ZOMBIFIED_PIGLIN
                     && !EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_PIGLIN)
               ? 0.5F
               : 1.0F;
         }
      } else {
         return 1.0F;
      }
   }

   @Generated
   private BoundingBoxSize() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}

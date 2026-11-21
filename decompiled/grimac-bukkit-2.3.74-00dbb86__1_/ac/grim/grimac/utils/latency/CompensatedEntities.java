package ac.grim.grimac.utils.latency;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attribute;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.Equipment;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.UserProfile;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.Direction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import ac.grim.grimac.shaded.fastutil.ints.Int2ObjectOpenHashMap;
import ac.grim.grimac.shaded.fastutil.ints.IntArraySet;
import ac.grim.grimac.shaded.fastutil.objects.Object2ObjectOpenHashMap;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.ShulkerData;
import ac.grim.grimac.utils.data.TrackerData;
import ac.grim.grimac.utils.data.attribute.ValuedAttribute;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityArmorStand;
import ac.grim.grimac.utils.data.packetentity.PacketEntityCamel;
import ac.grim.grimac.utils.data.packetentity.PacketEntityGuardian;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHappyGhast;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHook;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHorse;
import ac.grim.grimac.utils.data.packetentity.PacketEntityNautilus;
import ac.grim.grimac.utils.data.packetentity.PacketEntityPainting;
import ac.grim.grimac.utils.data.packetentity.PacketEntityRideable;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySelf;
import ac.grim.grimac.utils.data.packetentity.PacketEntityShulker;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySizeable;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySkeleton;
import ac.grim.grimac.utils.data.packetentity.PacketEntityStrider;
import ac.grim.grimac.utils.data.packetentity.PacketEntityTrackXRot;
import ac.grim.grimac.utils.data.packetentity.PacketEntityUnHittable;
import ac.grim.grimac.utils.data.packetentity.dragon.PacketEntityEnderDragon;
import ac.grim.grimac.utils.nmsutil.BoundingBoxSize;
import ac.grim.grimac.utils.nmsutil.WatchableIndexUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.Map.Entry;

public class CompensatedEntities {
   public static final UUID SPRINTING_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
   public static final UUID SNOW_MODIFIER_UUID = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce");
   public final Int2ObjectOpenHashMap<PacketEntity> entityMap = new Int2ObjectOpenHashMap<>(40, 0.7F);
   public final IntArraySet entitiesRemovedThisTick = new IntArraySet();
   public final Int2ObjectOpenHashMap<TrackerData> serverPositionsMap = new Int2ObjectOpenHashMap<>(40, 0.7F);
   public final Object2ObjectOpenHashMap<UUID, UserProfile> profiles = new Object2ObjectOpenHashMap<>();
   public Integer serverPlayerVehicle = null;
   public boolean hasSprintingAttributeEnabled = false;
   public TrackerData selfTrackedEntity;
   public PacketEntitySelf self;
   private final GrimPlayer player;

   public CompensatedEntities(GrimPlayer player) {
      this.player = player;
      this.self = new PacketEntitySelf(player);
      this.selfTrackedEntity = new TrackerData(0.0, 0.0, 0.0, 0.0F, 0.0F, EntityTypes.PLAYER, player.lastTransactionSent.get());
   }

   public int getPacketEntityID(PacketEntity entity) {
      for (Entry<Integer, PacketEntity> entry : this.entityMap.int2ObjectEntrySet()) {
         if (entry.getValue() == entity) {
            return entry.getKey();
         }
      }

      return Integer.MIN_VALUE;
   }

   public void tick() {
      this.self.setPositionRaw(this.player, new SimpleCollisionBox(this.player.x, this.player.y, this.player.z, this.player.x, this.player.y, this.player.z));

      for (PacketEntity vehicle : this.entityMap.values()) {
         for (PacketEntity passenger : vehicle.passengers) {
            this.tickPassenger(vehicle, passenger);
         }
      }
   }

   public void removeEntity(int entityID) {
      PacketEntity entity = this.entityMap.remove(entityID);
      if (entity != null) {
         if (entity instanceof PacketEntityEnderDragon dragon) {
            for (int i = 1; i < dragon.getParts().size() + 1; i++) {
               this.entityMap.remove(entityID + i);
            }
         }

         for (PacketEntity passenger : new ArrayList<>(entity.passengers)) {
            passenger.eject();
         }
      }
   }

   public OptionalInt getSlowFallingAmplifier() {
      return this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2)
         ? OptionalInt.empty()
         : this.getPotionLevelForPlayer(PotionTypes.SLOW_FALLING);
   }

   public OptionalInt getPotionLevelForPlayer(PotionType type) {
      return this.getEntityInControl().getPotionEffectLevel(type);
   }

   public OptionalInt getPotionLevelForSelfPlayer(PotionType type) {
      return this.self.getPotionEffectLevel(type);
   }

   public boolean hasPotionEffect(PotionType type) {
      return this.getEntityInControl().hasPotionEffect(type);
   }

   public PacketEntity getEntityInControl() {
      return (PacketEntity)(this.self.getRiding() != null ? this.self.getRiding() : this.self);
   }

   public void updateAttributes(int entityID, List<WrapperPlayServerUpdateAttributes.Property> objects) {
      if (entityID == this.player.entityID) {
         for (WrapperPlayServerUpdateAttributes.Property snapshotWrapper : objects) {
            Attribute attribute = snapshotWrapper.getAttribute();
            if (attribute == Attributes.MOVEMENT_SPEED) {
               boolean found = false;

               for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : snapshotWrapper.getModifiers()) {
                  ResourceLocation name = modifier.getName();
                  if (name.getKey().equals(SPRINTING_MODIFIER_UUID.toString()) || name.getKey().equals("sprinting")) {
                     found = true;
                     break;
                  }
               }

               this.hasSprintingAttributeEnabled = found;
               break;
            }
         }
      }

      PacketEntity entity = this.player.compensatedEntities.getEntity(entityID);
      if (entity != null) {
         for (WrapperPlayServerUpdateAttributes.Property snapshotWrapperx : objects) {
            Attribute attribute = snapshotWrapperx.getAttribute();
            if (attribute != null) {
               if (attribute == Attributes.HORSE_JUMP_STRENGTH) {
                  attribute = Attributes.JUMP_STRENGTH;
               }

               Optional<ValuedAttribute> valuedAttribute = entity.getAttribute(attribute);
               if (!valuedAttribute.isEmpty()) {
                  valuedAttribute.get().with(snapshotWrapperx);
               }
            }
         }
      }
   }

   private void tickPassenger(PacketEntity riding, PacketEntity passenger) {
      if (riding != null && passenger != null) {
         passenger.setPositionRaw(
            this.player,
            riding.getPossibleLocationBoxes()
               .offset(0.0, BoundingBoxSize.getMyRidingOffset(riding) + BoundingBoxSize.getPassengerRidingOffset(this.player, passenger), 0.0)
         );

         for (PacketEntity passengerPassenger : riding.passengers) {
            this.tickPassenger(passenger, passengerPassenger);
         }
      }
   }

   public PacketEntity addEntity(int entityID, UUID uuid, EntityType entityType, Vector3d position, float xRot, int data) {
      if (entityType == EntityTypes.ITEM) {
         return null;
      } else {
         PacketEntity packetEntity;
         if (EntityTypes.isTypeInstanceOf(entityType, EntityTypes.ABSTRACT_NAUTILUS)) {
            packetEntity = new PacketEntityNautilus(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.HAPPY_GHAST.equals(entityType)) {
            packetEntity = new PacketEntityHappyGhast(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), xRot);
         } else if (EntityTypes.isTypeInstanceOf(entityType, EntityTypes.CAMEL)) {
            packetEntity = new PacketEntityCamel(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), xRot);
         } else if (EntityTypes.isTypeInstanceOf(entityType, EntityTypes.ABSTRACT_HORSE)) {
            packetEntity = new PacketEntityHorse(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), xRot);
         } else if (entityType == EntityTypes.SULFUR_CUBE) {
            packetEntity = new PacketEntitySizeable(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (entityType == EntityTypes.SLIME || entityType == EntityTypes.MAGMA_CUBE || entityType == EntityTypes.PHANTOM) {
            packetEntity = new PacketEntitySizeable(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.PIG.equals(entityType)) {
            packetEntity = new PacketEntityRideable(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.SHULKER.equals(entityType)) {
            packetEntity = new PacketEntityShulker(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.STRIDER.equals(entityType)) {
            packetEntity = new PacketEntityStrider(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.isTypeInstanceOf(entityType, EntityTypes.BOAT) || EntityTypes.CHICKEN.equals(entityType)) {
            packetEntity = new PacketEntityTrackXRot(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), xRot);
         } else if (EntityTypes.FISHING_BOBBER.equals(entityType)) {
            packetEntity = new PacketEntityHook(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), data);
         } else if (EntityTypes.ENDER_DRAGON.equals(entityType)) {
            packetEntity = new PacketEntityEnderDragon(this.player, uuid, entityID, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.isTypeInstanceOf(entityType, EntityTypes.ABSTRACT_ARROW)
            || EntityTypes.FIREWORK_ROCKET.equals(entityType)
            || EntityTypes.BLOCK_DISPLAY.equals(entityType)
            || EntityTypes.TEXT_DISPLAY.equals(entityType)
            || EntityTypes.LIGHTNING_BOLT.equals(entityType)
            || EntityTypes.EXPERIENCE_BOTTLE.equals(entityType)
            || EntityTypes.EXPERIENCE_ORB.equals(entityType)
            || EntityTypes.EVOKER_FANGS.equals(entityType)) {
            packetEntity = new PacketEntityUnHittable(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         } else if (EntityTypes.ARMOR_STAND.equals(entityType)) {
            packetEntity = new PacketEntityArmorStand(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ(), data);
         } else if (EntityTypes.PAINTING.equals(entityType)) {
            packetEntity = new PacketEntityPainting(this.player, uuid, position.x, position.y, position.z, Direction.values()[data]);
         } else if (EntityTypes.GUARDIAN.equals(entityType)) {
            packetEntity = new PacketEntityGuardian(this.player, uuid, entityType, position.x, position.y, position.z, false);
         } else if (EntityTypes.ELDER_GUARDIAN.equals(entityType)) {
            packetEntity = new PacketEntityGuardian(this.player, uuid, entityType, position.x, position.y, position.z, true);
         } else if (EntityTypes.SKELETON == entityType) {
            packetEntity = new PacketEntitySkeleton(this.player, uuid, entityType, position.x, position.y, position.z);
         } else {
            packetEntity = new PacketEntity(this.player, uuid, entityType, position.getX(), position.getY(), position.getZ());
         }

         this.entityMap.put(entityID, packetEntity);
         return packetEntity;
      }
   }

   public PacketEntity getEntity(int entityID) {
      return (PacketEntity)(entityID == this.player.entityID ? this.self : this.entityMap.get(entityID));
   }

   public TrackerData getTrackedEntity(int id) {
      return id == this.player.entityID ? this.selfTrackedEntity : this.serverPositionsMap.get(id);
   }

   public void updateEntityMetadata(int entityID, List<EntityData<?>> watchableObjects) {
      PacketEntity entity = this.player.compensatedEntities.getEntity(entityID);
      if (entity != null) {
         int ageableOffset = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_26_1) ? 1 : 0;
         if (entity.isAgeable) {
            int id;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
               id = 12;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               id = 11;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
               id = 12;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               id = 14;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               id = 15;
            } else {
               id = 16 + (EntityTypes.isTypeInstanceOf(entity.getType(), EntityTypes.ABSTRACT_PIGLIN) ? 1 : 0);
            }

            EntityData<?> ageableObject = WatchableIndexUtil.getIndex(watchableObjects, id);
            if (ageableObject != null) {
               Object value = ageableObject.getValue();
               if (value instanceof Boolean) {
                  entity.isBaby = (Boolean)value;
               } else if (value instanceof Byte) {
                  entity.isBaby = (Byte)value < 0;
               }
            }
         }

         if (entity instanceof PacketEntitySizeable sizeable) {
            int idx;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
               idx = 16;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               idx = 11;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
               idx = 12;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               idx = 14;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               idx = 15;
            } else {
               idx = 16;
            }

            EntityData<?> sizeObject = WatchableIndexUtil.getIndex(watchableObjects, idx);
            if (sizeObject != null) {
               Object value = sizeObject.getValue();
               if (value instanceof Integer) {
                  sizeable.size = (Integer)value;
               } else if (value instanceof Byte) {
                  sizeable.size = (Byte)value;
               }
            }
         }

         if (entity instanceof PacketEntityShulker shulker) {
            int idxx;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               idxx = 11;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
               idxx = 12;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               idxx = 14;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               idxx = 15;
            } else {
               idxx = 16;
            }

            EntityData<?> shulkerAttached = WatchableIndexUtil.getIndex(watchableObjects, idxx);
            if (shulkerAttached != null) {
               shulker.facing = BlockFace.valueOf(shulkerAttached.getValue().toString().toUpperCase());
            }

            int peekOffset = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17) ? 1 : 2;
            EntityData<?> height = WatchableIndexUtil.getIndex(watchableObjects, idxx + peekOffset);
            if (height != null) {
               ShulkerData data = new ShulkerData(shulker, this.player.lastTransactionSent.get(), (Byte)height.getValue() == 0);
               this.player.compensatedWorld.openShulkerBoxes.remove(data);
               this.player.compensatedWorld.openShulkerBoxes.add(data);
            }
         }

         if (entity instanceof PacketEntityRideable rideable) {
            int offset = 0;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
               if (entity.getType() == EntityTypes.PIG) {
                  EntityData<?> pigSaddle = WatchableIndexUtil.getIndex(watchableObjects, 16);
                  if (pigSaddle != null) {
                     rideable.hasSaddle = (Byte)pigSaddle.getValue() != 0;
                  }
               }
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               offset = 5;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
               offset = 4;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               offset = 2;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               offset = 1;
            }

            if (entity.getType() == EntityTypes.PIG) {
               if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
                  offset = 1;
               }

               if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_5)) {
                  EntityData<?> pigSaddle = WatchableIndexUtil.getIndex(watchableObjects, 17 - offset + ageableOffset);
                  if (pigSaddle != null) {
                     rideable.hasSaddle = (Boolean)pigSaddle.getValue();
                  }
               }

               EntityData<?> pigBoost = WatchableIndexUtil.getIndex(watchableObjects, 18 - offset + ageableOffset);
               if (pigBoost != null) {
                  rideable.boostTimeMax = (Integer)pigBoost.getValue();
                  rideable.currentBoostTime = 0;
               }
            } else if (entity instanceof PacketEntityStrider) {
               EntityData<?> striderBoost = WatchableIndexUtil.getIndex(watchableObjects, 17 - offset + ageableOffset);
               if (striderBoost != null) {
                  rideable.boostTimeMax = (Integer)striderBoost.getValue();
                  rideable.currentBoostTime = 0;
               }

               EntityData<?> striderShaking = WatchableIndexUtil.getIndex(watchableObjects, 18 - offset + ageableOffset);
               if (striderShaking != null) {
                  ((PacketEntityStrider)rideable).isShaking = (Boolean)striderShaking.getValue();
               }

               if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_5)) {
                  EntityData<?> striderSaddle = WatchableIndexUtil.getIndex(watchableObjects, 19 - offset + ageableOffset);
                  if (striderSaddle != null) {
                     rideable.hasSaddle = (Boolean)striderSaddle.getValue();
                  }
               }
            }
         }

         if (entity instanceof PacketEntityHorse horse) {
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9_4)) {
               int offsetx = 0;
               if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
                  offsetx = 5;
               } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
                  offsetx = 4;
               } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
                  offsetx = 2;
               } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                  offsetx = 1;
               }

               EntityData<?> horseByte = WatchableIndexUtil.getIndex(watchableObjects, 17 - offsetx + ageableOffset);
               if (horseByte != null) {
                  byte info = (Byte)horseByte.getValue();
                  horse.isTame = (info & 2) != 0;
                  horse.hasSaddle = (info & 4) != 0;
                  horse.isRearing = (info & 32) != 0;
               }

               if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20) && entity instanceof PacketEntityCamel camel
                  )
                {
                  EntityData<?> entityData = WatchableIndexUtil.getIndex(watchableObjects, 18 + ageableOffset);
                  if (entityData != null) {
                     camel.setDashing((Boolean)entityData.getValue());
                     camel.setDashCooldown(camel.getDashCooldown() == 0 ? 55 : camel.getDashCooldown());
                  }
               }
            } else {
               EntityData<?> horseBytex = WatchableIndexUtil.getIndex(watchableObjects, 16);
               if (horseBytex != null) {
                  int info = (Integer)horseBytex.getValue();
                  horse.isTame = (info & 2) != 0;
                  horse.hasSaddle = (info & 4) != 0;
                  horse.isRearing = (info & 64) != 0;
               }
            }
         }

         if (entity instanceof PacketEntityNautilus nautilus) {
            EntityData<?> entityData = WatchableIndexUtil.getIndex(watchableObjects, 19 + ageableOffset);
            if (entityData != null) {
               nautilus.setDashing((Boolean)entityData.getValue());
               nautilus.setDashCooldown(nautilus.getDashCooldown() == 0 ? 40 : nautilus.getDashCooldown());
            }
         }

         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9_4)) {
            EntityData<?> gravity = WatchableIndexUtil.getIndex(watchableObjects, 5);
            if (gravity != null) {
               Object gravityObject = gravity.getValue();
               if (gravityObject instanceof Boolean) {
                  entity.hasGravity = !(Boolean)gravityObject;
               }
            }
         }

         if (entity.getType() == EntityTypes.FIREWORK_ROCKET) {
            int offsetxx = 0;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
               offsetxx = 2;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               offsetxx = 1;
            }

            EntityData<?> fireworkWatchableObject = WatchableIndexUtil.getIndex(watchableObjects, 9 - offsetxx);
            if (fireworkWatchableObject == null) {
               return;
            }

            if (fireworkWatchableObject.getValue() instanceof Integer) {
               int attachedEntityID = (Integer)fireworkWatchableObject.getValue();
               if (attachedEntityID == this.player.entityID) {
                  this.player.fireworks.addNewFirework(entityID);
               }
            } else {
               Optional<Integer> attachedEntityID = (Optional<Integer>)fireworkWatchableObject.getValue();
               if (attachedEntityID.isPresent() && attachedEntityID.get().equals(this.player.entityID)) {
                  this.player.fireworks.addNewFirework(entityID);
               }
            }
         }

         if (entity instanceof PacketEntityHook hook) {
            int index;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               index = 5;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               index = 6;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               index = 7;
            } else {
               index = 8;
            }

            EntityData<?> hookWatchableObject = WatchableIndexUtil.getIndex(watchableObjects, index);
            if (hookWatchableObject == null) {
               return;
            }

            Integer attachedEntityID = (Integer)hookWatchableObject.getValue();
            hook.attached = attachedEntityID - 1;
         }

         if (entity instanceof PacketEntityArmorStand) {
            int indexx;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
               indexx = 10;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
               indexx = 11;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_14_4)) {
               indexx = 13;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
               indexx = 14;
            } else {
               indexx = 15;
            }

            EntityData<?> armorStandByte = WatchableIndexUtil.getIndex(watchableObjects, indexx);
            if (armorStandByte != null) {
               byte info = (Byte)armorStandByte.getValue();
               entity.isBaby = (info & 1) != 0;
               ((PacketEntityArmorStand)entity).isMarker = (info & 16) != 0;
            }
         }

         if (entity instanceof PacketEntityGuardian && PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_11)) {
            int indexxx;
            int isElderlyBitMask;
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_9)) {
               indexxx = 16;
               isElderlyBitMask = 4;
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_10)) {
               indexxx = 11;
               isElderlyBitMask = 4;
            } else {
               indexxx = 12;
               isElderlyBitMask = 4;
            }

            EntityData<?> guardianByte = WatchableIndexUtil.getIndex(watchableObjects, indexxx);
            if (guardianByte != null) {
               int info = (Integer)guardianByte.getValue();
               ((PacketEntityGuardian)entity).isElder = (info & isElderlyBitMask) != 0;
            }
         }

         if (entity instanceof PacketEntitySkeleton skeleton
            && PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
            EntityData<?> skeletonTypeObject = WatchableIndexUtil.getIndex(watchableObjects, 13);
            if (skeletonTypeObject != null && skeletonTypeObject.getValue() instanceof Byte skeletonType) {
               skeleton.isWitherSkeleton = skeletonType == 1;
            }
         }
      }
   }

   public void updateEntityEquipment(int entityId, List<Equipment> equipment) {
      PacketEntity entity = this.player.compensatedEntities.getEntity(entityId);
      if (entity != null && entity.trackEntityEquipment) {
         for (Equipment equipmentItem : equipment) {
            entity.setItemBySlot(equipmentItem.getSlot(), equipmentItem.getItem());
         }
      }
   }
}

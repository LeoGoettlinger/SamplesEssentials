package com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.serializer;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axrankmenu.libs.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axrankmenu.libs.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.data.GlobalPosition;
import com.artillexstudios.axrankmenu.libs.axapi.packet.data.HumanoidArm;
import com.artillexstudios.axrankmenu.libs.axapi.packet.data.PaintingVariant;
import com.artillexstudios.axrankmenu.libs.axapi.packet.data.VillagerData;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleData;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleTypes;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ClientAsset;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Direction;
import com.artillexstudios.axrankmenu.libs.axapi.utils.GameProfile;
import com.artillexstudios.axrankmenu.libs.axapi.utils.PlayerModelType;
import com.artillexstudios.axrankmenu.libs.axapi.utils.PlayerSkin;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Quaternion;
import com.artillexstudios.axrankmenu.libs.axapi.utils.ResolvableProfile;
import com.artillexstudios.axrankmenu.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Vector3f;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Version;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.position.BlockPosition;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Pose;
import org.bukkit.util.EulerAngle;

public final class EntityDataSerializers {
   private static final Int2ObjectArrayMap<EntityDataSerializer<?>> SERIALIZERS = new Int2ObjectArrayMap();
   private static final Object2IntArrayMap<EntityDataSerializer<?>> REVERSE_SERIALIZERS = new Object2IntArrayMap();
   public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<Byte>() {
      public void write(FriendlyByteBuf buf, Byte value) {
         buf.writeByte(value);
      }

      public Byte read(FriendlyByteBuf buf) {
         return buf.readByte();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.BYTE;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> INT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.INT);
   public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<Long>() {
      public void write(FriendlyByteBuf buf, Long value) {
         buf.writeVarLong(value);
      }

      public Long read(FriendlyByteBuf buf) {
         return buf.readVarLong();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.LONG;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<Float>() {
      public void write(FriendlyByteBuf buf, Float value) {
         buf.writeFloat(value);
      }

      public Float read(FriendlyByteBuf buf) {
         return buf.readFloat();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.FLOAT;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<String>() {
      public void write(FriendlyByteBuf buf, String value) {
         buf.writeUTF(value);
      }

      public String read(FriendlyByteBuf buf) {
         return buf.readUTF();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.STRING;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Component> COMPONENT = new EntityDataSerializer<Component>() {
      public void write(FriendlyByteBuf buf, Component value) {
         buf.writeComponent(value);
      }

      public Component read(FriendlyByteBuf buf) {
         return buf.readComponent();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.COMPONENT;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = new EntityDataSerializer<Optional<Component>>() {
      public void write(FriendlyByteBuf buf, Optional<Component> value) {
         if (value.isPresent()) {
            buf.writeBoolean(true);
            buf.writeComponent(value.get());
         } else {
            buf.writeBoolean(false);
         }
      }

      public Optional<Component> read(FriendlyByteBuf buf) {
         return buf.readBoolean() ? Optional.of(buf.readComponent()) : Optional.empty();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_COMPONENT;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<WrappedItemStack> ITEM_STACK = new EntityDataSerializer<WrappedItemStack>() {
      public void write(FriendlyByteBuf buf, WrappedItemStack value) {
         buf.writeItemStack(value);
      }

      public WrappedItemStack read(FriendlyByteBuf buf) {
         return buf.readItemStack();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.ITEM_STACK;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<BlockData> BLOCK_DATA = new EntityDataSerializer<BlockData>() {
      public void write(FriendlyByteBuf buf, BlockData value) {
         buf.writeBlockData(value);
      }

      public BlockData read(FriendlyByteBuf buf) {
         return buf.readBlockData();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.BLOCK_DATA;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<Boolean>() {
      public void write(FriendlyByteBuf buf, Boolean value) {
         buf.writeBoolean(value);
      }

      public Boolean read(FriendlyByteBuf buf) {
         return buf.readBoolean();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.BOOLEAN;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<ParticleData<? extends ParticleOption>> PARTICLE = new EntityDataSerializer<ParticleData<? extends ParticleOption>>() {
      public void write(FriendlyByteBuf buf, ParticleData<? extends ParticleOption> value) {
         ParticleTypes.write(UncheckedUtils.unsafeCast(value), buf);
      }

      public ParticleData<? extends ParticleOption> read(FriendlyByteBuf buf) {
         return ParticleTypes.read(buf);
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.PARTICLE;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<EulerAngle> ROTATIONS = new EntityDataSerializer<EulerAngle>() {
      public void write(FriendlyByteBuf buf, EulerAngle value) {
         buf.writeFloat((float)value.getX());
         buf.writeFloat((float)value.getY());
         buf.writeFloat((float)value.getZ());
      }

      public EulerAngle read(FriendlyByteBuf buf) {
         return new EulerAngle(buf.readFloat(), buf.readFloat(), buf.readFloat());
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.ROTATIONS;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<BlockPosition> BLOCK_POSITION = new EntityDataSerializer<BlockPosition>() {
      public void write(FriendlyByteBuf buf, BlockPosition value) {
         buf.writeBlockPos(value);
      }

      public BlockPosition read(FriendlyByteBuf buf) {
         return buf.readBlockPosition();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.LOCATION;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Optional<BlockPosition>> OPTIONAL_BLOCK_POSITION = new EntityDataSerializer<Optional<BlockPosition>>() {
      public void write(FriendlyByteBuf buf, Optional<BlockPosition> value) {
         if (value.isPresent()) {
            buf.writeBoolean(true);
            BlockPosition pos = value.get();
            buf.writeBlockPos(pos);
         } else {
            buf.writeBoolean(false);
         }
      }

      public Optional<BlockPosition> read(FriendlyByteBuf buf) {
         return !buf.readBoolean() ? Optional.empty() : Optional.of(buf.readBlockPosition());
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_LOCATION;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Pose> POSE = new EntityDataSerializer<Pose>() {
      public void write(FriendlyByteBuf buf, Pose value) {
         buf.writeVarInt(value.ordinal());
      }

      public Pose read(FriendlyByteBuf buf) {
         return Pose.values()[buf.readVarInt()];
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.POSE;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> CAT_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.CAT_VARIANT);
   public static final EntityDataSerializer<Integer> WOLF_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.WOLF_VARIANT);
   public static final EntityDataSerializer<Integer> FROG_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.FROG_VARIANT);
   public static final EntityDataSerializer<PaintingVariant> PAINTING_VARIANT = new EntityDataSerializer<PaintingVariant>() {
      public void write(FriendlyByteBuf buf, PaintingVariant value) {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21)) {
            if (value.direct()) {
               buf.writeVarInt(0);
               buf.writeVarInt(value.width());
               buf.writeVarInt(value.height());
               buf.writeResourceLocation(value.key());
            } else {
               buf.writeVarInt(value.index());
            }
         } else {
            buf.writeVarInt(value.index());
         }
      }

      public PaintingVariant read(FriendlyByteBuf buf) {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21)) {
            int varInt = buf.readVarInt();
            return varInt == 0
               ? new PaintingVariant(0, buf.readVarInt(), buf.readVarInt(), buf.readResourceLocation(), true)
               : new PaintingVariant(varInt, 0, 0, null, false);
         } else {
            return new PaintingVariant(buf.readVarInt(), 0, 0, null, false);
         }
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.PAINTING_VARIANT;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Vector3f> VECTOR3 = new EntityDataSerializer<Vector3f>() {
      public void write(FriendlyByteBuf buf, Vector3f value) {
         buf.writeFloat(value.x());
         buf.writeFloat(value.y());
         buf.writeFloat(value.z());
      }

      public Vector3f read(FriendlyByteBuf buf) {
         return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.VECTOR3;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Quaternion> QUATERNION = new EntityDataSerializer<Quaternion>() {
      public void write(FriendlyByteBuf buf, Quaternion value) {
         buf.writeFloat(value.x());
         buf.writeFloat(value.y());
         buf.writeFloat(value.z());
         buf.writeFloat(value.w());
      }

      public Quaternion read(FriendlyByteBuf buf) {
         return new Quaternion(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.QUATERNION;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> COPPER_GOLEM_STATE = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.COPPER_GOLEM_STATE
   );
   public static final EntityDataSerializer<Integer> WEATHERING_COPPER_STATE = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.WEATHERING_COPPER_STATE
   );
   public static final EntityDataSerializer<ResolvableProfile> RESOLVABLE_PROFILE = new EntityDataSerializer<ResolvableProfile>() {
      public void write(FriendlyByteBuf buf, ResolvableProfile value) {
         boolean partial = value.getPartialProfile().name() == null || value.getPartialProfile().uuid() == null;
         buf.writeBoolean(!partial);
         if (!partial) {
            buf.writeUUID(value.getPartialProfile().uuid());
            buf.writeUTF(value.getPartialProfile().name());
         } else {
            buf.writeBoolean(value.getPartialProfile().name() != null);
            if (value.getPartialProfile().name() != null) {
               buf.writeUTF(value.getPartialProfile().name());
            }

            buf.writeBoolean(value.getPartialProfile().uuid() != null);
            if (value.getPartialProfile().uuid() != null) {
               buf.writeUUID(value.getPartialProfile().uuid());
            }
         }

         buf.writeVarInt(value.getPartialProfile().properties().properties().size());

         for (Entry<String, ProfileProperties.Property> entry : value.getPartialProfile().properties().properties().entries()) {
            buf.writeUTF(entry.getKey());
            buf.writeUTF(entry.getValue().value());
            buf.writeBoolean(entry.getValue().signature() != null);
            if (entry.getValue().signature() != null) {
               buf.writeUTF(entry.getValue().signature());
            }
         }

         value.getPatch().body().ifPresentOrElse(texture -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(texture.id());
         }, () -> buf.writeBoolean(false));
         value.getPatch().cape().ifPresentOrElse(texture -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(texture.id());
         }, () -> buf.writeBoolean(false));
         value.getPatch().elytra().ifPresentOrElse(texture -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(texture.id());
         }, () -> buf.writeBoolean(false));
         value.getPatch().model().ifPresentOrElse(texture -> {
            buf.writeBoolean(true);
            buf.writeEnum(texture);
         }, () -> buf.writeBoolean(false));
      }

      public ResolvableProfile read(FriendlyByteBuf buf) {
         boolean gameProfile = buf.readBoolean();
         UUID uuid;
         String name;
         if (gameProfile) {
            uuid = buf.readUUID();
            name = buf.readUTF(16);
         } else {
            boolean namePresent = buf.readBoolean();
            name = namePresent ? buf.readUTF(16) : null;
            boolean uuidPresent = buf.readBoolean();
            uuid = uuidPresent ? buf.readUUID() : null;
         }

         ProfileProperties properties = new ProfileProperties(uuid, name);
         int count = buf.readVarInt();
         if (count > 16) {
            throw new RuntimeException("Reading illegal profile!");
         } else {
            for (int i = 0; i < count; i++) {
               String propertyName = buf.readUTF(64);
               String propertyValue = buf.readUTF(32767);
               String propertySignature = buf.readBoolean() ? buf.readUTF(1024) : null;
               properties.put(propertyName, new ProfileProperties.Property(propertyName, propertyValue, propertySignature));
            }

            boolean bodyPresent = buf.readBoolean();
            Optional<ClientAsset.ResourceTexture> body = bodyPresent
               ? Optional.of(new ClientAsset.ResourceTexture(buf.readResourceLocation()))
               : Optional.empty();
            boolean capePresent = buf.readBoolean();
            Optional<ClientAsset.ResourceTexture> cape = capePresent
               ? Optional.of(new ClientAsset.ResourceTexture(buf.readResourceLocation()))
               : Optional.empty();
            boolean elytraPresent = buf.readBoolean();
            Optional<ClientAsset.ResourceTexture> elytra = elytraPresent
               ? Optional.of(new ClientAsset.ResourceTexture(buf.readResourceLocation()))
               : Optional.empty();
            boolean modelTypePresent = buf.readBoolean();
            Optional<PlayerModelType> modelType = modelTypePresent ? Optional.of(buf.readEnum(PlayerModelType.class)) : Optional.empty();
            PlayerSkin.Patch patch = new PlayerSkin.Patch(body, cape, elytra, modelType);
            GameProfile builtProfile = new GameProfile(name, uuid, properties);
            return new ResolvableProfile(builtProfile, patch);
         }
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.RESOLVABLE_PROFILE;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<Optional<UUID>>() {
      public void write(FriendlyByteBuf buf, Optional<UUID> value) {
         if (value.isPresent()) {
            buf.writeBoolean(true);
            UUID uuid = value.get();
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
         } else {
            buf.writeBoolean(false);
         }
      }

      public Optional<UUID> read(FriendlyByteBuf buf) {
         return buf.readBoolean() ? Optional.of(new UUID(buf.readLong(), buf.readLong())) : Optional.empty();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_UUID;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>() {
      public void write(FriendlyByteBuf buf, CompoundTag value) {
         buf.writeNBT(value);
      }

      public CompoundTag read(FriendlyByteBuf buf) {
         return buf.readNBT();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.COMPOUND_TAG;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> SNIFFER_STATE = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.SNIFFER_STATE);
   public static final EntityDataSerializer<Integer> ARMADILLO_STATE = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.ARMADILLO_STATE);
   public static final EntityDataSerializer<List<ParticleData<? extends ParticleOption>>> PARTICLES = new EntityDataSerializer<List<ParticleData<? extends ParticleOption>>>() {
      public void write(FriendlyByteBuf buf, List<ParticleData<? extends ParticleOption>> value) {
         buf.writeVarInt(value.size());

         for (ParticleData<? extends ParticleOption> option : value) {
            EntityDataSerializers.PARTICLE.write(buf, option);
         }
      }

      public List<ParticleData<? extends ParticleOption>> read(FriendlyByteBuf buf) {
         int size = buf.readVarInt();
         List<ParticleData<? extends ParticleOption>> options = new ArrayList<>();

         for (int i = 0; i < size; i++) {
            options.add(EntityDataSerializers.PARTICLE.read(buf));
         }

         return options;
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.PARTICLES;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Direction> DIRECTION = new EntityDataSerializer<Direction>() {
      public void write(FriendlyByteBuf buf, Direction value) {
         buf.writeVarInt(value.ordinal());
      }

      public Direction read(FriendlyByteBuf buf) {
         return Direction.values()[buf.readVarInt()];
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.DIRECTION;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer<VillagerData>() {
      public void write(FriendlyByteBuf buf, VillagerData value) {
         buf.writeVarInt(value.getType());
         buf.writeVarInt(value.getProfession());
         buf.writeVarInt(value.getLevel());
      }

      public VillagerData read(FriendlyByteBuf buf) {
         return new VillagerData(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.VILLAGER_DATA;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer<OptionalInt>() {
      public void write(FriendlyByteBuf buf, OptionalInt value) {
         buf.writeVarInt(value.orElse(-1) + 1);
      }

      public OptionalInt read(FriendlyByteBuf buf) {
         int i = buf.readVarInt();
         return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_UNSIGNED_INT;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Optional<GlobalPosition>> OPTIONAL_GLOBAL_POS = new EntityDataSerializer<Optional<GlobalPosition>>() {
      public void write(FriendlyByteBuf buf, Optional<GlobalPosition> value) {
         if (value.isPresent()) {
            buf.writeBoolean(true);
            GlobalPosition pos = value.get();
            buf.writeResourceLocation(pos.key());
            buf.writeBlockPos(pos.position());
         } else {
            buf.writeBoolean(false);
         }
      }

      public Optional<GlobalPosition> read(FriendlyByteBuf buf) {
         return buf.readBoolean() ? Optional.of(new GlobalPosition(buf.readResourceLocation(), buf.readBlockPosition())) : Optional.empty();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_GLOBAL_POS;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<OptionalInt> OPTIONAL_BLOCK_DATA = new EntityDataSerializer<OptionalInt>() {
      public void write(FriendlyByteBuf buf, OptionalInt value) {
         if (value.isPresent()) {
            buf.writeVarInt(value.getAsInt());
         } else {
            buf.writeVarInt(0);
         }
      }

      public OptionalInt read(FriendlyByteBuf buf) {
         int i = buf.readVarInt();
         return i != 0 ? OptionalInt.of(i) : OptionalInt.empty();
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.OPTIONAL_BLOCK_DATA;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> COW_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.COW_VARIANT);
   public static final EntityDataSerializer<Integer> CHICKEN_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.CHICKEN_VARIANT);
   public static final EntityDataSerializer<Integer> WOLF_SOUND_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.WOLF_SOUND_VARIANT
   );
   public static final EntityDataSerializer<Integer> PIG_VARIANT = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.PIG_VARIANT);
   public static final EntityDataSerializer<Integer> ZOMBIE_NAUTILUS_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.ZOMBIE_NAUTILUS_VARIANT
   );
   public static final EntityDataSerializer<Integer> CAT_SOUND_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.CAT_SOUND_VARIANT
   );
   public static final EntityDataSerializer<Integer> CHICKEN_SOUND_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.CHICKEN_SOUND_VARIANT
   );
   public static final EntityDataSerializer<Integer> COW_SOUND_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.COW_SOUND_VARIANT
   );
   public static final EntityDataSerializer<Integer> PIG_SOUND_VARIANT = new EntityDataSerializers.VarIntSerializer(
      EntityDataSerializers.Type.PIG_SOUND_VARIANT
   );
   public static final EntityDataSerializer<HumanoidArm> HUMANOID_ARM = new EntityDataSerializer<HumanoidArm>() {
      public void write(FriendlyByteBuf buf, HumanoidArm value) {
         buf.writeEnum(value);
      }

      public HumanoidArm read(FriendlyByteBuf buf) {
         return buf.readEnum(HumanoidArm.class);
      }

      @Override
      public EntityDataSerializers.Type type() {
         return EntityDataSerializers.Type.HUMANOID_ARM;
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   };
   public static final EntityDataSerializer<Integer> DYE_COLOR = new EntityDataSerializers.VarIntSerializer(EntityDataSerializers.Type.DYE_COLOR);

   public static <T> EntityDataSerializer<T> byId(int id) {
      return UncheckedUtils.unsafeCast(SERIALIZERS.get(id));
   }

   public static <T> int getId(EntityDataSerializer<T> serializer) {
      return REVERSE_SERIALIZERS.getInt(serializer);
   }

   private static <T> void register(EntityDataSerializer<T> serializer) {
      int index = SERIALIZERS.size();
      SERIALIZERS.put(index, serializer);
      REVERSE_SERIALIZERS.put(serializer, index);
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Registering entitydata serializer: {}", serializer.type());
      }
   }

   static {
      register(BYTE);
      register(INT);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19_2)) {
         register(LONG);
      }

      register(FLOAT);
      register(STRING);
      register(COMPONENT);
      register(OPTIONAL_COMPONENT);
      register(ITEM_STACK);
      register(BOOLEAN);
      register(ROTATIONS);
      register(BLOCK_POSITION);
      register(OPTIONAL_BLOCK_POSITION);
      register(DIRECTION);
      register(OPTIONAL_UUID);
      register(BLOCK_DATA);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19_3)) {
         register(OPTIONAL_BLOCK_DATA);
      }

      if (Version.getServerVersion().isOlderThanOrEqualTo(Version.v1_21_6)) {
         register(COMPOUND_TAG);
      }

      register(PARTICLE);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
         register(PARTICLES);
      }

      register(VILLAGER_DATA);
      register(OPTIONAL_UNSIGNED_INT);
      register(POSE);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
         register(CAT_VARIANT);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
            register(CAT_SOUND_VARIANT);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
            register(COW_VARIANT);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
            register(COW_SOUND_VARIANT);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
            register(WOLF_VARIANT);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
            register(WOLF_SOUND_VARIANT);
         }

         register(FROG_VARIANT);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
            register(PIG_VARIANT);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
               register(PIG_SOUND_VARIANT);
            }

            register(CHICKEN_VARIANT);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
               register(CHICKEN_SOUND_VARIANT);
            }

            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_8)) {
               register(ZOMBIE_NAUTILUS_VARIANT);
            }
         }

         register(OPTIONAL_GLOBAL_POS);
         register(PAINTING_VARIANT);
         register(SNIFFER_STATE);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
            register(ARMADILLO_STATE);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)) {
               register(COPPER_GOLEM_STATE);
               register(WEATHERING_COPPER_STATE);
            }
         }

         register(VECTOR3);
         register(QUATERNION);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)) {
            register(RESOLVABLE_PROFILE);
         }

         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_8)) {
            register(HUMANOID_ARM);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_3)) {
               register(DYE_COLOR);
            }
         }
      }
   }

   public static enum Type {
      BYTE,
      INT,
      LONG,
      FLOAT,
      STRING,
      COMPONENT,
      OPTIONAL_COMPONENT,
      ITEM_STACK,
      BLOCK_DATA,
      BOOLEAN,
      PARTICLE,
      ROTATIONS,
      LOCATION,
      OPTIONAL_LOCATION,
      POSE,
      VECTOR3,
      QUATERNION,
      PARTICLES,
      ARMADILLO_STATE,
      SNIFFER_STATE,
      COPPER_GOLEM_STATE,
      WEATHERING_COPPER_STATE,
      COMPOUND_TAG,
      OPTIONAL_UUID,
      WOLF_VARIANT,
      CAT_VARIANT,
      DIRECTION,
      VILLAGER_DATA,
      OPTIONAL_UNSIGNED_INT,
      OPTIONAL_GLOBAL_POS,
      OPTIONAL_BLOCK_DATA,
      COW_VARIANT,
      WOLF_SOUND_VARIANT,
      PIG_VARIANT,
      CHICKEN_VARIANT,
      RESOLVABLE_PROFILE,
      HUMANOID_ARM,
      ZOMBIE_NAUTILUS_VARIANT,
      CAT_SOUND_VARIANT,
      CHICKEN_SOUND_VARIANT,
      COW_SOUND_VARIANT,
      PIG_SOUND_VARIANT,
      FROG_VARIANT,
      PAINTING_VARIANT,
      DYE_COLOR;
   }

   public record VarIntSerializer(EntityDataSerializers.Type type) implements EntityDataSerializer<Integer> {
      public void write(FriendlyByteBuf buf, Integer value) {
         buf.writeVarInt(value);
      }

      public Integer read(FriendlyByteBuf buf) {
         return buf.readVarInt();
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof EntityDataSerializer<?> that ? Objects.equals(this.type(), that.type()) : false;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.type());
      }
   }
}

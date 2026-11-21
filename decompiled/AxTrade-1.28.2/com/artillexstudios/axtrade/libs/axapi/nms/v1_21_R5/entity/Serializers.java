package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.entity;

import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.packet.FriendlyByteBufWrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R5.packet.PacketTransformer;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.serializer.EntityDataSerializer;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.serializer.EntityDataSerializers;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleData;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleTypes;
import com.artillexstudios.axtrade.libs.axapi.utils.ComponentSerializer;
import com.artillexstudios.axtrade.libs.axapi.utils.Quaternion;
import com.artillexstudios.axtrade.libs.axapi.utils.ResolvableProfile;
import java.util.EnumMap;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Vector3f;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R5.block.data.CraftBlockData;
import org.bukkit.entity.Pose;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;

public class Serializers {
   private static final EnumMap<EntityDataSerializers.Type, Serializers.Transformer<?>> typeTransformers = new EnumMap<>(EntityDataSerializers.Type.class);

   public static <T, Z> Serializers.Transformer<Z> transformer(EntityDataSerializer<T> accessor) {
      return (Serializers.Transformer<Z>)typeTransformers.get(accessor.type());
   }

   static {
      typeTransformers.put(EntityDataSerializers.Type.BYTE, new Serializers.Transformer<Byte>() {
         public Byte transform(Object other) {
            return (Byte)other;
         }

         @Override
         public DataWatcherSerializer<Byte> serializer() {
            return DataWatcherRegistry.a;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.INT, new Serializers.Transformer<Integer>() {
         public Integer transform(Object other) {
            return (Integer)other;
         }

         @Override
         public DataWatcherSerializer<Integer> serializer() {
            return DataWatcherRegistry.b;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.FLOAT, new Serializers.Transformer<Float>() {
         public Float transform(Object other) {
            return (Float)other;
         }

         @Override
         public DataWatcherSerializer<Float> serializer() {
            return DataWatcherRegistry.d;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.STRING, new Serializers.Transformer<String>() {
         public String transform(Object other) {
            return (String)other;
         }

         @Override
         public DataWatcherSerializer<String> serializer() {
            return DataWatcherRegistry.e;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.COMPONENT, new Serializers.Transformer<IChatBaseComponent>() {
         public IChatBaseComponent transform(Object other) {
            return ComponentSerializer.INSTANCE.toVanilla((Component)other);
         }

         @Override
         public DataWatcherSerializer<IChatBaseComponent> serializer() {
            return DataWatcherRegistry.f;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.OPTIONAL_COMPONENT, new Serializers.Transformer<Optional<IChatBaseComponent>>() {
         public Optional<IChatBaseComponent> transform(Object other) {
            return ((Optional)other).map(ComponentSerializer.INSTANCE::toVanilla);
         }

         @Override
         public DataWatcherSerializer<Optional<IChatBaseComponent>> serializer() {
            return DataWatcherRegistry.g;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.ITEM_STACK, new Serializers.Transformer<ItemStack>() {
         public ItemStack transform(Object other) {
            return ((WrappedItemStack)other).itemStack;
         }

         @Override
         public DataWatcherSerializer<ItemStack> serializer() {
            return DataWatcherRegistry.h;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.BOOLEAN, new Serializers.Transformer<Boolean>() {
         public Boolean transform(Object other) {
            return (Boolean)other;
         }

         @Override
         public DataWatcherSerializer<Boolean> serializer() {
            return DataWatcherRegistry.k;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.PARTICLE, new Serializers.Transformer<ParticleParam>() {
         public ParticleParam transform(Object other) {
            ParticleData<ParticleOption> data = (ParticleData<ParticleOption>)other;
            FriendlyByteBufWrapper wrapper = (FriendlyByteBufWrapper)PacketTransformer.newByteBuf();
            ParticleTypes.write(data, wrapper);
            return (ParticleParam)Particles.bl.decode(wrapper.buf());
         }

         @Override
         public DataWatcherSerializer<ParticleParam> serializer() {
            return DataWatcherRegistry.l;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.ROTATIONS, new Serializers.Transformer<Vector3f>() {
         private static Vector3f toRotations(EulerAngle eulerAngle) {
            return new Vector3f((float)Math.toDegrees(eulerAngle.getX()), (float)Math.toDegrees(eulerAngle.getY()), (float)Math.toDegrees(eulerAngle.getZ()));
         }

         public Vector3f transform(Object other) {
            return toRotations((EulerAngle)other);
         }

         @Override
         public DataWatcherSerializer<Vector3f> serializer() {
            return DataWatcherRegistry.n;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.POSE, new Serializers.Transformer<EntityPose>() {
         private static final EntityPose[] poses = EntityPose.values();

         public EntityPose transform(Object other) {
            return poses[((Pose)other).ordinal()];
         }

         @Override
         public DataWatcherSerializer<EntityPose> serializer() {
            return DataWatcherRegistry.w;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.VECTOR3, new Serializers.Transformer<org.joml.Vector3f>() {
         public org.joml.Vector3f transform(Object other) {
            com.artillexstudios.axtrade.libs.axapi.utils.Vector3f vector3f = (com.artillexstudios.axtrade.libs.axapi.utils.Vector3f)other;
            return new org.joml.Vector3f(vector3f.x(), vector3f.y(), vector3f.z());
         }

         @Override
         public DataWatcherSerializer<org.joml.Vector3f> serializer() {
            return DataWatcherRegistry.H;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.QUATERNION, new Serializers.Transformer<Quaternionf>() {
         public Quaternionf transform(Object other) {
            Quaternion quaternion = (Quaternion)other;
            return new Quaternionf(quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
         }

         @Override
         public DataWatcherSerializer<Quaternionf> serializer() {
            return DataWatcherRegistry.I;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.BLOCK_DATA, new Serializers.Transformer<IBlockData>() {
         public IBlockData transform(Object other) {
            BlockData data = (BlockData)other;
            return ((CraftBlockData)data).getState();
         }

         @Override
         public DataWatcherSerializer<IBlockData> serializer() {
            return DataWatcherRegistry.i;
         }
      });
      typeTransformers.put(EntityDataSerializers.Type.RESOLVABLE_PROFILE, new Serializers.Transformer<ResolvableProfile>() {
         public ResolvableProfile transform(Object other) {
            throw new UnsupportedOperationException();
         }

         @Override
         public DataWatcherSerializer<ResolvableProfile> serializer() {
            throw new UnsupportedOperationException();
         }
      });
   }

   public interface Transformer<Z> {
      Z transform(Object other);

      DataWatcherSerializer<Z> serializer();
   }
}

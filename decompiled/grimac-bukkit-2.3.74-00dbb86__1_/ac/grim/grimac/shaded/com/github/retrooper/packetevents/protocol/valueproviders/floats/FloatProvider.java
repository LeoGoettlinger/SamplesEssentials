package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.valueproviders.floats;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Random;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FloatProvider {
   NbtMapCodec<FloatProvider> MAP_CODEC = new NbtMapCodec<FloatProvider>() {
      public FloatProvider decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         FloatProviderType<?> type = tag.getOrThrow("type", FloatProviderType.CODEC, wrapper);
         return (FloatProvider)type.getCodec().decode(tag, wrapper);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, FloatProvider value) throws NbtCodecException {
         FloatProviderType<FloatProvider> unsafeType = (FloatProviderType<FloatProvider>)value.getType();
         unsafeType.getCodec().encode(tag, wrapper, value);
         tag.set("type", value.getType(), FloatProviderType.CODEC, wrapper);
      }
   };
   NbtCodec<FloatProvider> CODEC = new NbtCodec<FloatProvider>() {
      private final NbtCodec<FloatProvider> delegate = FloatProvider.MAP_CODEC.codec();

      public FloatProvider decode(NBT tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         return (FloatProvider)(tag instanceof NBTNumber ? new ConstantFloat(((NBTNumber)tag).getAsFloat()) : this.delegate.decode(tag, wrapper));
      }

      public NBT encode(PacketWrapper<?> wrapper, FloatProvider value) throws NbtCodecException {
         return (NBT)(value instanceof ConstantFloat ? new NBTFloat(((ConstantFloat)value).getValue()) : this.delegate.encode(wrapper, value));
      }
   };

   FloatProviderType<?> getType();

   float getSample(Random random);

   float getMin();

   float getMax();
}

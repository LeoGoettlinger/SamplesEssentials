package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.sulfurcube;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SulfurCubeExplosionData {
   public static final NbtCodec<SulfurCubeExplosionData> CODEC = (new NbtMapCodec<SulfurCubeExplosionData>() {
      public SulfurCubeExplosionData decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         int power = tag.getNumberTagValueOrThrow("power").intValue();
         boolean causesFire = tag.getBooleanOrThrow("causes_fire");
         int fuse = tag.getNumberTagValueOrThrow("fuse").intValue();
         return new SulfurCubeExplosionData(power, causesFire, fuse);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, SulfurCubeExplosionData value) throws NbtCodecException {
         tag.setTag("power", new NBTInt(value.power));
         tag.setTag("causes_fire", new NBTByte(value.causesFire));
         tag.setTag("fuse", new NBTInt(value.fuse));
      }
   }).codec();
   private final int power;
   private final boolean causesFire;
   private final int fuse;

   public SulfurCubeExplosionData(int power, boolean causesFire, int fuse) {
      this.power = power;
      this.causesFire = causesFire;
      this.fuse = fuse;
   }

   public int getPower() {
      return this.power;
   }

   public boolean isCausesFire() {
      return this.causesFire;
   }

   public int getFuse() {
      return this.fuse;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         SulfurCubeExplosionData that = (SulfurCubeExplosionData)obj;
         if (this.power != that.power) {
            return false;
         } else {
            return this.causesFire != that.causesFire ? false : this.fuse == that.fuse;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.power, this.causesFire, this.fuse);
   }
}

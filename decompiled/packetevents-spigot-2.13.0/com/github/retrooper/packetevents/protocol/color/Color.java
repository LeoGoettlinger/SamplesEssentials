package com.github.retrooper.packetevents.protocol.color;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Arrays;
import java.util.Objects;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Color implements RGBLike {
   private static final NbtCodec<Color> INT_CODEC = NbtCodecs.INT.apply(Color::new, Color::asRGB);
   private static final NbtCodec<Color> FLOATS_CODEC = NbtCodecs.FLOAT
      .applyFixedList(3)
      .apply(
         floats -> new Color(floats.get(0), floats.get(1), floats.get(2)),
         color -> Arrays.asList(color.red / 255.0F, color.green / 255.0F, color.blue / 255.0F)
      );
   public static final NbtCodec<Color> CODEC = new NbtCodec<Color>() {
      private final NbtCodec<Color> decodeCodec = Color.INT_CODEC.withAlternative(Color.FLOATS_CODEC);

      public Color decode(NBT tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         return this.decodeCodec.decode(tag, wrapper);
      }

      public NBT encode(PacketWrapper<?> wrapper, Color value) throws NbtCodecException {
         return wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)
            ? Color.INT_CODEC.encode(wrapper, value)
            : Color.FLOATS_CODEC.encode(wrapper, value);
      }
   };
   public static final Color WHITE = new Color(-1);
   public static final Color BLACK = new Color(-16777216);
   protected static final int BIT_MASK = 255;
   protected final int red;
   protected final int green;
   protected final int blue;

   public Color(@Range(from = 0L,to = 255L) int red, @Range(from = 0L,to = 255L) int green, @Range(from = 0L,to = 255L) int blue) {
      this.red = MathUtil.clamp(red, 0, 255);
      this.green = MathUtil.clamp(green, 0, 255);
      this.blue = MathUtil.clamp(blue, 0, 255);
   }

   public Color(@Range(from = 0L,to = 1L) float red, @Range(from = 0L,to = 1L) float green, @Range(from = 0L,to = 1L) float blue) {
      this(MathUtil.floor(red * 255.0F), MathUtil.floor(green * 255.0F), MathUtil.floor(blue * 255.0F));
   }

   public Color(int rgb) {
      this(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
   }

   public static Color read(PacketWrapper<?> wrapper) {
      return new Color(wrapper.readInt());
   }

   public static void write(PacketWrapper<?> wrapper, Color color) {
      wrapper.writeInt(color.asRGB());
   }

   public static Color readShort(PacketWrapper<?> wrapper) {
      return new Color(wrapper.readUnsignedByte(), wrapper.readUnsignedByte(), wrapper.readUnsignedByte());
   }

   public static void writeShort(PacketWrapper<?> wrapper, Color color) {
      wrapper.writeByte(color.red);
      wrapper.writeByte(color.green);
      wrapper.writeByte(color.blue);
   }

   @Deprecated
   public static Color decode(NBT nbt, PacketWrapper<?> wrapper) {
      return CODEC.decode(nbt, wrapper);
   }

   @Deprecated
   public static Color decode(NBT nbt, ClientVersion version) {
      return decode(nbt, PacketWrapper.createDummyWrapper(version));
   }

   @Deprecated
   public static NBT encode(PacketWrapper<?> wrapper, Color color) {
      return CODEC.encode(wrapper, color);
   }

   @Deprecated
   public static NBT encode(Color color, ClientVersion version) {
      return encode(PacketWrapper.createDummyWrapper(version), color);
   }

   public AlphaColor withAlpha() {
      return this.withAlpha(255);
   }

   public AlphaColor withAlpha(@Range(from = 0L,to = 255L) int alpha) {
      return new AlphaColor(alpha, this.red, this.green, this.blue);
   }

   public Color withRed(@Range(from = 0L,to = 255L) int red) {
      return new Color(red, this.green, this.blue);
   }

   public Color withGreen(@Range(from = 0L,to = 255L) int green) {
      return new Color(this.red, green, this.blue);
   }

   public Color withBlue(@Range(from = 0L,to = 255L) int blue) {
      return new Color(this.red, this.green, blue);
   }

   public int asRGB() {
      return this.red << 16 | this.green << 8 | this.blue;
   }

   public Color plus(Color other) {
      return new Color(this.red + other.red, this.green + other.green, this.blue + other.blue);
   }

   public Color minus(Color other) {
      return new Color(this.red - other.red, this.green - other.green, this.blue - other.blue);
   }

   public Color times(Color other) {
      if (other instanceof AlphaColor) {
         return other.times(this);
      } else if (this.red == 255 && this.green == 255 && this.blue == 255) {
         return other;
      } else {
         return other.red == 255 && other.green == 255 && other.blue == 255
            ? this
            : new Color(this.red * other.red / 255, this.green * other.green / 255, this.blue * other.blue / 255);
      }
   }

   public AlphaColor blendWith(AlphaColor source) {
      int srcAlpha = source.alpha();
      if (srcAlpha == 255) {
         return source;
      } else if (srcAlpha == 0) {
         return this.withAlpha();
      } else {
         int alpha = srcAlpha + (255 - srcAlpha);
         return new AlphaColor(
            alpha,
            alphaBlendChannel(alpha, srcAlpha, this.red, source.red),
            alphaBlendChannel(alpha, srcAlpha, this.green, source.green),
            alphaBlendChannel(alpha, srcAlpha, this.blue, source.blue)
         );
      }
   }

   protected static int alphaBlendChannel(int alpha, int srcAlpha, int dest, int src) {
      return (src * srcAlpha + dest * (alpha - srcAlpha)) / alpha;
   }

   public Color asGrayscale() {
      int grayscale = (int)(this.red * 0.3F + this.green * 0.59F + this.blue * 0.11F);
      return new Color(grayscale, grayscale, grayscale);
   }

   public Color scale(float scale) {
      return this.scale(scale, scale, scale);
   }

   public Color scale(float redScale, float greenScale, float blueScale) {
      return new Color((int)(this.red * redScale), (int)(this.green * greenScale), (int)(this.blue * blueScale));
   }

   public Color lerpSrgb(Color dest, float t) {
      return new Color(MathUtil.lerp(t, this.red, dest.red), MathUtil.lerp(t, this.green, dest.green), MathUtil.lerp(t, this.blue, dest.blue));
   }

   @Range(
      from = 0L,
      to = 255L
   )
   public int alpha() {
      return 255;
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   public int red() {
      return this.red;
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   public int green() {
      return this.green;
   }

   @Range(
      from = 0L,
      to = 255L
   )
   @Override
   public int blue() {
      return this.blue;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Color ? this.asRGB() == ((Color)obj).asRGB() : false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.asRGB());
   }
}

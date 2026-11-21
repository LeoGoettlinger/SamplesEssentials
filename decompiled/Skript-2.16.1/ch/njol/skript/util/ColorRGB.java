package ch.njol.skript.util;

import ch.njol.skript.variables.Variables;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.Fields;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class ColorRGB implements Color {
   private static final Pattern RGB_PATTERN = Pattern.compile("(?>rgb|RGB) (\\d+), (\\d+), (\\d+)");
   private org.bukkit.Color bukkit;
   @Nullable
   private DyeColor dye;

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Internal
   public ColorRGB(int red, int green, int blue) {
      this(org.bukkit.Color.fromRGB(Math2.fit(0, red, 255), Math2.fit(0, green, 255), Math2.fit(0, blue, 255)));
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Internal
   public ColorRGB(org.bukkit.Color bukkit) {
      this.dye = DyeColor.getByColor(bukkit);
      this.bukkit = bukkit;
   }

   @org.jetbrains.annotations.Contract("_,_,_,_ -> new")
   @NotNull
   public static ColorRGB fromRGBA(int red, int green, int blue, int alpha) {
      return new ColorRGB(org.bukkit.Color.fromARGB(alpha, red, green, blue));
   }

   @org.jetbrains.annotations.Contract("_,_,_ -> new")
   @NotNull
   public static ColorRGB fromRGB(int red, int green, int blue) {
      return new ColorRGB(red, green, blue);
   }

   @org.jetbrains.annotations.Contract("_ -> new")
   @NotNull
   public static ColorRGB fromBukkitColor(org.bukkit.Color bukkit) {
      return new ColorRGB(bukkit);
   }

   @Override
   public int getAlpha() {
      return this.bukkit.getAlpha();
   }

   @Override
   public int getRed() {
      return this.bukkit.getRed();
   }

   @Override
   public int getGreen() {
      return this.bukkit.getGreen();
   }

   @Override
   public int getBlue() {
      return this.bukkit.getBlue();
   }

   @Override
   public org.bukkit.Color asBukkitColor() {
      return this.bukkit;
   }

   @Nullable
   @Override
   public DyeColor asDyeColor() {
      return this.dye;
   }

   @Override
   public String getName() {
      String rgb = this.bukkit.getRed() + ", " + this.bukkit.getGreen() + ", " + this.bukkit.getBlue();
      return this.bukkit.getAlpha() != 255 ? "argb " + this.bukkit.getAlpha() + ", " + rgb : "rgb " + rgb;
   }

   @Nullable
   public static ColorRGB fromString(String string) {
      Matcher matcher = RGB_PATTERN.matcher(string);
      return !matcher.matches()
         ? null
         : new ColorRGB(NumberUtils.toInt(matcher.group(1)), NumberUtils.toInt(matcher.group(2)), NumberUtils.toInt(matcher.group(3)));
   }

   @Nullable
   public static ColorRGB fromHexString(String hex) {
      if (hex.length() != 6 && hex.length() != 8) {
         return null;
      } else {
         if (hex.length() == 6) {
            hex = "FF" + hex;
         }

         try {
            int argb = Integer.parseUnsignedInt(hex, 16);
            return fromRGBA(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >> 24 & 0xFF);
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   @Override
   public Fields serialize() throws NotSerializableException {
      return new Fields(this, Variables.yggdrasil);
   }

   @Override
   public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
      org.bukkit.Color b = fields.getObject("bukkit", org.bukkit.Color.class);
      DyeColor d = fields.getObject("dye", DyeColor.class);
      if (b != null) {
         if (d == null) {
            this.dye = DyeColor.getByColor(b);
         } else {
            this.dye = d;
         }

         this.bukkit = b;
      }
   }
}

package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.color.AlphaColor;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.color.Color;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTByteArray;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTDouble;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTList;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTLong;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTLongArray;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTString;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Either;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.UniqueIdUtil;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Experimental
public final class NbtCodecs {
   public static final NbtCodec<Integer> INT = new NbtCodec<Integer>() {
      public Integer decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt.castOrThrow(NBTNumber.class).getAsInt();
      }

      public NBT encode(PacketWrapper<?> wrapper, Integer value) {
         return new NBTInt(value);
      }
   };
   public static final NbtCodec<Double> DOUBLE = new NbtCodec<Double>() {
      public Double decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt.castOrThrow(NBTNumber.class).getAsDouble();
      }

      public NBT encode(PacketWrapper<?> wrapper, Double value) {
         return new NBTDouble(value);
      }
   };
   public static final NbtCodec<Float> FLOAT = new NbtCodec<Float>() {
      public Float decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt.castOrThrow(NBTNumber.class).getAsFloat();
      }

      public NBT encode(PacketWrapper<?> wrapper, Float value) {
         return new NBTFloat(value);
      }
   };
   public static final NbtCodec<Boolean> BOOLEAN = new NbtCodec<Boolean>() {
      public Boolean decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt.castOrThrow(NBTNumber.class).getAsByte() != 0;
      }

      public NBT encode(PacketWrapper<?> wrapper, Boolean value) {
         return new NBTByte(value);
      }
   };
   public static final NbtCodec<String> STRING = new NbtCodec<String>() {
      public String decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt.castOrThrow(NBTString.class).getValue();
      }

      public NBT encode(PacketWrapper<?> wrapper, String value) {
         return new NBTString(value);
      }
   };
   public static final NbtCodec<List<String>> STRING_LIST = STRING.applyList();
   public static final NbtCodec<List<? extends NBT>> GENERIC_LIST = new NbtCodec<List<? extends NBT>>() {
      public List<? extends NBT> decode(NBT nbt, PacketWrapper<?> wrapper) {
         if (nbt instanceof NBTList) {
            return ((NBTList)nbt).unwrapTags();
         } else if (nbt instanceof NBTIntArray) {
            int[] arr = ((NBTIntArray)nbt).getValue();
            List<NBTInt> list = new ArrayList<>(arr.length);

            for (int num : arr) {
               list.add(new NBTInt(num));
            }

            return list;
         } else if (nbt instanceof NBTByteArray) {
            byte[] arr = ((NBTByteArray)nbt).getValue();
            List<NBTByte> list = new ArrayList<>(arr.length);

            for (byte num : arr) {
               list.add(new NBTByte(num));
            }

            return list;
         } else if (!(nbt instanceof NBTLongArray)) {
            throw new NbtCodecException("Not a list: " + nbt);
         } else {
            long[] arr = ((NBTLongArray)nbt).getValue();
            List<NBTLong> list = new ArrayList<>(arr.length);

            for (long num : arr) {
               list.add(new NBTLong(num));
            }

            return list;
         }
      }

      public NBT encode(PacketWrapper<?> wrapper, List<? extends NBT> value) {
         if (value.isEmpty()) {
            return new NBTList<>(NBTType.END, 0);
         } else {
            NBTType<?> type = NBTList.getCommonTagType(value);
            if (type == NBTType.COMPOUND) {
               NBTList<NBTCompound> list = new NBTList<>(NBTType.COMPOUND, value.size());

               for (NBT tag : value) {
                  list.addTagOrWrap(tag);
               }

               return list;
            } else if (type == NBTType.INT) {
               int[] arr = new int[value.size()];

               for (int i = 0; i < arr.length; i++) {
                  arr[i] = ((NBTInt)value.get(i)).getAsInt();
               }

               return new NBTIntArray(arr);
            } else if (type == NBTType.BYTE) {
               byte[] arr = new byte[value.size()];

               for (int i = 0; i < arr.length; i++) {
                  arr[i] = ((NBTByte)value.get(i)).getAsByte();
               }

               return new NBTByteArray(arr);
            } else if (type != NBTType.LONG) {
               return new NBTList<>(type, value);
            } else {
               long[] arr = new long[value.size()];

               for (int i = 0; i < arr.length; i++) {
                  arr[i] = ((NBTLong)value.get(i)).getAsLong();
               }

               return new NBTLongArray(arr);
            }
         }
      }
   };
   public static final NbtCodec<int[]> INT_ARRAY = new NbtCodec<int[]>() {
      public int[] decode(NBT nbt, PacketWrapper<?> wrapper) {
         if (nbt instanceof NBTIntArray) {
            return ((NBTIntArray)nbt).getValue();
         } else {
            List<? extends NBT> list = NbtCodecs.GENERIC_LIST.decode(nbt, wrapper);
            int size = list.size();
            int[] array = new int[size];

            for (int i = 0; i < size; i++) {
               array[i] = list.get(i).castOrThrow(NBTNumber.class).getAsInt();
            }

            return array;
         }
      }

      public NBT encode(PacketWrapper<?> wrapper, int[] value) {
         return new NBTIntArray(value);
      }
   };
   public static final NbtCodec<UUID> UUID = INT_ARRAY.apply(UniqueIdUtil::fromIntArray, UniqueIdUtil::toIntArray);
   public static final NbtCodec<UUID> STRING_UUID = new NbtCodec<UUID>() {
      public UUID decode(NBT nbt, PacketWrapper<?> wrapper) throws NbtCodecException {
         String uuidStr = ((NBTString)nbt).getValue();
         if (uuidStr.length() == 36) {
            try {
               return java.util.UUID.fromString(uuidStr);
            } catch (IllegalArgumentException var5) {
            }
         }

         throw new NbtCodecException("Invalid UUID " + uuidStr);
      }

      public NBT encode(PacketWrapper<?> wrapper, UUID value) throws NbtCodecException {
         return new NBTString(value.toString());
      }
   };
   public static final NbtCodec<UUID> LENIENT_UUID = UUID.withAlternative(STRING_UUID);
   public static final NbtCodec<Color> RGB_COLOR = new NbtCodec<Color>() {
      public Color decode(NBT nbt, PacketWrapper<?> wrapper) {
         if (nbt instanceof NBTString) {
            String string = ((NBTString)nbt).getValue();
            if (string.isEmpty() || string.charAt(0) != '#') {
               throw new NbtCodecException("Hex color must begin with #");
            } else if (string.length() - 1 != 6) {
               throw new NbtCodecException("Hex color is wrong, expected 6 digits but got " + string);
            } else {
               try {
                  String digits = string.substring(1);
                  int rgb = Integer.parseInt(digits, 16);
                  return new Color(rgb);
               } catch (NumberFormatException var6) {
                  throw new NbtCodecException(var6);
               }
            }
         } else {
            return nbt instanceof NBTNumber ? new Color(((NBTNumber)nbt).getAsInt()) : Color.WHITE;
         }
      }

      public NBT encode(PacketWrapper<?> wrapper, Color value) {
         return new NBTInt(value.asRGB());
      }
   };
   public static final NbtCodec<AlphaColor> ARGB_COLOR = new NbtCodec<AlphaColor>() {
      public AlphaColor decode(NBT nbt, PacketWrapper<?> wrapper) {
         if (nbt instanceof NBTString) {
            String string = ((NBTString)nbt).getValue();
            if (string.isEmpty() || string.charAt(0) != '#') {
               throw new NbtCodecException("Hex color must begin with #");
            } else if (string.length() - 1 != 8) {
               throw new NbtCodecException("Hex color is wrong, expected 8 digits but got " + string);
            } else {
               try {
                  String digits = string.substring(1);
                  int rgb = Integer.parseUnsignedInt(digits, 16);
                  return new AlphaColor(rgb);
               } catch (NumberFormatException var6) {
                  throw new NbtCodecException(var6);
               }
            }
         } else {
            return nbt instanceof NBTNumber ? new AlphaColor(((NBTNumber)nbt).getAsInt()) : AlphaColor.WHITE;
         }
      }

      public NBT encode(PacketWrapper<?> wrapper, AlphaColor value) {
         return new NBTInt(value.asRGB());
      }
   };
   public static final NbtCodec<NBT> NOOP = new NbtCodec<NBT>() {
      public NBT decode(NBT nbt, PacketWrapper<?> wrapper) {
         return nbt;
      }

      public NBT encode(PacketWrapper<?> wrapper, NBT value) {
         return value;
      }
   };
   private static final NbtCodec<?> ERROR_CODEC = new NbtCodec<Object>() {
      @Override
      public Object decode(NBT nbt, PacketWrapper<?> wrapper) {
         throw new UnsupportedOperationException();
      }

      @Override
      public NBT encode(PacketWrapper<?> wrapper, Object value) {
         throw new UnsupportedOperationException();
      }
   };

   private NbtCodecs() {
   }

   public static <T> NbtCodec<T> errorCodec() {
      return (NbtCodec<T>)ERROR_CODEC;
   }

   public static <T extends Enum<T> & CodecNameable> NbtCodec<T> forEnum(T[] values) {
      return new NbtCodec<T>() {
         private final Map<String, T> map = new HashMap<>(values.length);

         {
            for (T value : values) {
               T existingValue = this.map.putIfAbsent(value.getCodecName(), value);
               if (existingValue != null) {
                  throw new IllegalStateException("Can't create codec for enum with duplicate names: " + existingValue);
               }
            }
         }

         public T decode(NBT nbt, PacketWrapper<?> wrapper) {
            String key = ((NBTString)nbt).getValue();
            T value = this.map.get(key);
            if (value == null) {
               throw new NbtCodecException("Can't find " + key + " in " + this.map.keySet());
            } else {
               return value;
            }
         }

         public NBT encode(PacketWrapper<?> wrapper, T value) {
            return new NBTString(value.getCodecName());
         }
      };
   }

   public static <T extends MappedEntity> NbtCodec<T> forRegistry(IRegistry<T> registry) {
      return new NbtCodec<T>() {
         public T decode(NBT nbt, PacketWrapper<?> wrapper) {
            ClientVersion version = wrapper.getServerVersion().toClientVersion();
            IRegistry<T> replacedRegistry = wrapper.replaceRegistry(registry);
            T entry = null;
            if (nbt instanceof NBTNumber) {
               int id = ((NBTNumber)nbt).getAsInt();
               entry = replacedRegistry.getById(version, id);
               if (entry == null) {
                  throw new NbtCodecException("Can't find #" + id + " in " + registry.getRegistryKey());
               }
            } else if (nbt instanceof NBTString) {
               String name = ((NBTString)nbt).getValue();
               entry = replacedRegistry.getByName(version, name);
               if (entry == null) {
                  throw new NbtCodecException("Can't find " + name + " in " + registry.getRegistryKey());
               }
            }

            if (entry == null) {
               throw new NbtCodecException("Can't decode registry " + registry.getRegistryKey());
            } else {
               return entry;
            }
         }

         public NBT encode(PacketWrapper<?> wrapper, T value) {
            if (!value.isRegistered()) {
               throw new NbtCodecException("Unregistered entry");
            } else {
               return ResourceLocation.CODEC.encode(wrapper, value.getName());
            }
         }
      };
   }

   public static <L, R> NbtCodec<Either<L, R>> either(NbtCodec<L> left, NbtCodec<R> right) {
      return new NbtCodec<Either<L, R>>() {
         public Either<L, R> decode(NBT nbt, PacketWrapper<?> wrapper) throws NbtCodecException {
            try {
               return Either.createLeft(left.decode(nbt, wrapper));
            } catch (NbtCodecException var6) {
               try {
                  return Either.createRight(right.decode(nbt, wrapper));
               } catch (NbtCodecException var5) {
                  var5.addSuppressed(var6);
                  throw var5;
               }
            }
         }

         public NBT encode(PacketWrapper<?> wrapper, Either<L, R> value) throws NbtCodecException {
            return value.map(v -> left.encode(wrapper, v), v -> right.encode(wrapper, v));
         }
      };
   }

   public static <T> NbtMapCodec<T> forUnit(Supplier<T> supplier) {
      return new NbtMapCodec<T>() {
         @Override
         public T decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            return supplier.get();
         }

         @Override
         public void encode(NBTCompound tag, PacketWrapper<?> wrapper, T value) throws NbtCodecException {
         }
      };
   }
}

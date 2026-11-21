package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufInputStream;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufOutputStream;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.codec.NBTCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection.Reflection;
import ac.grim.grimac.shaded.kyori.adventure.nbt.BinaryTag;
import ac.grim.grimac.shaded.kyori.adventure.nbt.BinaryTagType;
import ac.grim.grimac.shaded.kyori.adventure.nbt.BinaryTagTypes;
import ac.grim.grimac.shaded.kyori.adventure.nbt.EndBinaryTag;
import ac.grim.grimac.shaded.kyori.adventure.nbt.TagStringIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AdventureNbtUtil {
   private static final byte END_TAG_ID = 0;
   private static final TagStringIO TAG_STRING_IO;
   private static final Method TAG_TYPE_GET_ID;
   private static final Method TAG_TYPE_READ;
   private static final Method TAG_TYPE_WRITE;
   private static final BinaryTagType<?>[] NBT_TAG_TYPES;

   private AdventureNbtUtil() {
   }

   private static BinaryTagType<?>[] buildNbtTagTypes() {
      BinaryTagTypes.BYTE.id();
      List<BinaryTagType<? extends BinaryTag>> types = null;
      List<Class<?>> candidates = new ArrayList<>();
      candidates.add(BinaryTagType.class);

      for (Class<?> c = BinaryTagTypes.BYTE.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
         if (!candidates.contains(c)) {
            candidates.add(c);
         }
      }

      try {
         label56:
         for (Class<?> candidate : candidates) {
            for (Field field : candidate.getDeclaredFields()) {
               if (Modifier.isStatic(field.getModifiers()) && List.class.isAssignableFrom(field.getType())) {
                  field.setAccessible(true);
                  Object value = field.get(null);
                  if (value instanceof List && ((List)value).contains(BinaryTagTypes.BYTE)) {
                     types = (List<BinaryTagType<? extends BinaryTag>>)value;
                     break label56;
                  }
               }
            }
         }
      } catch (Exception var9) {
         throw new RuntimeException("Error scanning for Adventure NBT registry", var9);
      }

      if (types == null) {
         throw new RuntimeException("Could not locate Adventure NBT registry field (mapping mismatch?)");
      } else {
         BinaryTagType<?>[] nbtTagTypes = new BinaryTagType[types.size()];

         for (int i = 0; i < nbtTagTypes.length; i++) {
            BinaryTagType<? extends BinaryTag> type = types.get(i);
            byte tagId = getTagId(type);
            if (tagId != i) {
               throw new IllegalStateException("Registered nbt tag types are wrong: " + tagId + " != " + i);
            }

            nbtTagTypes[i] = type;
         }

         return nbtTagTypes;
      }
   }

   private static byte getTagId(BinaryTagType<?> tagType) {
      try {
         return (Byte)TAG_TYPE_GET_ID.invoke(tagType);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   public static BinaryTag readAdventureTag(Object buf) {
      byte tagTypeId = ByteBufHelper.readByte(buf);
      if (tagTypeId == 0) {
         return EndBinaryTag.endBinaryTag();
      } else {
         BinaryTagType<?> tagType = NBT_TAG_TYPES[tagTypeId];

         try {
            return (BinaryTag)TAG_TYPE_READ.invoke(tagType, new ByteBufInputStream(buf));
         } catch (Exception var4) {
            throw new RuntimeException("Error while reading adventure nbt tag from buf: " + buf, var4);
         }
      }
   }

   public static void writeAdventureTag(Object buf, BinaryTag tag) {
      BinaryTagType<? super BinaryTag> tagType = (BinaryTagType<? super BinaryTag>)tag.type();
      byte tagId = getTagId(tagType);
      ByteBufHelper.writeByte(buf, tagId);
      if (tagId != 0) {
         try {
            TAG_TYPE_WRITE.invoke(tagType, tag, new ByteBufOutputStream(buf));
         } catch (Exception var5) {
            throw new RuntimeException("Error while writing adventure nbt tag to buf: " + tag, var5);
         }
      }
   }

   public static NBT fromAdventure(BinaryTag tag) {
      Object buf = UnpooledByteBufAllocationHelper.buffer();

      NBT var2;
      try {
         writeAdventureTag(buf, tag);
         var2 = NBTCodec.readNBTFromBuffer(buf, ServerVersion.getLatest());
      } finally {
         ByteBufHelper.release(buf);
      }

      return var2;
   }

   public static BinaryTag toAdventure(NBT tag) {
      Object buf = UnpooledByteBufAllocationHelper.buffer();

      BinaryTag var2;
      try {
         NBTCodec.writeNBTToBuffer(buf, ServerVersion.getLatest(), tag);
         var2 = readAdventureTag(buf);
      } finally {
         ByteBufHelper.release(buf);
      }

      return var2;
   }

   public static NBT fromString(String string) {
      BinaryTag advTag;
      try {
         advTag = TAG_STRING_IO.asTag(string);
      } catch (IOException var3) {
         throw new RuntimeException("Error while decoding nbt from string: " + string, var3);
      }

      return fromAdventure(advTag);
   }

   public static String toString(NBT tag) {
      BinaryTag advTag = toAdventure(tag);

      try {
         return TAG_STRING_IO.asString(advTag);
      } catch (IOException var3) {
         throw new RuntimeException("Error while encoding nbt to string: " + advTag, var3);
      }
   }

   static {
      TagStringIO tagStringIo;
      try {
         tagStringIo = TagStringIO.tagStringIO();
      } catch (Throwable var2) {
         tagStringIo = TagStringIO.get();
      }

      TAG_STRING_IO = tagStringIo;
      TAG_TYPE_GET_ID = Reflection.getMethodExact(BinaryTagType.class, "id", byte.class);
      TAG_TYPE_READ = Reflection.getMethodExact(BinaryTagType.class, "read", BinaryTag.class, DataInput.class);
      TAG_TYPE_WRITE = Reflection.getMethodExact(BinaryTagType.class, "write", void.class, BinaryTag.class, DataOutput.class);
      NBT_TAG_TYPES = buildNbtTagTypes();
   }
}

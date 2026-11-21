package com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_21_5;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.mcstructs.converter.DataConverter;
import com.viaversion.viaversion.libs.mcstructs.converter.impl.v1_20_3.NbtConverter_v1_20_3;
import com.viaversion.viaversion.libs.mcstructs.converter.model.Result;
import com.viaversion.viaversion.libs.mcstructs.snbt.SNbt;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class NbtConverter_v1_21_5 extends NbtConverter_v1_20_3 {
   public static final NbtConverter_v1_21_5 INSTANCE = new NbtConverter_v1_21_5();

   @Override
   public DataConverter<Tag> forkIfDefault() {
      return this == INSTANCE ? new NbtConverter_v1_21_5() : this;
   }

   public NbtConverter_v1_21_5() {
      this(SNbt.V1_21_5);
   }

   protected NbtConverter_v1_21_5(SNbt<CompoundTag> sNbt) {
      super(sNbt);
   }

   @Override
   public Result<Tag> mergeList(@Nullable Tag list, List<Tag> values) {
      NbtConverter_v1_21_5.ListType listType;
      if (list == null) {
         listType = NbtConverter_v1_21_5.ListType.LIST;
      } else if (list instanceof ByteArrayTag) {
         if (((ByteArrayTag)list).length() == 0) {
            listType = NbtConverter_v1_21_5.ListType.LIST;
         } else {
            listType = NbtConverter_v1_21_5.ListType.BYTE;
         }
      } else if (list instanceof IntArrayTag) {
         if (((IntArrayTag)list).length() == 0) {
            listType = NbtConverter_v1_21_5.ListType.LIST;
         } else {
            listType = NbtConverter_v1_21_5.ListType.INT;
         }
      } else if (list instanceof LongArrayTag) {
         if (((LongArrayTag)list).length() == 0) {
            listType = NbtConverter_v1_21_5.ListType.LIST;
         } else {
            listType = NbtConverter_v1_21_5.ListType.LONG;
         }
      } else {
         if (!(list instanceof ListTag)) {
            return Result.error("Invalid list tag: " + list);
         }

         ListTag<?> listTag = (ListTag<?>)list;
         if (CompoundTag.class == listTag.getElementType()) {
            listType = NbtConverter_v1_21_5.ListType.MIXED_LIST;
         } else {
            listType = NbtConverter_v1_21_5.ListType.LIST;
         }
      }

      Result<List<Tag>> listResult = list == null ? Result.success(new ArrayList<>()) : this.asList(list);
      if (listResult.isError()) {
         return listResult.mapError();
      } else {
         List<Tag> tags = listResult.get();
         tags.addAll(values);
         if (tags.isEmpty()) {
            return Result.success(new ListTag());
         } else {
            if (listType.equals(NbtConverter_v1_21_5.ListType.BYTE)) {
               if (tags.stream().allMatch(tagx -> tagx instanceof ByteTag)) {
                  byte[] bytes = new byte[tags.size()];

                  for (int i = 0; i < tags.size(); i++) {
                     bytes[i] = ((ByteTag)tags.get(i)).getValue();
                  }

                  return Result.success(new ByteArrayTag(bytes));
               }
            } else if (listType.equals(NbtConverter_v1_21_5.ListType.INT)) {
               if (tags.stream().allMatch(tagx -> tagx instanceof IntTag)) {
                  int[] ints = new int[tags.size()];

                  for (int i = 0; i < tags.size(); i++) {
                     ints[i] = ((IntTag)tags.get(i)).getValue();
                  }

                  return Result.success(new IntArrayTag(ints));
               }
            } else if (listType.equals(NbtConverter_v1_21_5.ListType.LONG)) {
               if (tags.stream().allMatch(tagx -> tagx instanceof LongTag)) {
                  long[] longs = new long[tags.size()];

                  for (int i = 0; i < tags.size(); i++) {
                     longs[i] = ((LongTag)tags.get(i)).getValue();
                  }

                  return Result.success(new LongArrayTag(longs));
               }
            } else if (listType.equals(NbtConverter_v1_21_5.ListType.LIST)) {
               Tag type = null;

               for (Tag tag : tags) {
                  if (type == null) {
                     type = tag;
                  } else if (!type.equals(tag)) {
                     type = null;
                     break;
                  }
               }

               if (type != null) {
                  return Result.success(new ListTag<>(tags));
               }
            }

            ListTag<Tag> out = new ListTag<>();

            for (Tag tagx : tags) {
               boolean isMarker = tagx instanceof CompoundTag && ((CompoundTag)tagx).size() == 1 && ((CompoundTag)tagx).contains("");
               if (tagx instanceof CompoundTag && !isMarker) {
                  out.add(tagx);
               } else {
                  CompoundTag compoundTag = new CompoundTag();
                  compoundTag.put("", tagx);
                  out.add(compoundTag);
               }
            }

            return Result.success(out);
         }
      }
   }

   private static enum ListType {
      BYTE,
      INT,
      LONG,
      LIST,
      MIXED_LIST;
   }
}

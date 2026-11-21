package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTList<T extends NBT> extends NBT {
   protected final NBTType<T> type;
   protected final List<T> tags;

   public NBTList(NBTType<T> type) {
      this.type = type;
      this.tags = new ArrayList<>();
   }

   public NBTList(NBTType<T> type, int size) {
      this.type = type;
      this.tags = new ArrayList<>(size);
   }

   public NBTList(NBTType<T> type, List<T> tags) {
      this.type = type;
      this.tags = new ArrayList<>();
      this.tags.addAll(tags);
   }

   public static NBTList<NBTCompound> createCompoundList() {
      return new NBTList<>(NBTType.COMPOUND);
   }

   public static NBTList<NBTString> createStringList() {
      return new NBTList<>(NBTType.STRING);
   }

   public static NBTType<?> getCommonTagType(List<? extends NBT> tags) {
      NBTType<?> type = NBTType.END;

      for (NBT tag : tags) {
         if (type == NBTType.END) {
            type = tag.getType();
         } else if (type != tag.getType()) {
            return NBTType.COMPOUND;
         }
      }

      return type;
   }

   @Override
   public NBTType<NBTList> getType() {
      return NBTType.LIST;
   }

   public NBTType<T> getTagsType() {
      return this.type;
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   public int size() {
      return this.tags.size();
   }

   public List<T> getTags() {
      return Collections.unmodifiableList(this.tags);
   }

   public T getTag(int index) {
      return this.tags.get(index);
   }

   public void setTag(int index, T tag) {
      this.validateAddTag(tag);
      this.tags.set(index, tag);
   }

   public void addTag(int index, T tag) {
      this.validateAddTag(tag);
      this.tags.add(index, tag);
   }

   public void addTag(T tag) {
      this.validateAddTag(tag);
      this.tags.add(tag);
   }

   public void addTagUnsafe(int index, NBT nbt) {
      this.addTag(index, (T)nbt);
   }

   public void addTagUnsafe(NBT nbt) {
      this.addTag((T)nbt);
   }

   public void removeTag(int index) {
      this.tags.remove(index);
   }

   protected void validateAddTag(T tag) {
      if (this.type != tag.getType()) {
         throw new IllegalArgumentException(MessageFormat.format("Invalid tag type. Expected {0}, got {1}.", this.type.getNBTClass(), tag.getClass()));
      }
   }

   public void addTagOrWrap(NBT tag) {
      if (this.type == tag.getType()) {
         this.tags.add((T)tag);
      } else {
         if (this.type != NBTType.COMPOUND) {
            throw new IllegalArgumentException("Can't add or wrap tag " + tag + " to list of type " + this.type);
         }

         NBTCompound wrapped = new NBTCompound();
         wrapped.setTag("", tag);
         this.tags.add((T)wrapped);
      }
   }

   private static NBT tryUnwrap(NBTCompound tag) {
      if (tag.tags.size() == 1) {
         NBT unwrapped = tag.getTagOrNull("");
         if (unwrapped != null) {
            return unwrapped;
         }
      }

      return tag;
   }

   public List<? extends NBT> unwrapTags() {
      if (this.type != NBTType.COMPOUND) {
         return new ArrayList<>(this.tags);
      } else {
         List<NBT> tags = new ArrayList<>(this.tags.size());

         for (T tag : this.tags) {
            if (tag instanceof NBTCompound) {
               tags.add(tryUnwrap((NBTCompound)tag));
            } else {
               tags.add(tag);
            }
         }

         return tags;
      }
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NBTList<T> other = (NBTList<T>)obj;
         return Objects.equals(this.type, other.type) && Objects.equals(this.tags, other.tags);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.tags);
   }

   public NBTList<T> copy() {
      List<T> newTags = new ArrayList<>();

      for (T tag : this.tags) {
         newTags.add((T)tag.copy());
      }

      return new NBTList<>(this.type, newTags);
   }

   @Override
   public String toString() {
      return "List(" + this.tags + ")";
   }
}

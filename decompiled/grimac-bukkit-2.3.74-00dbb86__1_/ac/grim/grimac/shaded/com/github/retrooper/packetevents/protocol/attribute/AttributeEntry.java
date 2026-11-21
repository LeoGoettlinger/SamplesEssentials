package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class AttributeEntry {
   public static final NbtCodec<AttributeEntry> CODEC = (new NbtMapCodec<AttributeEntry>() {
      public AttributeEntry decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         Attribute attribute = tag.getOrThrow("attribute", Attribute.CODEC, wrapper);
         AttributeModifier modifier = AttributeModifier.MAP_CODEC.decode(tag, wrapper);
         return new AttributeEntry(attribute, modifier);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, AttributeEntry value) throws NbtCodecException {
         tag.set("attribute", value.attribute, Attribute.CODEC, wrapper);
         AttributeModifier.MAP_CODEC.encode(tag, wrapper, value.modifier);
      }
   }).codec();
   private final Attribute attribute;
   private final AttributeModifier modifier;

   public AttributeEntry(Attribute attribute, AttributeModifier modifier) {
      this.attribute = attribute;
      this.modifier = modifier;
   }

   public static AttributeEntry read(PacketWrapper<?> wrapper) {
      Attribute attribute = wrapper.readMappedEntity(Attributes.getRegistry());
      AttributeModifier modifier = AttributeModifier.read(wrapper);
      return new AttributeEntry(attribute, modifier);
   }

   public static void write(PacketWrapper<?> wrapper, AttributeEntry entry) {
      wrapper.writeMappedEntity(entry.attribute);
      AttributeModifier.write(wrapper, entry.modifier);
   }

   public Attribute getAttribute() {
      return this.attribute;
   }

   public AttributeModifier getModifier() {
      return this.modifier;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         AttributeEntry that = (AttributeEntry)obj;
         return !this.attribute.equals(that.attribute) ? false : this.modifier.equals(that.modifier);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.attribute, this.modifier);
   }
}

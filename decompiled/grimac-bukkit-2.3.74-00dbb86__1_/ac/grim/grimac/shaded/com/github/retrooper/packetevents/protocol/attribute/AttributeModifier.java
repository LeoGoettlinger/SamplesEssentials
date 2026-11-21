package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTDouble;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class AttributeModifier {
   public static final NbtMapCodec<AttributeModifier> MAP_CODEC = new NbtMapCodec<AttributeModifier>() {
      public AttributeModifier decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
            throw new NbtCodecException(wrapper.getServerVersion() + " is not supported");
         } else {
            ResourceLocation id = tag.getOrThrow("id", ResourceLocation.CODEC, wrapper);
            double amount = tag.getNumberTagValueOrThrow("amount").doubleValue();
            AttributeOperation operation = tag.getOrThrow("operation", AttributeOperation.CODEC, wrapper);
            return new AttributeModifier(id, amount, operation);
         }
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, AttributeModifier value) throws NbtCodecException {
         if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
            throw new NbtCodecException(wrapper.getServerVersion() + " is not supported");
         } else {
            tag.set("id", value.id, ResourceLocation.CODEC, wrapper);
            tag.setTag("amount", new NBTDouble(value.amount));
            tag.set("operation", value.operation, AttributeOperation.CODEC, wrapper);
         }
      }
   };
   @ApiStatus.Obsolete
   private final UUID uniqueId;
   private final ResourceLocation id;
   private final double amount;
   private final AttributeOperation operation;

   @ApiStatus.Obsolete
   public AttributeModifier(UUID uniqueId, double amount, AttributeOperation operation) {
      this.uniqueId = uniqueId;
      this.id = new ResourceLocation(uniqueId.toString());
      this.amount = amount;
      this.operation = operation;
   }

   public AttributeModifier(ResourceLocation id, double amount, AttributeOperation operation) {
      this.uniqueId = WrapperPlayServerUpdateAttributes.PropertyModifier.generateSemiUniqueId(id);
      this.id = id;
      this.amount = amount;
      this.operation = operation;
   }

   public static AttributeModifier read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
         UUID uniqueId = wrapper.readUUID();
         double amount = wrapper.readDouble();
         AttributeOperation operation = wrapper.readEnum(AttributeOperation.values());
         return new AttributeModifier(uniqueId, amount, operation);
      } else {
         ResourceLocation id = ResourceLocation.read(wrapper);
         double amount = wrapper.readDouble();
         AttributeOperation operation = wrapper.readEnum(AttributeOperation.values());
         return new AttributeModifier(id, amount, operation);
      }
   }

   public static void write(PacketWrapper<?> wrapper, AttributeModifier modifier) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
         wrapper.writeUUID(modifier.uniqueId);
      } else {
         ResourceLocation.write(wrapper, modifier.id);
      }

      wrapper.writeDouble(modifier.amount);
      wrapper.writeEnum(modifier.operation);
   }

   @ApiStatus.Obsolete
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public double getAmount() {
      return this.amount;
   }

   public AttributeOperation getOperation() {
      return this.operation;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         AttributeModifier that = (AttributeModifier)obj;
         if (Double.compare(that.amount, this.amount) != 0) {
            return false;
         } else {
            return !this.id.equals(that.id) ? false : this.operation == that.operation;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.amount, this.operation);
   }
}

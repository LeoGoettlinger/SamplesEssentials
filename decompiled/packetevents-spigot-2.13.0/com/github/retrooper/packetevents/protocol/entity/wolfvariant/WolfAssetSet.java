package com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WolfAssetSet {
   public static final NbtMapCodec<WolfAssetSet> MAP_CODEC = new NbtMapCodec<WolfAssetSet>() {
      public WolfAssetSet decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         String suffix = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) ? "" : "_texture";
         ResourceLocation wildId = tag.getOrThrow("wild" + suffix, ResourceLocation.CODEC, wrapper);
         ResourceLocation tameId = tag.getOrThrow("tame" + suffix, ResourceLocation.CODEC, wrapper);
         ResourceLocation angryId = tag.getOrThrow("angry" + suffix, ResourceLocation.CODEC, wrapper);
         return new WolfAssetSet(wildId, tameId, angryId);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, WolfAssetSet value) throws NbtCodecException {
         String suffix = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) ? "" : "_texture";
         tag.set("wild" + suffix, value.getWildId(), ResourceLocation.CODEC, wrapper);
         tag.set("tame" + suffix, value.getTameId(), ResourceLocation.CODEC, wrapper);
         tag.set("angry" + suffix, value.getAngryId(), ResourceLocation.CODEC, wrapper);
      }
   };
   public static final NbtCodec<WolfAssetSet> CODEC = MAP_CODEC.codec();
   private final ResourceLocation wildId;
   private final ResourceLocation tameId;
   private final ResourceLocation angryId;

   public WolfAssetSet(ResourceLocation wildId, ResourceLocation tameId, ResourceLocation angryId) {
      this.wildId = wildId;
      this.tameId = tameId;
      this.angryId = angryId;
   }

   @Internal
   public static WolfAssetSet getOrThrow(@KeyPattern.Namespace String namespace, @KeyPattern.Value String prefix, @KeyPattern.Value String suffix) {
      return new WolfAssetSet(
         new ResourceLocation(namespace, prefix + suffix),
         new ResourceLocation(namespace, prefix + "_tame" + suffix),
         new ResourceLocation(namespace, prefix + "_angry" + suffix)
      );
   }

   public ResourceLocation getWildId() {
      return this.wildId;
   }

   public ResourceLocation getTameId() {
      return this.tameId;
   }

   public ResourceLocation getAngryId() {
      return this.angryId;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof WolfAssetSet)) {
         return false;
      } else {
         WolfAssetSet that = (WolfAssetSet)obj;
         if (!this.wildId.equals(that.wildId)) {
            return false;
         } else {
            return !this.tameId.equals(that.tameId) ? false : this.angryId.equals(that.angryId);
         }
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.wildId, this.tameId, this.angryId);
   }
}

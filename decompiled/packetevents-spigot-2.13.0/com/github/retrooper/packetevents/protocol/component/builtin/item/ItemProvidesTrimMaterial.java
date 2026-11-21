package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.trimmaterial.TrimMaterial;
import com.github.retrooper.packetevents.protocol.item.trimmaterial.TrimMaterials;
import com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class ItemProvidesTrimMaterial {
   private MaybeMappedEntity<TrimMaterial> material;

   public ItemProvidesTrimMaterial(TrimMaterial material) {
      this(new MaybeMappedEntity<>(material));
   }

   @Obsolete
   public ItemProvidesTrimMaterial(MaybeMappedEntity<TrimMaterial> material) {
      this.material = material;
   }

   public static ItemProvidesTrimMaterial read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         return new ItemProvidesTrimMaterial(TrimMaterial.read(wrapper));
      } else {
         MaybeMappedEntity<TrimMaterial> material = MaybeMappedEntity.read(wrapper, TrimMaterials.getRegistry(), TrimMaterial::read);
         return new ItemProvidesTrimMaterial(material);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ItemProvidesTrimMaterial material) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         TrimMaterial.write(wrapper, material.material.getValueOrThrow());
      } else {
         MaybeMappedEntity.write(wrapper, material.material, TrimMaterial::write);
      }
   }

   public MaybeMappedEntity<TrimMaterial> getMaterial() {
      return this.material;
   }

   public void setMaterial(MaybeMappedEntity<TrimMaterial> material) {
      this.material = material;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ItemProvidesTrimMaterial)) {
         return false;
      } else {
         ItemProvidesTrimMaterial that = (ItemProvidesTrimMaterial)obj;
         return this.material.equals(that.material);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.material);
   }
}

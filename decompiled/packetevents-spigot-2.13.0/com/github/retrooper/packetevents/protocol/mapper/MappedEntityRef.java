package com.github.retrooper.packetevents.protocol.mapper;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtDecoder;
import com.github.retrooper.packetevents.protocol.util.NbtEncoder;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.MemoizedSupplier;
import com.github.retrooper.packetevents.util.mappings.IRegistry;
import com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MappedEntityRef<T extends MappedEntity> extends Supplier<T> {
   T get();

   static <T extends MappedEntity> NbtCodec<MappedEntityRef<T>> codec(NbtCodec<T> codec) {
      return new NbtCodec<MappedEntityRef<T>>() {
         public MappedEntityRef<T> decode(NBT tag, PacketWrapper<?> wrapper) throws NbtCodecException {
            MemoizedSupplier<T> supplier = new MemoizedSupplier<>(() -> codec.decode(tag, wrapper));
            return new MappedEntityRef.Nbt<>(tag, supplier);
         }

         public NBT encode(PacketWrapper<?> wrapper, MappedEntityRef<T> value) throws NbtCodecException {
            return codec.encode(wrapper, value.get());
         }
      };
   }

   static <T extends MappedEntity> MappedEntityRef<T> decode(NBT tag, IRegistry<T> registry, NbtDecoder<T> decoder, PacketWrapper<?> wrapper) {
      if (tag instanceof NBTString) {
         ResourceLocation name = new ResourceLocation(((NBTString)tag).getValue());
         return new MappedEntityRef.Named<>(wrapper, registry, name);
      } else {
         return new MappedEntityRef.Static<>(decoder.decode(tag, wrapper));
      }
   }

   static <T extends MappedEntity> NBT encode(PacketWrapper<?> wrapper, NbtEncoder<T> encoder, MappedEntityRef<T> ref) {
      if (ref instanceof MappedEntityRef.Named) {
         return new NBTString(((MappedEntityRef.Named)ref).name.toString());
      } else if (ref instanceof MappedEntityRef.Static) {
         return encoder.encode(wrapper, ((MappedEntityRef.Static)ref).entity);
      } else {
         throw new UnsupportedOperationException("Unsupported MappedEntityRef implementation: " + ref);
      }
   }

   public static final class Named<T extends MappedEntity> implements MappedEntityRef<T> {
      private final WeakReference<IRegistryHolder> registryHolder;
      private final ClientVersion version;
      private final IRegistry<T> registry;
      private final ResourceLocation name;
      @Nullable
      private volatile T entity;

      public Named(PacketWrapper<?> wrapper, IRegistry<T> registry, ResourceLocation name) {
         this(wrapper.getRegistryHolder(), wrapper.getServerVersion().toClientVersion(), registry, name);
      }

      public Named(IRegistryHolder registryHolder, ClientVersion version, IRegistry<T> registry, ResourceLocation name) {
         this.registryHolder = new WeakReference<>(registryHolder);
         this.version = version;
         this.registry = registry;
         this.name = name;
      }

      @Override
      public T get() {
         T entity = this.entity;
         if (entity == null) {
            synchronized (this) {
               entity = this.entity;
               if (entity == null) {
                  IRegistryHolder registryHolder = this.registryHolder.get();
                  if (registryHolder == null) {
                     throw new IllegalStateException("Registry holder for " + this.registry + "/" + this.version + "/" + this.name + "has disappeared");
                  }

                  IRegistry<T> registry = registryHolder.getRegistryOr(this.registry, this.version);
                  entity = registry.getByNameOrThrow(this.version, this.name);
                  this.entity = entity;
               }
            }
         }

         return entity;
      }
   }

   @Internal
   public static final class Nbt<T extends MappedEntity> implements MappedEntityRef<T> {
      private final NBT tag;
      private final Supplier<T> supplier;

      public Nbt(NBT tag, Supplier<T> supplier) {
         this.tag = tag;
         this.supplier = supplier;
      }

      @Override
      public T get() {
         return this.supplier.get();
      }

      public NBT getTag() {
         return this.tag;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof MappedEntityRef) {
            obj = ((MappedEntityRef)obj).get();
         }

         return this.supplier.get().equals(obj);
      }

      @Override
      public int hashCode() {
         return this.supplier.get().hashCode();
      }

      @Override
      public String toString() {
         return "Ref[" + this.supplier.get() + ']';
      }
   }

   public static final class Static<T extends MappedEntity> implements MappedEntityRef<T> {
      private final T entity;

      public Static(T entity) {
         this.entity = entity;
      }

      @Override
      public T get() {
         return this.entity;
      }
   }
}

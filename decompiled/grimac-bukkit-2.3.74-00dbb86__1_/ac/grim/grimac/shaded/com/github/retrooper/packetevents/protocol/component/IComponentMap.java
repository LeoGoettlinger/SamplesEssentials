package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.Map.Entry;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IComponentMap {
   @ApiStatus.Internal
   static StaticComponentMap decode(NBT nbt, PacketWrapper<?> wrapper, IRegistry<? extends ComponentType<?>> registry) {
      ClientVersion version = wrapper.getServerVersion().toClientVersion();
      NBTCompound compound = (NBTCompound)nbt;
      StaticComponentMap.Builder components = StaticComponentMap.builder();

      for (Entry<String, NBT> entry : compound.getTags().entrySet()) {
         ComponentType<?> type = (ComponentType<?>)registry.getByName(entry.getKey());
         if (type == null) {
            throw new IllegalStateException("Unknown component type named " + entry.getKey() + " encountered");
         }

         Object value = type.decode(entry.getValue(), version);
         components.set(type, value);
      }

      return components.setRegistries(wrapper.getRegistryHolder()).build();
   }

   @ApiStatus.Internal
   static NBT encode(PacketWrapper<?> wrapper, StaticComponentMap components) {
      components = components.withRegistries(wrapper.getRegistryHolder());
      ClientVersion version = wrapper.getServerVersion().toClientVersion();
      NBTCompound compound = new NBTCompound();

      for (ComponentType<?> type : components.getKeys()) {
         String key = type.getName().toString();
         Object value = components.get(type);
         NBT tag = ((ComponentType<Object>)type).encode(value, version);
         compound.setTag(key, tag);
      }

      return compound;
   }

   default <T> Optional<T> getOptional(ComponentType<T> type) {
      return Optional.ofNullable(this.get(type));
   }

   boolean has(ComponentType<?> type);

   @Contract("_, !null -> !null")
   @Nullable
   default <T> T getOr(ComponentType<T> type, @Nullable T otherValue) {
      T value = this.get(type);
      return value != null ? value : otherValue;
   }

   @Nullable
   <T> T get(ComponentType<T> type);

   default <T> void set(ComponentValue<T> component) {
      this.set(component.getType(), component.getValue());
   }

   default <T> void set(ComponentType<T> type, @Nullable T value) {
      this.set(type, Optional.ofNullable(value));
   }

   default <T> void unset(ComponentType<T> type) {
      this.set(type, Optional.empty());
   }

   <T> void set(ComponentType<T> type, Optional<T> value);

   IComponentMap withRegistries(IRegistryHolder registries);
}

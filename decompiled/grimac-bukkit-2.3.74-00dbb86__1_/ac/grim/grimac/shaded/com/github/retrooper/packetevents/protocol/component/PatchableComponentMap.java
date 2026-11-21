package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.IRegistryHolder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PatchableComponentMap implements IComponentMap {
   public static final PatchableComponentMap EMPTY = new PatchableComponentMap(Collections.emptyMap(), Collections.emptyMap());
   private final Map<ComponentType<?>, ?> base;
   private final Map<ComponentType<?>, Optional<?>> patches;
   private final IRegistryHolder registries;

   public PatchableComponentMap(StaticComponentMap base) {
      this(base.delegate, new HashMap<>(), base.registries);
   }

   @Deprecated
   public PatchableComponentMap(Map<ComponentType<?>, ?> base) {
      this(base, new HashMap<>(), GlobalRegistryHolder.INSTANCE);
   }

   public PatchableComponentMap(StaticComponentMap base, Map<ComponentType<?>, Optional<?>> patches) {
      this(base.delegate, patches, base.registries);
   }

   @Deprecated
   public PatchableComponentMap(Map<ComponentType<?>, ?> base, Map<ComponentType<?>, Optional<?>> patches) {
      this(base, patches, GlobalRegistryHolder.INSTANCE);
   }

   public PatchableComponentMap(StaticComponentMap base, IRegistryHolder registries) {
      this(base.delegate, new HashMap<>(), registries);
   }

   public PatchableComponentMap(Map<ComponentType<?>, ?> base, IRegistryHolder registries) {
      this(base, new HashMap<>(), registries);
   }

   public PatchableComponentMap(StaticComponentMap base, Map<ComponentType<?>, Optional<?>> patches, IRegistryHolder registries) {
      this(base.delegate, patches, registries);
   }

   public PatchableComponentMap(Map<ComponentType<?>, ?> base, Map<ComponentType<?>, Optional<?>> patches, IRegistryHolder registries) {
      this.base = Collections.unmodifiableMap(new HashMap<>(base));
      this.patches = patches;
      this.registries = registries;
   }

   public static PatchableComponentMap read(PacketWrapper<?> wrapper, ItemType item, boolean lengthPrefixed) {
      return read(wrapper, item.getComponents(wrapper.getServerVersion().toClientVersion()), lengthPrefixed);
   }

   public static PatchableComponentMap read(PacketWrapper<?> wrapper, StaticComponentMap base, boolean lengthPrefixed) {
      int presentCount = wrapper.readVarInt();
      int absentCount = wrapper.readVarInt();
      if (presentCount == 0 && absentCount == 0) {
         return new PatchableComponentMap(base);
      } else {
         PatchableComponentMap components = new PatchableComponentMap(base, new HashMap<>(presentCount + absentCount), wrapper.getRegistryHolder());

         for (int i = 0; i < presentCount; i++) {
            ComponentType<?> type = wrapper.readMappedEntity(ComponentTypes.getRegistry());
            int expectedReaderIndex;
            if (lengthPrefixed) {
               int size = wrapper.readVarInt();
               if (size > ByteBufHelper.readableBytes(wrapper.buffer)) {
                  throw new RuntimeException("Component size " + size + " for " + type.getName() + " out of bounds");
               }

               expectedReaderIndex = ByteBufHelper.readerIndex(wrapper.buffer) + size;
            } else {
               expectedReaderIndex = -1;
            }

            Object value = type.read(wrapper);
            if (expectedReaderIndex != -1) {
               int readerIndex = ByteBufHelper.readerIndex(wrapper.buffer);
               if (readerIndex != expectedReaderIndex) {
                  throw new RuntimeException(
                     "Invalid component read for " + type.getName() + "; expected reader index " + expectedReaderIndex + ", got reader index " + readerIndex
                  );
               }
            }

            components.set(type, value);
         }

         for (int i = 0; i < absentCount; i++) {
            components.unset(wrapper.readMappedEntity(ComponentTypes.getRegistry()));
         }

         return components;
      }
   }

   public static void write(PacketWrapper<?> wrapper, ItemStack stack, boolean lengthPrefixed) {
      if (!stack.hasComponentPatches()) {
         wrapper.writeShort(0);
      } else {
         write(wrapper, stack.getComponents(), lengthPrefixed);
      }
   }

   public static void write(PacketWrapper<?> wrapper, PatchableComponentMap components, boolean lengthPrefixed) {
      Map<ComponentType<?>, Optional<?>> allPatches = components.getPatches();
      int presentCount = 0;
      int absentCount = 0;

      for (Entry<ComponentType<?>, Optional<?>> patch : allPatches.entrySet()) {
         if (patch.getValue().isPresent()) {
            presentCount++;
         } else {
            absentCount++;
         }
      }

      wrapper.writeVarInt(presentCount);
      wrapper.writeVarInt(absentCount);

      for (Entry<ComponentType<?>, Optional<?>> patchx : allPatches.entrySet()) {
         if (patchx.getValue().isPresent()) {
            wrapper.writeVarInt(patchx.getKey().getId(wrapper.getServerVersion().toClientVersion()));
            if (lengthPrefixed) {
               Object originalBuffer = wrapper.buffer;
               wrapper.buffer = ByteBufHelper.allocateNewBuffer(originalBuffer);
               ((ComponentType<Object>)patchx.getKey()).write(wrapper, patchx.getValue().get());
               Object componentBuffer = wrapper.buffer;
               wrapper.buffer = originalBuffer;
               wrapper.writeVarInt(ByteBufHelper.readableBytes(componentBuffer));
               ByteBufHelper.writeBytes(wrapper.buffer, componentBuffer);
               ByteBufHelper.release(componentBuffer);
            } else {
               ((ComponentType<Object>)patchx.getKey()).write(wrapper, patchx.getValue().get());
            }
         }
      }

      for (Entry<ComponentType<?>, Optional<?>> patchxx : allPatches.entrySet()) {
         if (!patchxx.getValue().isPresent()) {
            wrapper.writeVarInt(patchxx.getKey().getId(wrapper.getServerVersion().toClientVersion()));
         }
      }
   }

   @Nullable
   @Override
   public <T> T get(ComponentType<T> type) {
      Optional<?> patched = this.patches.get(type);
      Object v = patched != null ? patched.orElse(null) : this.base.get(type);
      if (v instanceof ComponentValueRef) {
         v = ((ComponentValueRef)v).resolve(this.registries);
      }

      return (T)v;
   }

   @Override
   public <T> void set(ComponentType<T> type, Optional<T> value) {
      Object baseVal = this.base.get(type);
      if (baseVal instanceof ComponentValueRef) {
         baseVal = ((ComponentValueRef)baseVal).resolve(this.registries);
      }

      T newVal = value.orElse(null);
      if (Objects.equals(baseVal, newVal)) {
         this.patches.remove(type);
      } else {
         this.patches.put(type, value);
      }
   }

   @Override
   public boolean has(ComponentType<?> type) {
      Optional<?> patched = this.patches.get(type);
      return patched != null ? patched.isPresent() : this.base.containsKey(type);
   }

   public PatchableComponentMap withRegistries(IRegistryHolder registries) {
      return this.registries != registries ? new PatchableComponentMap(this.base, this.patches, this.registries) : this;
   }

   public PatchableComponentMap copy() {
      return new PatchableComponentMap(this.base, new HashMap<>(this.patches), this.registries);
   }

   @Deprecated
   public Map<ComponentType<?>, ?> getBase() {
      return this.base;
   }

   @Deprecated
   public Map<ComponentType<?>, Optional<?>> getPatches() {
      return this.patches;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof PatchableComponentMap)) {
         return false;
      } else {
         PatchableComponentMap that = (PatchableComponentMap)obj;
         return !this.base.equals(that.base) ? false : this.patches.equals(that.patches);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.base, this.patches);
   }

   @Override
   public String toString() {
      return "PatchableComponentMap{base=" + this.base + ", patches=" + this.patches + '}';
   }
}

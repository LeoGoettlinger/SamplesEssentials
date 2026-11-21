package com.github.retrooper.packetevents.protocol.component;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class ComponentPredicate implements Predicate<IComponentMap> {
   private List<ComponentValue<?>> requiredComponents;

   public ComponentPredicate() {
      this(new ArrayList<>());
   }

   public ComponentPredicate(List<ComponentValue<?>> requiredComponents) {
      this.requiredComponents = requiredComponents;
   }

   public static ComponentPredicate read(PacketWrapper<?> wrapper) {
      List<ComponentValue<?>> components = wrapper.readList(ComponentValue::read);
      return new ComponentPredicate(components);
   }

   public static void write(PacketWrapper<?> wrapper, ComponentPredicate predicate) {
      wrapper.writeList(predicate.requiredComponents, ComponentValue::write);
   }

   public static ComponentPredicate emptyPredicate() {
      return new ComponentPredicate(new ArrayList<>());
   }

   public static ComponentPredicate fromPatches(PatchableComponentMap components) {
      Map<ComponentType<?>, Optional<?>> patches = components.getPatches();
      List<ComponentValue<?>> values = new ArrayList<>(patches.size());

      for (Entry<ComponentType<?>, Optional<?>> patch : patches.entrySet()) {
         if (patch.getValue().isPresent()) {
            values.add(new ComponentValue<>(patch.getKey(), patch.getValue().get()));
         }
      }

      return new ComponentPredicate(values);
   }

   public PatchableComponentMap asPatches(StaticComponentMap base) {
      PatchableComponentMap patched = new PatchableComponentMap(base);

      for (ComponentValue<?> component : this.requiredComponents) {
         patched.set(component);
      }

      return patched;
   }

   public boolean test(IComponentMap components) {
      for (ComponentValue<?> component : this.requiredComponents) {
         Optional<?> value = components.getOptional(component.getType());
         if (!value.isPresent() || !component.getValue().equals(value.get())) {
            return false;
         }
      }

      return true;
   }

   public List<ComponentValue<?>> getRequiredComponents() {
      return this.requiredComponents;
   }

   public void setRequiredComponents(List<ComponentValue<?>> requiredComponents) {
      this.requiredComponents = requiredComponents;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ComponentPredicate)) {
         return false;
      } else {
         ComponentPredicate that = (ComponentPredicate)obj;
         return this.requiredComponents.equals(that.requiredComponents);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.requiredComponents);
   }

   @Override
   public String toString() {
      return "ComponentPredicate{requiredComponents=" + this.requiredComponents + '}';
   }
}

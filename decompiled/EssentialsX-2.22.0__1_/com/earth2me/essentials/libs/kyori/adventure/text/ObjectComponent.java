package com.earth2me.essentials.libs.kyori.adventure.text;

import com.earth2me.essentials.libs.kyori.adventure.text.object.ObjectContents;
import com.earth2me.essentials.libs.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface ObjectComponent extends BuildableComponent<ObjectComponent, ObjectComponent.Builder>, ScopedComponent<ObjectComponent> {
   @NotNull
   ObjectContents contents();

   @NotNull
   ObjectComponent contents(@NotNull ObjectContents contents);

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.concat(Stream.of(ExaminableProperty.of("contents", this.contents())), BuildableComponent.super.examinableProperties());
   }

   public interface Builder extends ComponentBuilder<ObjectComponent, ObjectComponent.Builder> {
      @NotNull
      ObjectComponent.Builder contents(@NotNull ObjectContents objectContents);
   }
}

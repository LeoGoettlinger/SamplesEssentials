package xyz.lychee.lagfixer.libs.kyori.adventure.text;

import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.object.ObjectContents;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

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

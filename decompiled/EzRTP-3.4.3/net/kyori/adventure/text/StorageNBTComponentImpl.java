package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.Style;
import org.jspecify.annotations.Nullable;

record StorageNBTComponentImpl(
   List<Component> children, Style style, String nbtPath, boolean interpret, @Nullable Component separator, Key storage, boolean plain
) implements StorageNBTComponent {
   static StorageNBTComponent create(
      final List<? extends ComponentLike> children,
      final Style style,
      final String nbtPath,
      final boolean interpret,
      @Nullable final ComponentLike separator,
      final Key storage,
      final boolean plain
   ) {
      return new StorageNBTComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(nbtPath, "nbtPath"),
         interpret,
         ComponentLike.unbox(separator),
         Objects.requireNonNull(storage, "storage"),
         plain
      );
   }

   public StorageNBTComponent nbtPath(final String nbtPath) {
      return (StorageNBTComponent)(Objects.equals(this.nbtPath, nbtPath)
         ? this
         : create(this.children, this.style, nbtPath, this.interpret, this.separator, this.storage, this.plain));
   }

   public StorageNBTComponent interpret(final boolean interpret) {
      if (this.interpret == interpret) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(interpret, this.plain);
         return create(this.children, this.style, this.nbtPath, interpret, this.separator, this.storage, this.plain);
      }
   }

   public StorageNBTComponent plain(final boolean plain) {
      if (this.plain == plain) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(this.interpret, plain);
         return create(this.children, this.style, this.nbtPath, this.interpret, this.separator, this.storage, plain);
      }
   }

   public StorageNBTComponent separator(@Nullable final ComponentLike separator) {
      return create(this.children, this.style, this.nbtPath, this.interpret, separator, this.storage, this.plain);
   }

   @Override
   public StorageNBTComponent storage(final Key storage) {
      return (StorageNBTComponent)(Objects.equals(this.storage, storage)
         ? this
         : create(this.children, this.style, this.nbtPath, this.interpret, this.separator, storage, this.plain));
   }

   public StorageNBTComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.nbtPath, this.interpret, this.separator, this.storage, this.plain);
   }

   public StorageNBTComponent style(final Style style) {
      return create(this.children, style, this.nbtPath, this.interpret, this.separator, this.storage, this.plain);
   }

   @Override
   public StorageNBTComponent.Builder toBuilder() {
      return new StorageNBTComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractNBTComponentBuilder<StorageNBTComponent, StorageNBTComponent.Builder> implements StorageNBTComponent.Builder {
      @Nullable
      private Key storage;

      BuilderImpl() {
      }

      BuilderImpl(final StorageNBTComponent component) {
         super(component);
         this.storage = component.storage();
      }

      @Override
      public StorageNBTComponent.Builder storage(final Key storage) {
         this.storage = Objects.requireNonNull(storage, "storage");
         return this;
      }

      public StorageNBTComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.storage == null) {
            throw new IllegalStateException("storage must be set");
         } else {
            return StorageNBTComponentImpl.create(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.separator, this.storage, this.plain);
         }
      }
   }
}

package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

abstract sealed class AbstractNBTComponentBuilder<C extends NBTComponent<C>, B extends NBTComponentBuilder<C, B>>
   extends AbstractComponentBuilder<C, B>
   implements NBTComponentBuilder<C, B>
   permits BlockNBTComponentImpl.BuilderImpl,
   EntityNBTComponentImpl.BuilderImpl,
   StorageNBTComponentImpl.BuilderImpl {
   @Nullable
   protected String nbtPath;
   protected boolean interpret = false;
   @Nullable
   protected Component separator;
   protected boolean plain;

   AbstractNBTComponentBuilder() {
   }

   AbstractNBTComponentBuilder(final C component) {
      super(component);
      this.nbtPath = component.nbtPath();
      this.interpret = component.interpret();
      this.separator = component.separator();
      this.plain = component.plain();
   }

   @Override
   public B nbtPath(final String nbtPath) {
      this.nbtPath = Objects.requireNonNull(nbtPath, "nbtPath");
      return (B)this;
   }

   @Override
   public B interpret(final boolean interpret) {
      if (this.interpret == interpret) {
         return (B)this;
      } else {
         checkInterpretPlainState(interpret, this.plain);
         this.interpret = interpret;
         return (B)this;
      }
   }

   @Override
   public B plain(final boolean plain) {
      if (this.plain == plain) {
         return (B)this;
      } else {
         checkInterpretPlainState(this.interpret, plain);
         this.plain = plain;
         return (B)this;
      }
   }

   @Override
   public B separator(@Nullable final ComponentLike separator) {
      this.separator = ComponentLike.unbox(separator);
      return (B)this;
   }

   static void checkInterpretPlainState(final boolean interpret, final boolean plain) {
      if (interpret && plain) {
         throw new IllegalArgumentException("Cannot have `interpret` and `plain` set to `true` at the same time");
      }
   }
}

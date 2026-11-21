package net.kyori.adventure.text.minimessage.tag.standard;

import java.util.Collections;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.kyori.adventure.internal.Internals;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.VirtualComponentRenderer;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

abstract class AbstractColorChangingTag implements Modifying, Examinable {
   private static final ComponentFlattener LENGTH_CALCULATOR = ComponentFlattener.builder()
      .mapper(TextComponent.class, TextComponent::content)
      .unknownMapper(x -> "_")
      .build();
   private boolean visited;
   private int size = 0;
   private int disableApplyingColorDepth = -1;
   private final boolean emitVirtuals;

   AbstractColorChangingTag(final Context ctx) {
      this.emitVirtuals = ctx.emitVirtuals();
   }

   protected final int size() {
      return this.size;
   }

   @Override
   public final void visit(@NotNull final Node current, final int depth) {
      if (this.visited) {
         throw new IllegalStateException("Color changing tag instances cannot be re-used, return a new one for each resolve");
      } else {
         if (current instanceof ValueNode) {
            String value = ((ValueNode)current).value();
            this.size = this.size + value.codePointCount(0, value.length());
         } else if (current instanceof TagNode) {
            TagNode tag = (TagNode)current;
            if (tag.tag() instanceof Inserting) {
               LENGTH_CALCULATOR.flatten(((Inserting)tag.tag()).value(), s -> this.size = this.size + s.codePointCount(0, s.length()));
            }
         }
      }
   }

   @Override
   public final void postVisit() {
      this.visited = true;
      this.init();
   }

   @Override
   public final Component apply(@NotNull final Component current, final int depth) {
      if (this.emitVirtuals && depth == 0) {
         return Component.virtual(Void.class, new AbstractColorChangingTag.TagInfoHolder(this.preserveData(), current), current.style());
      } else if ((this.disableApplyingColorDepth == -1 || depth <= this.disableApplyingColorDepth) && current.style().color() == null) {
         this.disableApplyingColorDepth = -1;
         if (current instanceof VirtualComponent) {
            this.skipColorForLengthOf(((VirtualComponent)current).content());
            return current.children(Collections.emptyList());
         } else if (current instanceof TextComponent && ((TextComponent)current).content().length() > 0) {
            TextComponent textComponent = (TextComponent)current;
            String content = textComponent.content();
            TextComponent.Builder parent = Component.text();
            int[] holder = new int[1];
            OfInt it = content.codePoints().iterator();

            while (it.hasNext()) {
               holder[0] = it.nextInt();
               Component comp = Component.text(new String(holder, 0, 1), current.style().color(this.color()));
               this.advanceColor();
               parent.append(comp);
            }

            return parent.build();
         } else if (!(current instanceof TextComponent)) {
            Component ret = current.children(Collections.emptyList()).colorIfAbsent(this.color());
            this.advanceColor();
            return ret;
         } else {
            return Component.empty().mergeStyle(current);
         }
      } else {
         if (this.disableApplyingColorDepth == -1 || depth < this.disableApplyingColorDepth) {
            this.disableApplyingColorDepth = depth;
         }

         if (current instanceof TextComponent) {
            this.skipColorForLengthOf(((TextComponent)current).content());
         }

         return current.children(Collections.emptyList());
      }
   }

   private void skipColorForLengthOf(final String content) {
      int len = content.codePointCount(0, content.length());

      for (int i = 0; i < len; i++) {
         this.advanceColor();
      }
   }

   protected abstract void init();

   protected abstract void advanceColor();

   protected abstract TextColor color();

   @NotNull
   protected abstract Consumer<TokenEmitter> preserveData();

   @NotNull
   @Override
   public abstract Stream<? extends ExaminableProperty> examinableProperties();

   @NotNull
   @Override
   public final String toString() {
      return Internals.toString(this);
   }

   @Override
   public abstract boolean equals(@Nullable final Object other);

   @Override
   public abstract int hashCode();

   @Nullable
   static Emitable claimComponent(final Component comp) {
      if (!(comp instanceof VirtualComponent)) {
         return null;
      } else {
         VirtualComponentRenderer<?> holder = ((VirtualComponent)comp).renderer();
         return !(holder instanceof AbstractColorChangingTag.TagInfoHolder) ? null : (AbstractColorChangingTag.TagInfoHolder)holder;
      }
   }

   static final class TagInfoHolder implements VirtualComponentRenderer<Void>, Emitable {
      private final Consumer<TokenEmitter> output;
      private final Component originalComp;

      TagInfoHolder(final Consumer<TokenEmitter> output, final Component originalComp) {
         this.output = output;
         this.originalComp = originalComp;
      }

      @UnknownNullability
      public ComponentLike apply(@NotNull final Void context) {
         return this.originalComp;
      }

      @NotNull
      @Override
      public String fallbackString() {
         return "";
      }

      @Override
      public void emit(@NotNull final TokenEmitter emitter) {
         this.output.accept(emitter);
      }

      @Nullable
      @Override
      public Component substitute() {
         return this.originalComp;
      }
   }
}

package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ScopedComponent<C extends Component> extends Component {
   @NonNull
   @Override
   C children(@NonNull final List<Component> children);

   @NonNull
   @Override
   C style(@NonNull final Style style);

   @NonNull
   @Override
   default C style(@NonNull final Consumer<Style.Builder> style) {
      return (C)Component.super.style(style);
   }

   @NonNull
   @Override
   default C style(@NonNull final Style.Builder style) {
      return (C)Component.super.style(style);
   }

   @NonNull
   @Override
   default C mergeStyle(@NonNull final Component that) {
      return (C)Component.super.mergeStyle(that);
   }

   @NonNull
   @Override
   default C mergeStyle(@NonNull final Component that, @NonNull final Style.Merge... merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @NonNull
   @Override
   default C append(@NonNull final Component component) {
      this.detectCycle(component);
      List<Component> oldChildren = this.children();
      List<Component> newChildren = new ArrayList<>(oldChildren.size() + 1);
      newChildren.addAll(oldChildren);
      newChildren.add(component);
      return this.children(newChildren);
   }

   @NonNull
   @Override
   default C append(@NonNull final ComponentBuilder<?, ?> builder) {
      return (C)Component.super.append(builder);
   }

   @NonNull
   @Override
   default C mergeStyle(@NonNull final Component that, @NonNull final Set<Style.Merge> merges) {
      return (C)Component.super.mergeStyle(that, merges);
   }

   @NonNull
   @Override
   default C color(@Nullable final TextColor color) {
      return (C)Component.super.color(color);
   }

   @NonNull
   @Override
   default C colorIfAbsent(@Nullable final TextColor color) {
      return (C)Component.super.colorIfAbsent(color);
   }

   @NonNull
   @Override
   default C decoration(@NonNull final TextDecoration decoration, final boolean flag) {
      return (C)Component.super.decoration(decoration, flag);
   }

   @NonNull
   @Override
   default C decoration(@NonNull final TextDecoration decoration, @NonNull final TextDecoration.State state) {
      return (C)Component.super.decoration(decoration, state);
   }

   @NonNull
   @Override
   default C clickEvent(@Nullable final ClickEvent event) {
      return (C)Component.super.clickEvent(event);
   }

   @NonNull
   @Override
   default C hoverEvent(@Nullable final HoverEvent event) {
      return (C)Component.super.hoverEvent(event);
   }

   @NonNull
   @Override
   default C insertion(@Nullable final String insertion) {
      return (C)Component.super.insertion(insertion);
   }

   @Deprecated
   @NonNull
   @Override
   default C mergeColor(@NonNull final Component that) {
      return (C)Component.super.mergeColor(that);
   }

   @Deprecated
   @NonNull
   @Override
   default C mergeDecorations(@NonNull final Component that) {
      return (C)Component.super.mergeDecorations(that);
   }

   @Deprecated
   @NonNull
   @Override
   default C mergeEvents(@NonNull final Component that) {
      return (C)Component.super.mergeEvents(that);
   }
}

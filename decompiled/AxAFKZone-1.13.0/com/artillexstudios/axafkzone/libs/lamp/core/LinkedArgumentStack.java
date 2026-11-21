package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

public final class LinkedArgumentStack extends LinkedList<String> implements ArgumentStack {
   private final List<String> unmodifiableView = Collections.unmodifiableList(this);

   public LinkedArgumentStack(@NotNull Collection<? extends String> c) {
      super(c);
   }

   public LinkedArgumentStack(@NotNull String... c) {
      Collections.addAll(this, c);
   }

   @NotNull
   @Override
   public String join(String delimiter) {
      return String.join(delimiter, this);
   }

   @NotNull
   @Override
   public String join(@NotNull String delimiter, int startIndex) {
      StringJoiner joiner = new StringJoiner(delimiter);

      for (int i = startIndex; i < this.size(); i++) {
         joiner.add(this.get(i));
      }

      return joiner.toString();
   }

   @NotNull
   @Override
   public String popForParameter(@NotNull CommandParameter parameter) {
      if (parameter.consumesAllString()) {
         String value = this.join(" ");
         this.clear();
         return value;
      } else {
         return this.pop();
      }
   }

   @NotNull
   @UnmodifiableView
   @Override
   public List<String> asImmutableView() {
      return this.unmodifiableView;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> asImmutableCopy() {
      return Collections.unmodifiableList(new ArrayList<>(this));
   }

   @NotNull
   @Override
   public ArgumentStack copy() {
      return new LinkedArgumentStack(this);
   }
}

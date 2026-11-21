package org.skriptlang.skript.lang.converter;

import org.jetbrains.annotations.Nullable;

final class ChainedConverter<F, M, T> implements Converter<F, T> {
   private final ConverterInfo<F, M> first;
   private final ConverterInfo<M, T> second;

   ChainedConverter(ConverterInfo<F, M> first, ConverterInfo<M, T> second) {
      this.first = first;
      this.second = second;
   }

   @Nullable
   @Override
   public T convert(F from) {
      M middle = this.first.getConverter().convert(from);
      return middle == null ? null : this.second.getConverter().convert(middle);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ChainedConverter{(");
      if (this.first.getConverter() instanceof ChainedConverter) {
         builder.append(this.first.getConverter());
      } else {
         builder.append(this.first.getFrom()).append(" -> ").append(this.first.getTo());
      }

      builder.append(") -> (");
      if (this.second.getConverter() instanceof ChainedConverter) {
         builder.append(this.second.getConverter());
      } else {
         builder.append(this.second.getFrom()).append(" -> ").append(this.second.getTo());
      }

      builder.append(")}");
      return builder.toString();
   }
}

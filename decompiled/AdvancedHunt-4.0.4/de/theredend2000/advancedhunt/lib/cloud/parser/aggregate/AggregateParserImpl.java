package de.theredend2000.advancedhunt.lib.cloud.parser.aggregate;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

final class AggregateParserImpl<C, O> implements AggregateParser<C, O> {
   private final List<CommandComponent<C>> components;
   private final TypeToken<O> valueType;
   private final AggregateResultMapper<C, O> mapper;

   AggregateParserImpl(
      @NonNull final List<CommandComponent<C>> components, @NonNull final TypeToken<O> valueType, @NonNull final AggregateResultMapper<C, O> mapper
   ) {
      this.components = components;
      this.valueType = valueType;
      this.mapper = mapper;
   }

   @NonNull
   @Override
   public List<CommandComponent<C>> components() {
      return Collections.unmodifiableList(this.components);
   }

   @NonNull
   @Override
   public AggregateResultMapper<C, O> mapper() {
      return this.mapper;
   }

   @NonNull
   @Override
   public TypeToken<O> valueType() {
      return this.valueType;
   }
}

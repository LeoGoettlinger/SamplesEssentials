package de.oliver.cloud.exception.parsing;

import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.caption.StandardCaptionKeys;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.parser.standard.NumberParser;
import de.oliver.cloud.type.range.Range;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public abstract class NumberParseException extends ParserException {
   private final String input;
   private final NumberParser<?, ?, ?> parser;

   protected NumberParseException(@NonNull final String input, @NonNull final NumberParser<?, ?, ?> parser, @NonNull final CommandContext<?> context) {
      super(
         parser.getClass(),
         context,
         StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
         CaptionVariable.of("input", input),
         CaptionVariable.of("min", String.valueOf(parser.range().min())),
         CaptionVariable.of("max", String.valueOf(parser.range().max()))
      );
      this.input = input;
      this.parser = parser;
   }

   @NonNull
   public abstract String numberType();

   @NonNull
   public final NumberParser<?, ?, ?> parser() {
      return this.parser;
   }

   public final boolean hasMax() {
      return this.parser.hasMax();
   }

   public final boolean hasMin() {
      return this.parser.hasMax();
   }

   @NonNull
   public String input() {
      return this.input;
   }

   @NonNull
   public final Range<? extends Number> range() {
      return (Range<? extends Number>)this.parser.range();
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NumberParseException that = (NumberParseException)o;
         return this.parser().equals(that.parser());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.parser());
   }
}

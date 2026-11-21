package de.oliver.cloud.exception.parsing;

import de.oliver.cloud.caption.Caption;
import de.oliver.cloud.caption.CaptionFormatter;
import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.context.CommandContext;
import java.util.Arrays;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.STABLE
)
public class ParserException extends IllegalArgumentException {
   private final Class<?> argumentParser;
   private final CommandContext<?> context;
   private final Caption errorCaption;
   private final CaptionVariable[] captionVariables;

   protected ParserException(
      @Nullable final Throwable cause,
      @NonNull final Class<?> argumentParser,
      @NonNull final CommandContext<?> context,
      @NonNull final Caption errorCaption,
      final CaptionVariable... captionVariables
   ) {
      super(cause);
      this.argumentParser = argumentParser;
      this.context = context;
      this.errorCaption = errorCaption;
      this.captionVariables = captionVariables;
   }

   protected ParserException(
      @NonNull final Class<?> argumentParser,
      @NonNull final CommandContext<?> context,
      @NonNull final Caption errorCaption,
      final CaptionVariable... captionVariables
   ) {
      this(null, argumentParser, context, errorCaption, captionVariables);
   }

   @Override
   public final String getMessage() {
      return this.context.formatCaption(this.errorCaption, this.captionVariables);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public final <T> T formatCaption(@NonNull final CaptionFormatter<?, T> formatter) {
      return this.context.formatCaption(formatter, this.errorCaption, this.captionVariables());
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Caption errorCaption() {
      return this.errorCaption;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CaptionVariable[] captionVariables() {
      return Arrays.copyOf(this.captionVariables, this.captionVariables.length);
   }

   @NonNull
   public final Class<?> argumentParserClass() {
      return this.argumentParser;
   }

   @NonNull
   public final CommandContext<?> context() {
      return this.context;
   }
}

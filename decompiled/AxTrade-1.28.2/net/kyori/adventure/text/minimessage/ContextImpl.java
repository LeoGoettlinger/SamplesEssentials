package net.kyori.adventure.text.minimessage;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.internal.parser.ParsingExceptionImpl;
import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ContextImpl implements Context {
   private static final Token[] EMPTY_TOKEN_ARRAY = new Token[0];
   private final boolean strict;
   private final Consumer<String> debugOutput;
   private String message;
   private final MiniMessage miniMessage;
   private final TagResolver tagResolver;
   private final UnaryOperator<String> preProcessor;
   private final UnaryOperator<Component> postProcessor;

   ContextImpl(
      final boolean strict,
      final Consumer<String> debugOutput,
      final String message,
      final MiniMessage miniMessage,
      @NotNull final TagResolver extraTags,
      final UnaryOperator<String> preProcessor,
      final UnaryOperator<Component> postProcessor
   ) {
      this.strict = strict;
      this.debugOutput = debugOutput;
      this.message = message;
      this.miniMessage = miniMessage;
      this.tagResolver = extraTags;
      this.preProcessor = preProcessor == null ? UnaryOperator.identity() : preProcessor;
      this.postProcessor = postProcessor == null ? UnaryOperator.identity() : postProcessor;
   }

   static ContextImpl of(
      final boolean strict,
      final Consumer<String> debugOutput,
      final String input,
      final MiniMessageImpl miniMessage,
      final TagResolver extraTags,
      final UnaryOperator<String> preProcessor,
      final UnaryOperator<Component> postProcessor
   ) {
      return new ContextImpl(strict, debugOutput, input, miniMessage, extraTags, preProcessor, postProcessor);
   }

   public boolean strict() {
      return this.strict;
   }

   public Consumer<String> debugOutput() {
      return this.debugOutput;
   }

   @NotNull
   public String message() {
      return this.message;
   }

   void message(@NotNull final String message) {
      this.message = message;
   }

   @NotNull
   public TagResolver extraTags() {
      return this.tagResolver;
   }

   public UnaryOperator<Component> postProcessor() {
      return this.postProcessor;
   }

   public UnaryOperator<String> preProcessor() {
      return this.preProcessor;
   }

   @NotNull
   @Override
   public Component deserialize(@NotNull final String message) {
      return this.miniMessage.deserialize(Objects.requireNonNull(message, "message"), this.tagResolver);
   }

   @NotNull
   @Override
   public Component deserialize(@NotNull final String message, @NotNull final TagResolver resolver) {
      return this.miniMessage
         .deserialize(
            Objects.requireNonNull(message, "message"),
            TagResolver.builder().resolver(this.tagResolver).resolver(Objects.requireNonNull(resolver, "resolver")).build()
         );
   }

   @NotNull
   @Override
   public Component deserialize(@NotNull final String message, @NotNull final TagResolver... resolvers) {
      return this.miniMessage
         .deserialize(
            Objects.requireNonNull(message, "message"),
            TagResolver.builder().resolver(this.tagResolver).resolvers(Objects.requireNonNull(resolvers, "resolvers")).build()
         );
   }

   @NotNull
   @Override
   public ParsingException newException(@NotNull final String message) {
      return new ParsingExceptionImpl(message, this.message, null, false, EMPTY_TOKEN_ARRAY);
   }

   @NotNull
   @Override
   public ParsingException newException(@NotNull final String message, @NotNull final ArgumentQueue tags) {
      return new ParsingExceptionImpl(message, this.message, null, false, tagsToTokens(((ArgumentQueueImpl)tags).args));
   }

   @NotNull
   @Override
   public ParsingException newException(@NotNull final String message, @Nullable final Throwable cause, @NotNull final ArgumentQueue tags) {
      return new ParsingExceptionImpl(message, this.message, cause, false, tagsToTokens(((ArgumentQueueImpl)tags).args));
   }

   private static Token[] tagsToTokens(final List<? extends Tag.Argument> tags) {
      Token[] tokens = new Token[tags.size()];
      int i = 0;

      for (int length = tokens.length; i < length; i++) {
         tokens[i] = ((TagPart)tags.get(i)).token();
      }

      return tokens;
   }
}

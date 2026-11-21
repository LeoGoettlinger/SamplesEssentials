package ch.njol.skript.lang;

import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.LiteralUtils;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public interface InputSource {
   Set<ExprInput<?>> getDependentInputs();

   @Nullable
   Object getCurrentValue();

   default boolean hasIndices() {
      return false;
   }

   @UnknownNullability
   default String getCurrentIndex() {
      return null;
   }

   @Nullable
   default Expression<?> parseExpression(String expr, ParserInstance parser, int flags) {
      InputSource.InputData inputData = parser.getData(InputSource.InputData.class);
      InputSource originalSource = inputData.getSource();
      inputData.setSource(this);
      Expression<?> mappingExpr = new SkriptParser(expr, flags, ParseContext.DEFAULT).parseExpression(Object.class);
      if (mappingExpr != null && LiteralUtils.hasUnparsedLiteral(mappingExpr)) {
         mappingExpr = LiteralUtils.defendExpression(mappingExpr);
         if (!LiteralUtils.canInitSafely(mappingExpr)) {
            return null;
         }
      }

      inputData.setSource(originalSource);
      return mappingExpr;
   }

   public static class InputData extends ParserInstance.Data {
      @Nullable
      private InputSource source;

      public InputData(ParserInstance parserInstance) {
         super(parserInstance);
      }

      public void setSource(@Nullable InputSource source) {
         this.source = source;
      }

      @Nullable
      public InputSource getSource() {
         return this.source;
      }
   }
}

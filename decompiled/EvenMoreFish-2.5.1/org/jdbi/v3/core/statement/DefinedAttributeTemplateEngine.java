package org.jdbi.v3.core.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$CharStreams;
import org.jdbi.v3.core.inlined.org.antlr.v4.runtime.$Token;
import org.jdbi.v3.core.internal.lexer.DefineStatementLexer;
import org.jdbi.v3.core.statement.internal.ErrorListener;

public class DefinedAttributeTemplateEngine implements TemplateEngine.Parsing {
   @Override
   public Optional<Function<StatementContext, String>> parse(String template, ConfigRegistry config) {
      StringBuilder buf = new StringBuilder();
      List<BiConsumer<StatementContext, StringBuilder>> preparation = new ArrayList<>();
      Runnable pushBuf = () -> {
         if (buf.length() > 0) {
            String bit = buf.toString();
            buf.setLength(0);
            preparation.add((ctx, b) -> b.append(bit));
         }
      };
      DefineStatementLexer lexer = new DefineStatementLexer($CharStreams.fromString(template));
      lexer.addErrorListener(new ErrorListener());

      for ($Token t = lexer.nextToken(); t.getType() != -1; t = lexer.nextToken()) {
         switch (t.getType()) {
            case 1:
            case 2:
            case 3:
            case 6:
               buf.append(t.getText());
               break;
            case 4:
               buf.append(t.getText().substring(1));
               break;
            case 5:
               pushBuf.run();
               String text = t.getText();
               String key = text.substring(1, text.length() - 1);
               preparation.add((ctx, b) -> {
                  Object value = ctx.getAttribute(key);
                  if (value == null) {
                     throw new UnableToCreateStatementException("Undefined attribute for token '" + text + "'", ctx);
                  } else {
                     b.append(value);
                  }
               });
         }
      }

      pushBuf.run();
      return Optional.of(ctx -> {
         try {
            StringBuilder result = new StringBuilder();
            preparation.forEach(a -> a.accept(ctx, result));
            return result.toString();
         } catch (RuntimeException var4x) {
            throw new UnableToCreateStatementException("Error rendering SQL template: '" + template + "'", var4x, ctx);
         }
      });
   }
}

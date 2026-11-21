package me.byteful.plugin.leveltools.libs.crunch;

import me.byteful.plugin.leveltools.libs.crunch.data.FastNumberParsing;
import me.byteful.plugin.leveltools.libs.crunch.exceptions.ExpressionCompilationException;
import me.byteful.plugin.leveltools.libs.crunch.functional.ArgumentList;
import me.byteful.plugin.leveltools.libs.crunch.functional.ExpressionEnv;
import me.byteful.plugin.leveltools.libs.crunch.functional.Function;
import me.byteful.plugin.leveltools.libs.crunch.functional.FunctionCall;
import me.byteful.plugin.leveltools.libs.crunch.token.BinaryOperator;
import me.byteful.plugin.leveltools.libs.crunch.token.LiteralValue;
import me.byteful.plugin.leveltools.libs.crunch.token.Token;
import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;
import me.byteful.plugin.leveltools.libs.crunch.token.UnaryOperation;
import me.byteful.plugin.leveltools.libs.crunch.token.UnaryOperator;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class ExpressionParser {
   private final String input;
   private final ExpressionEnv environment;
   private final CompiledExpression expression = new CompiledExpression();
   private int maxVarIndex;
   private int cursor = 0;

   ExpressionParser(String input, ExpressionEnv env) {
      if (input == null) {
         throw new ExpressionCompilationException(null, "Expression is null");
      } else if (env == null) {
         throw new ExpressionCompilationException(null, "Environment is null");
      } else {
         this.maxVarIndex = env.getVariableCount() - 1;
         this.input = input;
         this.environment = env;
      }
   }

   public char peek() {
      return this.input.charAt(this.cursor);
   }

   public char advance() {
      return this.input.charAt(this.cursor++);
   }

   public void advanceCursor() {
      this.cursor++;
   }

   public boolean isAtEnd() {
      return this.cursor >= this.input.length();
   }

   public int getCursor() {
      return this.cursor;
   }

   public void setCursor(int cursor) {
      this.cursor = cursor;
   }

   public String getInput() {
      return this.input;
   }

   public void expectChar(char c) {
      if (this.isAtEnd() || this.advance() != c) {
         throw new ExpressionCompilationException(this, "Expected '" + c + "'");
      }
   }

   private void error(String msg) {
      throw new ExpressionCompilationException(this, msg);
   }

   private boolean whitespace() {
      while (!this.isAtEnd() && Character.isWhitespace(this.peek())) {
         this.cursor++;
      }

      return true;
   }

   private Value parseExpression() {
      if (this.isAtEnd()) {
         this.error("Expected expression");
      }

      Value first = this.parseTerm();
      if (!this.isAtEnd() && this.peek() != ')' && this.peek() != ',') {
         ShuntingYard tokens = new ShuntingYard();
         tokens.addValue(first);

         while (this.whitespace() && !this.isAtEnd() && this.peek() != ')' && this.peek() != ',') {
            BinaryOperator token = this.environment.getBinaryOperators().getWith(this);
            if (token == null) {
               this.error("Expected binary operator");
            }

            tokens.addOperator(token);
            this.whitespace();
            tokens.addValue(this.parseTerm());
         }

         return tokens.finish();
      } else {
         return first;
      }
   }

   private Value parseNestedExpression() {
      this.expectChar('(');
      this.whitespace();
      Value expression = this.parseExpression();
      this.expectChar(')');
      return expression;
   }

   private Value parseAnonymousVariable() {
      this.expectChar('$');
      double value = this.parseLiteral().getValue(new double[0]);
      if (value % 1.0 != 0.0) {
         this.error("Decimal variable indices are not allowed");
      }

      if (value < 1.0) {
         this.error("Zero and negative variable indices are not allowed");
      }

      int index = (int)value - 1;
      this.maxVarIndex = Math.max(index, this.maxVarIndex);
      return new Variable(index);
   }

   private Value parseTerm() {
      switch (this.peek()) {
         case '$':
            return this.parseAnonymousVariable();
         case '%':
         case '&':
         case '\'':
         case ')':
         case '*':
         case '+':
         case ',':
         case '-':
         case '/':
         default:
            Token leadingOperator = this.environment.getLeadingOperators().getWith(this);
            if (leadingOperator != null) {
               return this.parseLeadingOperation(leadingOperator);
            }

            Value term = this.environment.getValues().getWith(this);
            if (term == null) {
               this.error("Expected value");
            }

            return term;
         case '(':
            return this.parseNestedExpression();
         case '.':
         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
            return this.parseLiteral();
      }
   }

   private LiteralValue parseLiteral() {
      int start = this.cursor;

      char c;
      while (Character.isDigit(c = this.peek()) || c == '.') {
         this.advanceCursor();
         if (this.isAtEnd()) {
            break;
         }
      }

      return new LiteralValue(FastNumberParsing.parseDouble(this.input, start, this.cursor));
   }

   private Value parseLeadingOperation(Token token) {
      this.whitespace();
      switch (token.getType()) {
         case UNARY_OPERATOR:
            UnaryOperator op = (UnaryOperator)token;
            Value term = this.parseTerm();
            if (op.isPure() && term.getType() == TokenType.LITERAL_VALUE) {
               return new LiteralValue(op.getOperation().applyAsDouble(term.getValue(new double[0])));
            }

            return new UnaryOperation(op, term);
         case FUNCTION:
            Function function = (Function)token;
            ArgumentList args = this.parseArgumentList(function.getArgCount());
            return new FunctionCall(function, args.getArguments());
         default:
            this.error("Expected leading operation");
            return null;
      }
   }

   private ArgumentList parseArgumentList(int args) {
      this.expectChar('(');
      this.whitespace();
      Value[] values = new Value[args];
      if (args == 0) {
         this.expectChar(')');
         return new ArgumentList(new Value[0]);
      } else {
         values[0] = this.parseExpression();
         this.whitespace();

         for (int i = 1; i < args; i++) {
            this.expectChar(',');
            this.whitespace();
            values[i] = this.parseExpression();
            this.whitespace();
         }

         this.expectChar(')');
         return new ArgumentList(values);
      }
   }

   public CompiledExpression parse() {
      this.whitespace();
      Value value = this.parseExpression();
      this.whitespace();
      if (!this.isAtEnd()) {
         this.error("Dangling term");
      }

      this.expression.initialize(value, this.maxVarIndex + 1);
      return this.expression;
   }
}

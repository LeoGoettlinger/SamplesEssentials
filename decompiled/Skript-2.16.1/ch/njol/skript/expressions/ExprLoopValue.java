package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.KeyedIterableExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Loop value")
@Description({"Returns the previous, current, or next looped value."})
@Example.Examples({@Example("# Countdown\nloop 10 times:\n\tmessage \"%11 - loop-number%\"\n\twait a second\n"), @Example("# Generate a 10x10 floor made of randomly colored wool below the player\nloop blocks from the block below the player to the block 10 east of the block below the player:\n\tloop blocks from the loop-block to the block 10 north of the loop-block:\n\t\tset loop-block-2 to any wool\n"), @Example("loop {top-balances::*}:\n\tloop-iteration <= 10\n\tsend \"#%loop-iteration% %loop-index% has $%loop-value%\"\n"), @Example("loop shuffled (integers between 0 and 8):\n\tif all:\n\t\tprevious loop-value = 1\n\t\tloop-value = 4\n\t\tnext loop-value = 8\n\tthen:\n\t\t kill all players\n")})
@Since({"1.0, 2.8.0 (loop-counter), 2.10 (previous, next)"})
public class ExprLoopValue extends SimpleExpression<Object> {
   private static final ExprLoopValue.LoopState[] loopStates = ExprLoopValue.LoopState.values();
   private String name;
   private SecLoop loop;
   boolean isKeyedLoop = false;
   boolean isIndex = false;
   private ExprLoopValue.LoopState selectedState;
   private static final Pattern LOOP_PATTERN;

   @Override
   public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.selectedState = loopStates[matchedPattern];
      this.name = parser.expr;
      String loopOf = parser.regexes.get(0).group();
      int expectedDepth = -1;
      Matcher m = LOOP_PATTERN.matcher(loopOf);
      if (m.matches()) {
         loopOf = m.group(1);
         expectedDepth = Utils.parseInt(m.group(2));
      }

      if (!"counter".equalsIgnoreCase(loopOf) && !"iteration".equalsIgnoreCase(loopOf)) {
         Class<?> expectedClass = Classes.getClassFromUserInput(loopOf);
         int candidateDepth = 1;
         SecLoop loop = null;

         for (SecLoop candidate : this.getParser().getCurrentSections(SecLoop.class)) {
            if (expectedClass != null && expectedClass.isAssignableFrom(candidate.getLoopedExpression().getReturnType())
               || "value".equalsIgnoreCase(loopOf)
               || candidate.getLoopedExpression().isLoopOf(loopOf)) {
               if (candidateDepth < expectedDepth) {
                  candidateDepth++;
               } else {
                  if (loop != null) {
                     Skript.error(
                        "There are multiple loops that match loop-" + loopOf + ". Use loop-" + loopOf + "-1/2/3/etc. to specify which loop's value you want."
                     );
                     return false;
                  }

                  loop = candidate;
                  if (candidateDepth == expectedDepth) {
                     break;
                  }
               }
            }
         }

         if (loop == null) {
            Skript.error("There's no loop that matches 'loop-" + loopOf + "'");
            return false;
         } else if (this.selectedState == ExprLoopValue.LoopState.NEXT && !loop.supportsPeeking()) {
            Skript.error("The expression '" + loop.getExpression().toString() + "' does not allow the usage of 'next loop-" + loopOf + "'.");
            return false;
         } else {
            if (loop.isKeyedLoop()) {
               this.isKeyedLoop = true;
               if (((KeyedIterableExpression)loop.getLoopedExpression()).isIndexLoop(loopOf)) {
                  this.isIndex = true;
               }
            }

            this.loop = loop;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<?> getReturnType() {
      return this.isIndex ? String.class : this.loop.getLoopedExpression().getReturnType();
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.isIndex ? new Class[]{String.class} : this.loop.getLoopedExpression().possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.isIndex ? super.canReturn(returnType) : this.loop.getLoopedExpression().canReturn(returnType);
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      if (this.isKeyedLoop) {
         KeyedValue<Object> value = (KeyedValue<Object>)(switch (this.selectedState) {
            case CURRENT -> this.loop.getCurrent(event);
            case NEXT -> this.loop.getNext(event);
            case PREVIOUS -> this.loop.getPrevious(event);
         });
         if (value == null) {
            return null;
         } else if (this.isIndex) {
            return new String[]{value.key()};
         } else {
            Object[] one = (Object[])Array.newInstance(this.getReturnType(), 1);
            one[0] = value.value();
            return one;
         }
      } else {
         Object[] one = (Object[])Array.newInstance(this.getReturnType(), 1);

         one[0] = switch (this.selectedState) {
            case CURRENT -> this.loop.getCurrent(event);
            case NEXT -> this.loop.getNext(event);
            case PREVIOUS -> this.loop.getPrevious(event);
         };
         return one;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (event == null) {
         return this.name;
      } else if (this.isKeyedLoop) {
         KeyedValue<Object> value = (KeyedValue<Object>)(switch (this.selectedState) {
            case CURRENT -> this.loop.getCurrent(event);
            case NEXT -> this.loop.getNext(event);
            case PREVIOUS -> this.loop.getPrevious(event);
         });
         if (value == null) {
            return Classes.getDebugMessage(null);
         } else {
            return this.isIndex ? "\"" + value.key() + "\"" : Classes.getDebugMessage(value.value());
         }
      } else {
         return Classes.getDebugMessage(switch (this.selectedState) {
            case CURRENT -> this.loop.getCurrent(event);
            case NEXT -> this.loop.getNext(event);
            case PREVIOUS -> this.loop.getPrevious(event);
         });
      }
   }

   static {
      String[] patterns = new String[loopStates.length];

      for (ExprLoopValue.LoopState state : loopStates) {
         patterns[state.ordinal()] = "[the] " + state.pattern + " loop-<.+>";
      }

      Skript.registerExpression(ExprLoopValue.class, Object.class, ExpressionType.SIMPLE, patterns);
      LOOP_PATTERN = Pattern.compile("^(.+)-(\\d+)$");
   }

   static enum LoopState {
      CURRENT("[current]"),
      NEXT("next"),
      PREVIOUS("previous");

      private final String pattern;

      private LoopState(String pattern) {
         this.pattern = pattern;
      }
   }
}

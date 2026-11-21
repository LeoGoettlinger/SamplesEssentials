package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.ComparatorInfo;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.util.Cyclical;

@Name("Comparison")
@Description({"A very general condition, it simply compares two values. Usually you can only compare for equality (e.g. block is/isn't of &lt;type&gt;), but some values can also be compared using greater than/less than. In that case you can also test for whether an object is between two others.", "Note: This is the only element where not all patterns are shown. It has actually another two sets of similar patters, but with <code>(was|were)</code> or <code>will be</code> instead of <code>(is|are)</code> respectively, which check different <a href='#ExprTimeState'>time states</a> of the first expression."})
@Example.Examples({@Example("the clicked block is a stone slab or a double stone slab"), @Example("time in the player's world is greater than 8:00"), @Example("the creature is not an enderman or an ender dragon")})
@Since({"1.0"})
public class CondCompare extends Condition implements VerboseAssert {
   private static final Patterns<Relation> patterns = new Patterns<>(
      new Object[][]{
         {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((greater|more|higher|bigger|larger) than|above)|\\>) %objects%", Relation.GREATER},
         {
               "(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects%",
               Relation.GREATER_OR_EQUAL
         },
         {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((less|smaller|lower) than|below)|\\<) %objects%", Relation.SMALLER},
         {
               "(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (less|smaller|lower|below) [than] or (equal to|the same as)|\\<=) %objects%",
               Relation.SMALLER_OR_EQUAL
         },
         {"(1¦neither|) %objects% (2¦)((is|are) (not|4¦neither)|isn't|aren't|!=) [equal to] %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects% (is|are|=) [(equal to|the same as)] %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects% (is|are) between %objects% and %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects% (2¦)(is not|are not|isn't|aren't) between %objects% and %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) ((greater|more|higher|bigger|larger) than|above) %objects%", Relation.GREATER},
         {
               "(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as) %objects%",
               Relation.GREATER_OR_EQUAL
         },
         {"(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) ((less|smaller|lower) than|below) %objects%", Relation.SMALLER},
         {
               "(1¦neither|) %objects@-1% (was|were)(|2¦(n't| not|4¦ neither)) (less|smaller|lower|below) [than] or (equal to|the same as) %objects%",
               Relation.SMALLER_OR_EQUAL
         },
         {"(1¦neither|) %objects@-1% (2¦)((was|were) (not|4¦neither)|wasn't|weren't) [equal to] %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@-1% (was|were) [(equal to|the same as)] %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@-1% (was|were) between %objects% and %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@-1% (2¦)(was not|were not|wasn't|weren't) between %objects% and %objects%", Relation.EQUAL},
         {
               "(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) ((greater|more|higher|bigger|larger) than|above) %objects%",
               Relation.GREATER
         },
         {
               "(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as) %objects%",
               Relation.GREATER_OR_EQUAL
         },
         {"(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) ((less|smaller|lower) than|below) %objects%", Relation.SMALLER},
         {
               "(1¦neither|) %objects@1% (will be|2¦(will (not|4¦neither) be|won't be)) (less|smaller|lower|below) [than] or (equal to|the same as) %objects%",
               Relation.SMALLER_OR_EQUAL
         },
         {
               "(1¦neither|) %objects@1% (2¦)((will (not|4¦neither) be|won't be)|(isn't|aren't|is not|are not) (turning|changing) [in]to) [equal to] %objects%",
               Relation.EQUAL
         },
         {"(1¦neither|) %objects@1% (will be [(equal to|the same as)]|(is|are) (turning|changing) [in]to) %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@1% will be between %objects% and %objects%", Relation.EQUAL},
         {"(1¦neither|) %objects@1% (2¦)(will not be|won't be) between %objects% and %objects%", Relation.EQUAL}
      }
   );
   private Expression<?> first;
   private Expression<?> second;
   @Nullable
   private Expression<?> third;
   private Relation relation;
   @Nullable
   private Comparator comparator;

   @Override
   public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.first = vars[0];
      this.second = vars[1];
      if (vars.length == 3) {
         this.third = vars[2];
      }

      this.relation = patterns.getInfo(matchedPattern);
      if ((parser.mark & 2) != 0) {
         this.setNegated(true);
      }

      if ((parser.mark & 1) != 0) {
         this.setNegated(!this.isNegated());
      }

      if ((parser.mark & 4) != 0) {
         if (this.second instanceof ExpressionList) {
            ((ExpressionList)this.second).invertAnd();
         }

         if (this.third instanceof ExpressionList) {
            ((ExpressionList)this.third).invertAnd();
         }
      }

      boolean b = this.init(parser.expr);
      Expression<?> third = this.third;
      if (!b) {
         if (third == null && this.first.getReturnType() == Object.class && this.second.getReturnType() == Object.class) {
            return false;
         } else {
            Skript.error(
               "Can't compare " + f(this.first) + " with " + f(this.second) + (third == null ? "" : " and " + f(third)), ErrorQuality.NOT_AN_EXPRESSION
            );
            return false;
         }
      } else {
         Comparator comp = this.comparator;
         if (comp != null) {
            if (third == null) {
               if (!this.relation.isImpliedBy(Relation.EQUAL, Relation.NOT_EQUAL) && !comp.supportsOrdering()) {
                  Skript.error("Can't test " + f(this.first) + " for being '" + this.relation + "' " + f(this.second), ErrorQuality.NOT_AN_EXPRESSION);
                  return false;
               }
            } else if (!comp.supportsOrdering()) {
               Skript.error("Can't test " + f(this.first) + " for being 'between' " + f(this.second) + " and " + f(third), ErrorQuality.NOT_AN_EXPRESSION);
               return false;
            }
         }

         return true;
      }
   }

   public static String f(Expression<?> e) {
      return e.getReturnType() == Object.class ? e.toString(null, false) : Classes.getSuperClassInfo(e.getReturnType()).getName().withIndefiniteArticle();
   }

   private boolean init(String expr) {
      Expression<?> third = this.third;

      try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
         if (this.first.getReturnType() == Object.class) {
            Expression<?> expression = null;
            if (this.first instanceof UnparsedLiteral) {
               expression = this.attemptReconstruction((UnparsedLiteral)this.first, this.second);
            }

            if (expression == null) {
               expression = this.first.getConvertedExpression(Object.class);
            }

            if (expression == null) {
               log.printError();
               return false;
            }

            this.first = expression;
         }

         if (this.second.getReturnType() == Object.class) {
            Expression<?> expressionx = null;
            if (this.second instanceof UnparsedLiteral) {
               expressionx = this.attemptReconstruction((UnparsedLiteral)this.second, this.first);
            }

            if (expressionx == null) {
               expressionx = this.second.getConvertedExpression(Object.class);
            }

            if (expressionx == null) {
               log.printError();
               return false;
            }

            this.second = expressionx;
         }

         if (third != null && third.getReturnType() == Object.class) {
            Expression<?> expressionxx = null;
            if (third instanceof UnparsedLiteral) {
               expressionxx = this.attemptReconstruction((UnparsedLiteral)third, this.first);
            }

            if (expressionxx == null) {
               expressionxx = third.getConvertedExpression(Object.class);
            }

            if (expressionxx == null) {
               log.printError();
               return false;
            }

            third = expressionxx;
            this.third = expressionxx;
         }

         log.printLog(false);
      }

      Class<?> firstReturnType = this.first.getReturnType();
      Class<?> secondReturnType = third == null ? this.second.getReturnType() : Utils.getSuperType(this.second.getReturnType(), third.getReturnType());
      if (firstReturnType != Object.class && secondReturnType != Object.class) {
         this.comparator = Comparators.getComparator(firstReturnType, secondReturnType);
         if (this.comparator == null) {
            SimpleLiteral<?> reparsedSecond = this.reparseLiteral(firstReturnType, this.second);
            if (reparsedSecond != null) {
               this.second = reparsedSecond;
               this.comparator = Comparators.getComparator(firstReturnType, this.second.getReturnType());
            } else {
               SimpleLiteral<?> reparsedFirst = this.reparseLiteral(this.second.getReturnType(), this.first);
               if (reparsedFirst != null) {
                  this.first = reparsedFirst;
                  this.comparator = Comparators.getComparator(this.first.getReturnType(), secondReturnType);
               }
            }
         }

         return this.comparator != null;
      } else {
         return true;
      }
   }

   @Nullable
   private <T> SimpleLiteral<T> reparseLiteral(Class<T> type, Expression<?> expression) {
      Expression<?> source = expression;
      if (expression instanceof SimpleLiteral) {
         source = expression.getSource();
      }

      return source instanceof UnparsedLiteral unparsedLiteral ? unparsedLiteral.reparse(type) : null;
   }

   private Literal<?> attemptReconstruction(UnparsedLiteral one, Expression<?> two) {
      Expression<?> expression = null;
      expression = one.getConvertedExpression(Number.class);
      if (expression == null) {
         for (ClassInfo<?> classinfo : Classes.getClassInfos()) {
            if (classinfo.getParser() != null) {
               ComparatorInfo<?, ?> comparator = Comparators.getComparatorInfo(two.getReturnType(), classinfo.getC());
               if (comparator != null && comparator.getFirstType() != Object.class) {
                  expression = this.reparseLiteral(classinfo.getC(), one);
                  if (expression != null) {
                     break;
                  }
               }
            }
         }
      }

      if (expression == null) {
         expression = one.getConvertedExpression(two.getReturnType());
      }

      return (Literal<?>)expression;
   }

   @Override
   public boolean check(Event event) {
      Expression<?> third = this.third;
      return this.relation == Relation.EQUAL
            && third == null
            && this.first.getAnd()
            && !this.first.isSingle()
            && this.second.getAnd()
            && !this.second.isSingle()
         ? this.compareLists(event)
         : this.first
            .check(
               event,
               o1 -> this.second
                  .check(
                     event,
                     o2 -> third == null
                        ? this.relation.isImpliedBy(this.comparator != null ? this.comparator.compare(o1, o2) : Comparators.compare(o1, o2))
                        : third.check(
                           event,
                           o3 -> {
                              boolean isBetween;
                              if (this.comparator != null) {
                                 if (o1 instanceof Cyclical && o2 instanceof Cyclical && o3 instanceof Cyclical) {
                                    if (Relation.GREATER_OR_EQUAL.isImpliedBy(this.comparator.compare(o2, o3))) {
                                       isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o2))
                                          || Relation.SMALLER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o3));
                                    } else {
                                       isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o2))
                                          && Relation.SMALLER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o3));
                                    }
                                 } else {
                                    isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o2))
                                          && Relation.SMALLER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o3))
                                       || Relation.GREATER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o3))
                                          && Relation.SMALLER_OR_EQUAL.isImpliedBy(this.comparator.compare(o1, o2));
                                 }
                              } else if (o1 instanceof Cyclical && o2 instanceof Cyclical && o3 instanceof Cyclical) {
                                 if (Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o2, o3))) {
                                    isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2))
                                       || Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3));
                                 } else {
                                    isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2))
                                       && Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3));
                                 }
                              } else {
                                 isBetween = Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2))
                                       && Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3))
                                    || Relation.GREATER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o3))
                                       && Relation.SMALLER_OR_EQUAL.isImpliedBy(Comparators.compare(o1, o2));
                              }

                              return this.relation == Relation.NOT_EQUAL ^ isBetween;
                           }
                        )
                  ),
               this.isNegated()
            );
   }

   @Override
   public String getExpectedMessage(Event event) {
      String message = "a value ";
      if (this.third == null) {
         return message + (this.isNegated() ? "not " : "") + this.relation + " " + VerboseAssert.getExpressionValue(this.second, event);
      } else {
         if (this.isNegated()) {
            message = message + "not ";
         }

         return message + "between " + VerboseAssert.getExpressionValue(this.second, event) + " and " + VerboseAssert.getExpressionValue(this.third, event);
      }
   }

   @Override
   public String getReceivedMessage(Event event) {
      return VerboseAssert.getExpressionValue(this.first, event);
   }

   private boolean compareLists(Event event) {
      Object[] first = this.first.getArray(event);
      Object[] second = this.second.getArray(event);
      boolean shouldMatch = !this.isNegated();
      if (first.length != second.length) {
         return !shouldMatch;
      } else {
         for (int i = 0; i < first.length; i++) {
            if (!this.relation.isImpliedBy(this.comparator != null ? this.comparator.compare(first[i], second[i]) : Comparators.compare(first[i], second[i]))) {
               return !shouldMatch;
            }
         }

         return shouldMatch;
      }
   }

   @Override
   public Condition simplify() {
      return (Condition)(!(this.first instanceof Literal) || !(this.second instanceof Literal) || this.third != null && !(this.third instanceof Literal)
         ? this
         : SimplifiedCondition.fromCondition(this));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      Expression<?> third = this.third;
      String s;
      if (third == null) {
         s = this.first.toString(event, debug) + " is " + (this.isNegated() ? "not " : "") + this.relation + " " + this.second.toString(event, debug);
      } else {
         s = this.first.toString(event, debug)
            + " is "
            + (this.isNegated() ? "not " : "")
            + "between "
            + this.second.toString(event, debug)
            + " and "
            + third.toString(event, debug);
      }

      if (debug) {
         s = s + " (comparator: " + this.comparator + ")";
      }

      return s;
   }

   static {
      Skript.registerCondition(CondCompare.class, Condition.ConditionType.PATTERN_MATCHES_EVERYTHING, patterns.getPatterns());
   }
}

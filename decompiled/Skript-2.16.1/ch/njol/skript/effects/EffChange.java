package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Change: Set/Add/Remove/Remove All/Delete/Reset")
@Description({"A general effect that can be used for changing many <a href='./expressions'>expressions</a>.", "Some expressions can only be set and/or deleted, while others can also have things added to or removed from them."})
@Example.Examples({@Example("set the player's display name to \"<red>%name of player%\"\nset the block above the victim to lava\n"), @Example("add 2 to the player's health # preferably use '<a href='#EffHealth'>heal</a>' for this\nadd argument to {blacklist::*}\ngive a diamond pickaxe of efficiency 5 to the player\nincrease the data value of the clicked block by 1\n"), @Example("remove 2 pickaxes from the victim\nsubtract 2.5 from {points::%uuid of player%}\n"), @Example("remove every iron tool from the player\nremove all minecarts from {entitylist::*}\n"), @Example("delete the block below the player\nclear drops\ndelete {variable}\n"), @Example("reset walk speed of player\nreset chunk at the targeted block\n")})
@Since({"1.0 (set, add, remove, delete), 2.0 (remove all)"})
public class EffChange extends Effect {
   private static final Patterns<Changer.ChangeMode> PATTERNS = new Patterns<>(
      new Object[][]{
         {"(add|give) %objects% to %~objects%", Changer.ChangeMode.ADD},
         {"increase %~objects% by %objects%", Changer.ChangeMode.ADD},
         {"give %~objects% %objects%", Changer.ChangeMode.ADD},
         {"set %~objects% to %objects%", Changer.ChangeMode.SET},
         {"remove (all|every) %objects% from %~objects%", Changer.ChangeMode.REMOVE_ALL},
         {"(remove|subtract) %objects% from %~objects%", Changer.ChangeMode.REMOVE},
         {"(reduce|decrease) %~objects% by %objects%", Changer.ChangeMode.REMOVE},
         {"(delete|clear) %~objects%", Changer.ChangeMode.DELETE},
         {"reset %~objects%", Changer.ChangeMode.RESET}
      }
   );
   private Expression<?> changed;
   @Nullable
   private Expression<?> changer;
   private Changer.ChangeMode mode;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.mode = PATTERNS.getInfo(matchedPattern);
      switch (this.mode) {
         case ADD:
         case REMOVE:
            if (matchedPattern != 0 && matchedPattern != 5) {
               this.changer = exprs[1];
               this.changed = exprs[0];
            } else {
               this.changer = exprs[0];
               this.changed = exprs[1];
            }
            break;
         case SET:
            this.changer = exprs[1];
            this.changed = exprs[0];
            break;
         case REMOVE_ALL:
            this.changer = exprs[0];
            this.changed = exprs[1];
            break;
         case DELETE:
         case RESET:
            this.changed = exprs[0];
      }

      CountingLogHandler changeLog = new CountingLogHandler(Level.SEVERE);
      CountingLogHandler flatAcceptedTypes = changeLog;

      Class<?>[] acceptedTypes;
      String what;
      try {
         acceptedTypes = this.changed.acceptChange(this.mode);
         ClassInfo<?> changedInfo = Classes.getSuperClassInfo(this.changed.getReturnType());
         Changer<?> changer = changedInfo.getChanger();
         if (changer != null && Arrays.equals((Object[])changer.acceptChange(this.mode), (Object[])acceptedTypes)) {
            what = changedInfo.getName().withIndefiniteArticle();
         } else {
            what = this.changed.toString(null, Skript.debug());
         }
      } catch (Throwable var21) {
         if (changeLog != null) {
            try {
               flatAcceptedTypes.close();
            } catch (Throwable var19) {
               var21.addSuppressed(var19);
            }
         }

         throw var21;
      }

      if (changeLog != null) {
         changeLog.close();
      }

      if (acceptedTypes == null) {
         if (changeLog.getCount() > 0) {
            return false;
         } else {
            Skript.error(
               switch (this.mode) {
                  case ADD -> what + " can't have anything added to it";
                  case REMOVE, REMOVE_ALL -> this.mode == Changer.ChangeMode.REMOVE_ALL && this.changed.acceptChange(Changer.ChangeMode.REMOVE) != null
                     ? what
                        + " can't have 'all of something' removed from it. However, it does support regular removal which could be used to achieve the desired effect"
                     : what + " can't have anything removed from it";
                  case SET -> what + " can't be set to anything";
                  case DELETE -> {
                     String error = what + " can't be deleted";
                     if (this.changed.acceptChange(Changer.ChangeMode.RESET) != null) {
                        error = error + ". However, it can be reset which might result in the desired effect";
                     }

                     yield error;
                  }
                  case RESET -> {
                     String error = what + " can't be reset";
                     if (this.changed.acceptChange(Changer.ChangeMode.DELETE) != null) {
                        error = error + ". However, it can be deleted which might result in the desired effect";
                     }

                     yield error;
                  }
               }
            );
            return false;
         }
      } else {
         Class<?>[] flatAcceptedTypesx = new Class[acceptedTypes.length];

         for (int i = 0; i < flatAcceptedTypesx.length; i++) {
            Class<?> type = acceptedTypes[i];
            if (type.isArray()) {
               type = type.getComponentType();
            }

            flatAcceptedTypesx[i] = type;
         }

         if (this.changed instanceof Variable<?> variable && this.mode == Changer.ChangeMode.DELETE && HintManager.canUseHints(variable)) {
            this.getParser().getHintManager().delete(variable);
         }

         if (this.changer == null) {
            return true;
         } else {
            Expression<?> validatedChanger = null;

            try (ParseLogHandler log = new ParseLogHandler().start()) {
               if (LiteralUtils.canInitSafely(this.changer)) {
                  Class<?> changerReturnType = this.changer.getReturnType();

                  for (Class<?> type : flatAcceptedTypesx) {
                     if (type.isAssignableFrom(changerReturnType)) {
                        validatedChanger = this.changer;
                        break;
                     }
                  }
               }

               if (validatedChanger == null) {
                  validatedChanger = this.changer.getConvertedExpression(flatAcceptedTypesx);
               }

               if (validatedChanger == null) {
                  if (!log.hasError()) {
                     String changerString = this.changer.toString(null, Skript.debug());
                     if (flatAcceptedTypesx.length == 1 && flatAcceptedTypesx[0] == Object.class) {
                        Skript.error("Can't understand this expression: " + changerString, ErrorQuality.NOT_AN_EXPRESSION);
                     } else {
                        String not = "is " + SkriptParser.notOfType(flatAcceptedTypesx);

                        Skript.error(switch (this.mode) {
                           case ADD -> changerString + " can't be added to " + what + " because the former " + not;
                           case REMOVE, REMOVE_ALL -> changerString + " can't be removed from " + what + " because the former " + not;
                           case SET -> what + " can't be set to " + changerString + " because the latter " + not;
                           default -> throw new IllegalStateException("Unexpected value: " + this.mode);
                        });
                     }
                  }

                  log.printError();
                  return false;
               }

               log.printLog();
            }

            this.changer = validatedChanger;
            List<Class<?>> singleTypes = new ArrayList<>();
            boolean expectingSingle = true;

            for (int i = 0; i < acceptedTypes.length; i++) {
               if (validatedChanger.canReturn(flatAcceptedTypesx[i])) {
                  if (acceptedTypes[i].isArray()) {
                     expectingSingle = false;
                     break;
                  }

                  singleTypes.add(flatAcceptedTypesx[i]);
               }
            }

            if (expectingSingle && !this.changer.canBeSingle()) {
               String changedString = this.changed.toString(null, Skript.debug());
               String types = Classes.toString(singleTypes.toArray(), false);

               Skript.error(switch (this.mode) {
                  case ADD -> "Only one " + types + " can be added to " + changedString + ", not more";
                  case REMOVE, REMOVE_ALL -> "Only one " + types + " can be removed from " + changedString + ", not more";
                  case SET -> changedString + " can only be set to one " + types + ", not more";
                  default -> throw new IllegalStateException("Unexpected value: " + this.mode);
               });
               return false;
            } else {
               if (this.changed instanceof Variable<?> variable) {
                  if (!this.changed.isSingle() && this.mode == Changer.ChangeMode.SET) {
                     if (this.changer instanceof ExprParse exprParse) {
                        exprParse.flatten = false;
                     } else if (this.changer instanceof ExpressionList<?> exprList) {
                        for (Expression<?> expression : exprList.getAllExpressions()) {
                           if (expression instanceof ExprParse exprParse) {
                              exprParse.flatten = false;
                           }
                        }
                     }
                  }

                  if (this.mode == Changer.ChangeMode.SET || variable.isList() && this.mode == Changer.ChangeMode.ADD) {
                     if (HintManager.canUseHints(variable)) {
                        HintManager hintManager = this.getParser().getHintManager();
                        Class<?>[] hints = this.changer.possibleReturnTypes();
                        if (this.mode == Changer.ChangeMode.SET) {
                           hintManager.set(variable, hints);
                        } else {
                           hintManager.add(variable, hints);
                        }
                     }

                     if (!variable.isLocal() && !variable.isEphemeral()) {
                        ClassInfo<?> changerInfo = Classes.getSuperClassInfo(this.changer.getReturnType());
                        if (changerInfo.getC() != Object.class
                           && changerInfo.getSerializer() == null
                           && changerInfo.getSerializeAs() == null
                           && !SkriptConfig.disableObjectCannotBeSavedWarnings.value()
                           && this.getParser().isActive()
                           && !this.getParser().getCurrentScript().suppressesWarning(ScriptWarning.VARIABLE_SAVE)) {
                           Skript.warning(
                              changerInfo.getName().withIndefiniteArticle()
                                 + " cannot be saved. That is, the contents of the variable "
                                 + this.changed.toString(null, Skript.debug())
                                 + " will be lost when the server stops."
                           );
                        }
                     }
                  }
               }

               return true;
            }
         }
      }
   }

   @Override
   protected void execute(Event event) {
      Object[] delta = null;
      if (this.changer != null) {
         delta = this.changer.getArray(event);
         delta = this.changer.beforeChange(this.changed, delta);
         if (delta == null || delta.length == 0) {
            if (this.mode == Changer.ChangeMode.SET && this.changed.acceptChange(Changer.ChangeMode.DELETE) != null) {
               this.changed.change(event, null, Changer.ChangeMode.DELETE);
            }

            return;
         }

         if (this.mode.supportsKeyedChange()
            && KeyProviderExpression.areKeysRecommended(this.changer)
            && this.changed instanceof KeyReceiverExpression<?> receiver) {
            receiver.change(event, delta, this.mode, ((KeyProviderExpression)this.changer).getArrayKeys(event));
            return;
         }
      }

      this.changed.change(event, delta, this.mode);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      switch (this.mode) {
         case ADD:
            assert this.changer != null;

            builder.append("add", this.changer, "to", this.changed);
            break;
         case REMOVE:
            assert this.changer != null;

            builder.append("remove", this.changer, "from", this.changed);
            break;
         case SET:
            assert this.changer != null;

            builder.append("set", this.changed, "to", this.changer);
            break;
         case REMOVE_ALL:
            assert this.changer != null;

            builder.append("remove all", this.changer, "from", this.changed);
            break;
         case DELETE:
            builder.append("delete", this.changed);
            break;
         case RESET:
            builder.append("reset", this.changed);
      }

      return builder.toString();
   }

   static {
      Skript.registerEffect(EffChange.class, PATTERNS.getPatterns());
   }
}

package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.NonNullIterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class UnparsedLiteral implements Literal<Object> {
   private final String data;
   @Nullable
   private final LogEntry error;
   @Nullable
   private final List<ClassInfo<?>> possibleInfos;
   private boolean reparsed = false;
   private boolean converted = false;

   public UnparsedLiteral(String data) {
      this(data, null);
   }

   public UnparsedLiteral(String data, @Nullable LogEntry error) {
      assert data.length() > 0;

      assert error == null || error.getLevel() == Level.SEVERE;

      this.data = data;
      this.error = error;
      this.possibleInfos = Classes.getPatternInfos(data);
   }

   public String getData() {
      return this.data;
   }

   @Override
   public Class<?> getReturnType() {
      return Object.class;
   }

   @Nullable
   @Override
   public <R> Literal<? extends R> getConvertedExpression(Class<R>... to) {
      return this.getConvertedExpression(ParseContext.DEFAULT, to);
   }

   @Nullable
   public <R> Literal<? extends R> getConvertedExpression(ParseContext context, Class<? extends R>... to) {
      assert to.length > 0;

      assert to.length == 1 || !CollectionUtils.contains(to, Object.class);

      ParseLogHandler log = SkriptLogger.startParseLogHandler();

      try {
         for (Class<? extends R> type : to) {
            assert type != null;

            R parsedObject = Classes.parse(this.data, (Class<R>)type, context);
            if (parsedObject != null) {
               if (!type.equals(Object.class)) {
                  this.converted = true;
               }

               log.printLog();
               return new SimpleLiteral<>(parsedObject, false, this);
            }

            log.clear();
         }

         if (this.error != null) {
            log.printLog();
            SkriptLogger.log(this.error);
         } else {
            log.printError();
         }

         return null;
      } finally {
         log.stop();
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "'" + this.data + "'";
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @Override
   public Expression<?> getSource() {
      return this;
   }

   @Override
   public boolean getAnd() {
      return true;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Expression<?> simplify() {
      return this;
   }

   @Nullable
   public <T> SimpleLiteral<T> reparse(Class<T> type) {
      T typedObject = Classes.parse(this.data, type, ParseContext.DEFAULT);
      if (typedObject != null) {
         if (!type.equals(Object.class)) {
            this.reparsed = true;
         }

         return new SimpleLiteral<>(typedObject, false, new UnparsedLiteral(this.data));
      } else {
         return null;
      }
   }

   public boolean wasReparsed() {
      return this.reparsed;
   }

   public boolean wasConverted() {
      return this.converted;
   }

   @Nullable
   public List<ClassInfo<?>> getPossibleInfos() {
      return this.possibleInfos;
   }

   public boolean multipleWarning() {
      if (!this.reparsed && !this.converted && this.possibleInfos != null && this.possibleInfos.size() > 1) {
         String infoCodeName = this.possibleInfos.get(0).getName().getSingular();
         String combinedInfos = Classes.toString(this.possibleInfos.toArray(), true);
         Skript.warning(
            "'" + this.data + "' has multiple types (" + combinedInfos + "). Consider specifying which type to use: '" + this.data + " (" + infoCodeName + ")'"
         );
         return true;
      } else {
         return false;
      }
   }

   private static SkriptAPIException invalidAccessException() {
      return new SkriptAPIException("UnparsedLiterals must be converted before use");
   }

   @Override
   public Object[] getAll() {
      throw invalidAccessException();
   }

   @Override
   public Object[] getAll(Event event) {
      throw invalidAccessException();
   }

   @Override
   public Object[] getArray() {
      throw invalidAccessException();
   }

   @Override
   public Object[] getArray(Event event) {
      throw invalidAccessException();
   }

   @Override
   public Object getSingle() {
      throw invalidAccessException();
   }

   @Override
   public Object getSingle(Event event) {
      throw invalidAccessException();
   }

   public NonNullIterator<Object> iterator(Event event) {
      throw invalidAccessException();
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      throw invalidAccessException();
   }

   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      throw invalidAccessException();
   }

   @Override
   public boolean check(Event event, Predicate<? super Object> checker) {
      throw invalidAccessException();
   }

   @Override
   public boolean check(Event event, Predicate<? super Object> checker, boolean negated) {
      throw invalidAccessException();
   }

   @Override
   public boolean setTime(int time) {
      throw invalidAccessException();
   }

   @Override
   public int getTime() {
      throw invalidAccessException();
   }

   @Override
   public boolean isDefault() {
      throw invalidAccessException();
   }

   @Override
   public boolean isLoopOf(String input) {
      throw invalidAccessException();
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      throw invalidAccessException();
   }
}

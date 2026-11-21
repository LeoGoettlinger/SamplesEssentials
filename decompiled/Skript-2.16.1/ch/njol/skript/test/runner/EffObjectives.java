package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Objectives")
@Description({"An effect to setup required objectives for JUnit tests to complete."})
@NoDoc
public class EffObjectives extends Effect {
   private static final Multimap<String, String> requirements;
   private static final Multimap<String, String> completeness;
   private Expression<String> junit;
   private Expression<String> objectives;
   private boolean setup;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.objectives = (Expression<String>)exprs[matchedPattern ^ 1];
      this.junit = (Expression<String>)exprs[matchedPattern];
      this.setup = matchedPattern == 0;
      return true;
   }

   @Override
   protected void execute(Event event) {
      String junit = this.junit.getSingle(event);

      assert junit != null;

      String[] objectives = this.objectives.getArray(event);

      assert objectives.length > 0;

      if (this.setup) {
         requirements.putAll(junit, Lists.newArrayList(objectives));
      } else {
         completeness.putAll(junit, Lists.newArrayList(objectives));
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.setup
         ? "ensure junit test " + this.junit.toString(event, debug) + " completes objectives " + this.objectives.toString(event, debug)
         : "complete objectives " + this.objectives.toString(event, debug) + " on junit test " + this.junit.toString(event, debug);
   }

   public static boolean isJUnitComplete() {
      assert !completeness.isEmpty() || !requirements.isEmpty();

      return completeness.equals(requirements);
   }

   public static void fail() {
      for (String test : requirements.keySet()) {
         if (!completeness.containsKey(test)) {
            TestTracker.JUnitTestFailed("JUnit: '" + test + "'", "didn't complete any objectives.");
         } else {
            List<String> failures = Lists.newArrayList(requirements.get(test));
            failures.removeAll(completeness.get(test));
            if (!failures.isEmpty()) {
               TestTracker.JUnitTestFailed("JUnit: '" + test + "'", "failed objectives: " + Arrays.toString(failures.toArray(new String[0])));
            }
         }
      }
   }

   static {
      if (TestMode.ENABLED) {
         Skript.registerEffect(
            EffObjectives.class,
            "ensure [[junit] test] %string% completes [(objective|trigger)[s]] %strings%",
            "complete [(objective|trigger)[s]] %strings% (for|on) [[junit] test] %string%"
         );
      }

      requirements = HashMultimap.create();
      completeness = HashMultimap.create();
   }
}

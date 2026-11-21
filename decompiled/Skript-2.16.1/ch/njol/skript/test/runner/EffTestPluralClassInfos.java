package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Test Plural Class Infos")
@Description({"Tests that plural class infos are identified correctly."})
@NoDoc
public class EffTestPluralClassInfos extends Effect {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return false;
   }

   @Override
   protected void execute(Event event) {
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "";
   }

   static {
      if (TestMode.ENABLED) {
         Classes.registerClass(new ClassInfo<>(EffTestPluralClassInfos$1Example1.class, "testgui").user("example1").name(ClassInfo.NO_DOC));
         Classes.registerClass(new ClassInfo<>(EffTestPluralClassInfos$1Example2.class, "exemplus").user("example2").name(ClassInfo.NO_DOC));
         Classes.registerClass(new ClassInfo<>(EffTestPluralClassInfos$1Example3.class, "aardwolf").user("example3").name(ClassInfo.NO_DOC));
         Classes.registerClass(new ClassInfo<>(EffTestPluralClassInfos$1Example4.class, "hoof").user("example3").name(ClassInfo.NO_DOC));
         Skript.registerEffect(
            EffTestPluralClassInfos.class,
            "classinfo test for %testgui%",
            "classinfo test for %testguis%",
            "classinfo test for %exemplus%",
            "classinfo test for %exempli%",
            "classinfo test for %aardwolf%",
            "classinfo test for %aardwolves%",
            "classinfo test for %hoof%",
            "classinfo test for %hooves%"
         );
      }
   }
}

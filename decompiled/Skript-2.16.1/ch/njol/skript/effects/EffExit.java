package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.sections.SecConditional;
import ch.njol.util.Kleenean;
import java.util.List;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Exit")
@Description({"Exits a given amount of loops and conditionals, or the entire trigger."})
@Example("loop blocks above the player:\n\tloop-block is not air:\n\t\texit 2 sections\n\tset loop-block to water\n")
@Since({"unknown (before 2.1)"})
public class EffExit extends Effect {
   private static final Class<? extends TriggerSection>[] types = new Class[]{TriggerSection.class, LoopSection.class, SecConditional.class};
   private static final String[] names = new String[]{"sections", "loops", "conditionals"};
   private int type;
   private int breakLevels;
   private TriggerSection outerSection;
   @UnknownNullability
   private List<SectionExitHandler> sectionsToExit;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      List<TriggerSection> innerSections = null;
      switch (matchedPattern) {
         case 0:
            innerSections = this.getParser().getCurrentSections();
            this.breakLevels = innerSections.size() + 1;
            break;
         case 1:
         case 2:
            this.breakLevels = matchedPattern == 1 ? 1 : Integer.parseInt(parseResult.regexes.get(0).group());
            if (this.breakLevels < 1) {
               return false;
            }

            this.type = parseResult.mark;
            ParserInstance parser = this.getParser();
            int levels = parser.getCurrentSections(types[this.type]).size();
            if (this.breakLevels > levels) {
               if (levels == 0) {
                  Skript.error("Can't stop any " + names[this.type] + " as there are no " + names[this.type] + " present");
               } else {
                  Skript.error("Can't stop " + this.breakLevels + " " + names[this.type] + " as there are only " + levels + " " + names[this.type] + " present");
               }

               return false;
            }

            innerSections = parser.getSections(this.breakLevels, types[this.type]);
            this.outerSection = innerSections.get(0);
            break;
         case 3:
            ParserInstance parser = this.getParser();
            this.type = parseResult.mark;
            List<? extends TriggerSection> sections = parser.getCurrentSections(types[this.type]);
            if (sections.isEmpty()) {
               Skript.error("Can't stop any " + names[this.type] + " as there are no " + names[this.type] + " present");
               return false;
            }

            this.outerSection = sections.get(0);
            innerSections = parser.getSectionsUntil(this.outerSection);
            innerSections.add(0, this.outerSection);
            this.breakLevels = innerSections.size();
      }

      assert innerSections != null;

      this.sectionsToExit = innerSections.stream().filter(SectionExitHandler.class::isInstance).map(SectionExitHandler.class::cast).toList();
      return true;
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      this.debug(event, false);

      for (SectionExitHandler section : this.sectionsToExit) {
         section.exit(event);
      }

      if (this.outerSection == null) {
         return null;
      } else {
         return this.outerSection instanceof LoopSection loopSection ? loopSection.getActualNext() : this.outerSection.getNext();
      }
   }

   @Override
   protected void execute(Event event) {
      assert false;
   }

   @Nullable
   @Override
   public ExecutionIntent executionIntent() {
      return (ExecutionIntent)(this.outerSection == null ? ExecutionIntent.stopTrigger() : ExecutionIntent.stopSections(this.breakLevels));
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.outerSection == null ? "stop trigger" : "stop " + this.breakLevels + " " + names[this.type];
   }

   static {
      Skript.registerEffect(
         EffExit.class,
         "(exit|stop) [trigger]",
         "(exit|stop) [1|a|the|this] (section|1:loop|2:conditional)",
         "(exit|stop) <-?\\d+(_\\d+)*> (section|1:loop|2:conditional)s",
         "(exit|stop) all (section|1:loop|2:conditional)s"
      );
   }
}

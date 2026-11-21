package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Panda Gene")
@Description({"The main or hidden gene of a panda."})
@Example("if the main gene of last spawned panda is lazy:\n\tset the main gene of last spawned panda to playful\n")
@Since({"2.11"})
public class ExprPandaGene extends SimplePropertyExpression<LivingEntity, Gene> {
   private boolean mainGene;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.mainGene = parseResult.hasTag("main");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Gene convert(LivingEntity entity) {
      if (entity instanceof Panda panda) {
         return this.mainGene ? panda.getMainGene() : panda.getHiddenGene();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Gene.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      Gene gene = (Gene)delta[0];

      for (LivingEntity entity : this.getExpr().getArray(event)) {
         if (entity instanceof Panda panda) {
            if (this.mainGene) {
               panda.setMainGene(gene);
            } else {
               panda.setHiddenGene(gene);
            }
         }
      }
   }

   @Override
   public Class<? extends Gene> getReturnType() {
      return Gene.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.mainGene ? "main" : "hidden") + "gene";
   }

   static {
      register(ExprPandaGene.class, Gene.class, "(:main|hidden) gene[s]", "livingentities");
   }
}

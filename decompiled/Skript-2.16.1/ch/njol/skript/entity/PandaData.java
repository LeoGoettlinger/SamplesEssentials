package ch.njol.skript.entity;

import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PandaData extends EntityData<Panda> {
   private static final Gene[] GENES = Gene.values();
   @Nullable
   private Gene mainGene = null;
   @Nullable
   private Gene hiddenGene = null;

   public PandaData() {
   }

   public PandaData(@Nullable Gene mainGene, @Nullable Gene hiddenGene) {
      this.mainGene = mainGene;
      this.hiddenGene = hiddenGene;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (exprs[0] != null) {
         this.mainGene = (Gene)exprs[0].getSingle();
         if (exprs[1] != null) {
            this.hiddenGene = (Gene)exprs[1].getSingle();
         }
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Panda> entityClass, @Nullable Panda panda) {
      if (panda != null) {
         this.mainGene = panda.getMainGene();
         this.hiddenGene = panda.getHiddenGene();
      }

      return true;
   }

   public void set(Panda panda) {
      Gene gene = this.mainGene;
      if (gene == null) {
         gene = CollectionUtils.getRandom(GENES);
      }

      assert gene != null;

      panda.setMainGene(gene);
      panda.setHiddenGene(this.hiddenGene != null ? this.hiddenGene : gene);
   }

   protected boolean match(Panda panda) {
      return !this.dataMatch(this.mainGene, panda.getMainGene()) ? false : this.dataMatch(this.hiddenGene, panda.getHiddenGene());
   }

   @Override
   public Class<? extends Panda> getType() {
      return Panda.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new PandaData();
   }

   @Override
   protected int hashCode_i() {
      int prime = 7;
      int result = 0;
      result = result * prime + Objects.hashCode(this.mainGene);
      return result * prime + Objects.hashCode(this.hiddenGene);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return !(entityData instanceof PandaData other) ? false : other.mainGene == this.mainGene && other.hiddenGene == this.hiddenGene;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof PandaData other) {
         return !this.dataMatch(this.mainGene, other.mainGene) ? false : this.dataMatch(this.hiddenGene, other.hiddenGene);
      } else {
         return false;
      }
   }

   @Override
   public String toString(int flags) {
      StringBuilder builder = new StringBuilder();
      if (this.mainGene != null) {
         builder.append(Language.getList("genes." + this.mainGene.name())[0]).append(" ");
      }

      if (this.hiddenGene != null && this.hiddenGene != this.mainGene) {
         builder.append(Language.getList("genes." + this.hiddenGene.name())[0]).append(" ");
      }

      builder.append(Language.get("panda"));
      return builder.toString();
   }

   static {
      EntityData.register(PandaData.class, "panda", Panda.class, "panda");
      Classes.registerClass(
         new EnumClassInfo(Gene.class, "gene", "genes")
            .user(new String[]{"(panda )?genes?"})
            .name("Gene")
            .description("Represents a Panda's main or hidden gene. See <a href='https://minecraft.wiki/w/Panda#Genetics'>genetics</a> for more info.")
            .since("2.4")
            .requiredPlugins("Minecraft 1.14 or newer")
      );
   }
}

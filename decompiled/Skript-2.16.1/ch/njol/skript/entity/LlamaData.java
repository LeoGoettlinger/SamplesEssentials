package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import java.util.Objects;
import org.bukkit.entity.Llama;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Llama.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LlamaData extends EntityData<Llama> {
   private static final Patterns<LlamaData.LlamaState> PATTERNS = new Patterns<>(
      new Object[][]{
         {"llama", new LlamaData.LlamaState(null, false)},
         {"creamy llama", new LlamaData.LlamaState(Color.CREAMY, false)},
         {"white llama", new LlamaData.LlamaState(Color.WHITE, false)},
         {"brown llama", new LlamaData.LlamaState(Color.BROWN, false)},
         {"gray llama", new LlamaData.LlamaState(Color.GRAY, false)},
         {"trader llama", new LlamaData.LlamaState(null, true)},
         {"creamy trader llama", new LlamaData.LlamaState(Color.CREAMY, true)},
         {"white trader llama", new LlamaData.LlamaState(Color.WHITE, true)},
         {"brown trader llama", new LlamaData.LlamaState(Color.BROWN, true)},
         {"gray trader llama", new LlamaData.LlamaState(Color.GRAY, true)}
      }
   );
   private static final Color[] LLAMA_COLORS = Color.values();
   @Nullable
   private Color color = null;
   private boolean isTrader;

   public LlamaData() {
   }

   public LlamaData(@Nullable Color color, boolean isTrader) {
      this.color = color;
      this.isTrader = isTrader;
      super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaData.LlamaState(color, isTrader), 0).orElse(0);
   }

   public LlamaData(@Nullable LlamaData.LlamaState llamaState) {
      if (llamaState != null) {
         this.color = llamaState.color;
         this.isTrader = llamaState.trader;
         super.codeNameIndex = PATTERNS.getMatchedPattern(llamaState, 0).orElse(0);
      } else {
         this.color = null;
         this.isTrader = false;
         super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaData.LlamaState(this.color, this.isTrader), 0).orElse(0);
      }
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      LlamaData.LlamaState llamaState = PATTERNS.getInfo(matchedCodeName);

      assert llamaState != null;

      this.color = llamaState.color;
      this.isTrader = llamaState.trader;
      return true;
   }

   protected boolean init(@Nullable Class<? extends Llama> entityClass, @Nullable Llama llama) {
      if (entityClass != null) {
         this.isTrader = TraderLlama.class.isAssignableFrom(entityClass);
      }

      if (llama != null) {
         this.color = llama.getColor();
         this.isTrader = llama instanceof TraderLlama;
         super.codeNameIndex = PATTERNS.getMatchedPattern(new LlamaData.LlamaState(this.color, this.isTrader), 0).orElse(0);
      }

      return true;
   }

   public void set(Llama llama) {
      Color color = this.color;
      if (color == null) {
         color = CollectionUtils.getRandom(LLAMA_COLORS);
      }

      assert color != null;

      llama.setColor(color);
   }

   protected boolean match(Llama llama) {
      return this.isTrader && !(llama instanceof TraderLlama) ? false : this.dataMatch(this.color, llama.getColor());
   }

   @Override
   public Class<? extends Llama> getType() {
      return this.isTrader ? TraderLlama.class : Llama.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new LlamaData();
   }

   @Override
   protected int hashCode_i() {
      int prime = 31;
      int result = 1;
      result = prime * result + Objects.hashCode(this.color);
      return prime * result + (this.isTrader ? 1 : 0);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return !(entityData instanceof LlamaData other) ? false : this.isTrader == other.isTrader && other.color == this.color;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof LlamaData other) {
         return this.isTrader && !other.isTrader ? false : this.dataMatch(this.color, other.color);
      } else {
         return false;
      }
   }

   static {
      EntityData.register(LlamaData.class, "llama", Llama.class, 0, PATTERNS.getPatterns());
      Variables.yggdrasil.registerSingleClass(Color.class, "Llama.Color");
   }

   public record LlamaState(Color color, boolean trader) {
   }
}

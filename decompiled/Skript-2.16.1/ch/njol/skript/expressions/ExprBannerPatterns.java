package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Nullable;

@Name("Banner Patterns")
@Description({"Gets or sets the banner patterns of a banner.", "In order to set a specific position of a banner, there needs to be that many patterns already on the banner.", "This expression will add filler patterns to the banner to allow the specified position to be set.", "For Example, setting the 3rd banner pattern of a banner that has no patterns on it, will internally add 3 base patterns, allowing the 3rd banner pattern to be set."})
@Example.Examples({@Example("broadcast banner patterns of {_banneritem}"), @Example("broadcast 1st banner pattern of block at location(0,0,0)"), @Example("clear banner patterns of {_banneritem}")})
@Since({"2.10"})
public class ExprBannerPatterns extends PropertyExpression<Object, Pattern> {
   private Expression<?> objects;
   private Expression<Integer> patternNumber = null;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern <= 1) {
         this.objects = exprs[0];
      } else if (matchedPattern == 2) {
         this.patternNumber = (Expression<Integer>)exprs[0];
         this.objects = exprs[1];
      } else {
         this.patternNumber = (Expression<Integer>)exprs[1];
         this.objects = exprs[0];
      }

      this.setExpr((Expression<? extends Object>)this.objects);
      return true;
   }

   @Nullable
   protected Pattern[] get(Event event, Object[] source) {
      List<Pattern> patterns = new ArrayList<>();
      Integer placement = this.patternNumber != null ? this.patternNumber.getSingle(event) : null;

      for (Object object : this.objects.getArray(event)) {
         if (object instanceof Block block) {
            if (block.getState() instanceof Banner banner) {
               if (placement != null && banner.numberOfPatterns() >= placement) {
                  patterns.add(banner.getPattern(placement - 1));
               } else if (placement == null) {
                  patterns.addAll(banner.getPatterns());
               }
            }
         } else {
            ItemStack itemStack = ItemUtils.asItemStack(object);
            if (itemStack != null && itemStack.getItemMeta() instanceof BannerMeta bannerMeta) {
               if (placement != null && bannerMeta.numberOfPatterns() >= placement) {
                  patterns.add(bannerMeta.getPattern(placement - 1));
               } else if (placement == null) {
                  patterns.addAll(bannerMeta.getPatterns());
               }
            }
         }
      }

      return patterns.toArray(new Pattern[0]);
   }

   private Consumer<BannerMeta> getPlacementMetaChanger(Changer.ChangeMode mode, int placement, @Nullable Pattern pattern) {
      return switch (mode) {
         case SET -> bannerMeta -> {
            if (bannerMeta.numberOfPatterns() < placement) {
               int toAdd = placement - bannerMeta.numberOfPatterns();

               for (int i = 0; i < toAdd; i++) {
                  bannerMeta.addPattern(new Pattern(DyeColor.WHITE, PatternType.BASE));
               }
            }

            bannerMeta.setPattern(placement - 1, pattern);
         };
         case DELETE -> bannerMeta -> {
            if (bannerMeta.numberOfPatterns() >= placement) {
               bannerMeta.removePattern(placement - 1);
            }
         };
         default -> bannerMeta -> {};
      };
   }

   private Consumer<Banner> getPlacementBlockChanger(Changer.ChangeMode mode, int placement, @Nullable Pattern pattern) {
      return switch (mode) {
         case SET -> banner -> {
            if (banner.numberOfPatterns() < placement) {
               int toAdd = placement - banner.numberOfPatterns();

               for (int i = 0; i < toAdd; i++) {
                  banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.BASE));
               }
            }

            banner.setPattern(placement - 1, pattern);
         };
         case DELETE -> banner -> {
            if (banner.numberOfPatterns() >= placement) {
               banner.removePattern(placement - 1);
            }
         };
         default -> banner -> {};
      };
   }

   private Consumer<BannerMeta> getAllMetaChanger(Changer.ChangeMode mode, List<Pattern> patterns) {
      return switch (mode) {
         case SET -> bannerMeta -> bannerMeta.setPatterns(patterns);
         case DELETE -> bannerMeta -> bannerMeta.setPatterns(new ArrayList());
         case ADD -> bannerMeta -> patterns.forEach(bannerMeta::addPattern);
         case REMOVE -> bannerMeta -> {
            List<Pattern> current = bannerMeta.getPatterns();
            current.removeAll(patterns);
            bannerMeta.setPatterns(current);
         };
         default -> bannerMeta -> {};
      };
   }

   private Consumer<Banner> getAllBlockChanger(Changer.ChangeMode mode, List<Pattern> patterns) {
      return switch (mode) {
         case SET -> banner -> banner.setPatterns(patterns);
         case DELETE -> banner -> banner.setPatterns(new ArrayList());
         case ADD -> banner -> patterns.forEach(banner::addPattern);
         case REMOVE -> banner -> {
            List<Pattern> current = banner.getPatterns();
            current.removeAll(patterns);
            banner.setPatterns(current);
         };
         default -> banner -> {};
      };
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE -> this.patternNumber != null ? (Class[])CollectionUtils.array(Pattern.class) : (Class[])CollectionUtils.array(Pattern[].class);
         case ADD, REMOVE -> this.patternNumber != null ? null : (Class[])CollectionUtils.array(Pattern[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int placement = 0;
      if (this.patternNumber != null) {
         Integer patternNum = this.patternNumber.getSingle(event);
         if (patternNum != null) {
            placement = patternNum;
         }
      }

      List<Pattern> patterns = (List<Pattern>)(delta != null ? Arrays.stream(delta).map(Pattern.class::cast).toList() : new ArrayList<>());
      Consumer<BannerMeta> metaChanger;
      Consumer<Banner> blockChanger;
      if (placement >= 1) {
         Pattern pattern = patterns.size() == 1 ? patterns.get(0) : null;
         metaChanger = this.getPlacementMetaChanger(mode, placement, pattern);
         blockChanger = this.getPlacementBlockChanger(mode, placement, pattern);
      } else {
         metaChanger = this.getAllMetaChanger(mode, patterns);
         blockChanger = this.getAllBlockChanger(mode, patterns);
      }

      for (Object object : this.objects.getArray(event)) {
         if (object instanceof Block block && block.getState() instanceof Banner banner) {
            blockChanger.accept(banner);
            banner.update(true, false);
         } else {
            ItemStack itemStack = ItemUtils.asItemStack(object);
            if (itemStack != null && itemStack.getItemMeta() instanceof BannerMeta bannerMeta) {
               metaChanger.accept(bannerMeta);
               itemStack.setItemMeta(bannerMeta);
               if (object instanceof Slot slot) {
                  slot.setItem(itemStack);
               } else if (object instanceof ItemType itemType) {
                  itemType.setItemMeta(bannerMeta);
               } else if (object instanceof ItemStack itemStack1) {
                  itemStack1.setItemMeta(bannerMeta);
               }
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.patternNumber != null && this.getExpr().isSingle();
   }

   @Override
   public Class<Pattern> getReturnType() {
      return Pattern.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.patternNumber != null) {
         builder.append("banner pattern", this.patternNumber);
      } else {
         builder.append("banner patterns");
      }

      builder.append("of", this.objects);
      return builder.toString();
   }

   static {
      Skript.registerExpression(
         ExprBannerPatterns.class,
         Pattern.class,
         ExpressionType.PROPERTY,
         "[all [[of] the]|the] banner pattern[s] of %itemstacks/itemtypes/slots/blocks%",
         "%itemstacks/itemtypes/slots/blocks%'[s] banner pattern[s]",
         "[the] %integer%[st|nd|rd|th] [banner] pattern of %itemstacks/itemtypes/slots/blocks%",
         "%itemstacks/itemtypes/slots/blocks%'[s] %integer%[st|nd|rd|th] [banner] pattern"
      );
   }
}

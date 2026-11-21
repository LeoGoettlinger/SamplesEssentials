package org.skriptlang.skript.lang.structure;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.ConsumingIterator;
import java.util.Arrays;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Structure implements SyntaxElement, Debuggable {
   public static final Structure.Priority DEFAULT_PRIORITY = new Structure.Priority(1000);
   @Nullable
   private EntryContainer entryContainer = null;

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public final EntryContainer getEntryContainer() {
      if (this.entryContainer == null) {
         throw new IllegalStateException("This Structure hasn't been initialized!");
      } else {
         return this.entryContainer;
      }
   }

   @Override
   public final boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Literal<?>[] literals = Arrays.copyOf(expressions, expressions.length, Literal[].class);
      Structure.StructureData structureData = this.getParser().getData(Structure.StructureData.class);
      StructureInfo<? extends Structure> structureInfo = structureData.structureInfo;

      assert structureInfo != null;

      if (structureData.node instanceof SimpleNode) {
         return this.init(literals, matchedPattern, parseResult, null);
      } else {
         EntryValidator entryValidator = structureInfo.entryValidator;
         if (entryValidator == null) {
            this.entryContainer = EntryContainer.withoutValidator((SectionNode)structureData.node);
         } else {
            EntryContainer entryContainer = entryValidator.validate((SectionNode)structureData.node);
            if (entryContainer == null) {
               return false;
            }

            this.entryContainer = entryContainer;
         }

         return this.init(literals, matchedPattern, parseResult, this.entryContainer);
      }
   }

   public abstract boolean init(Literal<?>[] var1, int var2, SkriptParser.ParseResult var3, @UnknownNullability EntryContainer var4);

   public boolean preLoad() {
      return true;
   }

   public abstract boolean load();

   public boolean postLoad() {
      return true;
   }

   public void unload() {
   }

   public void postUnload() {
   }

   public Structure.Priority getPriority() {
      return DEFAULT_PRIORITY;
   }

   @Override
   public String toString() {
      return this.toString(null, false);
   }

   @NotNull
   @Override
   public String getSyntaxTypeName() {
      return "structure";
   }

   @Nullable
   public static Structure parse(String expr, Node node, @Nullable String defaultError) {
      if (!(node instanceof SimpleNode) && !(node instanceof SectionNode)) {
         throw new IllegalArgumentException("only simple or section nodes may be parsed as a structure");
      } else {
         ParserInstance.get().getData(Structure.StructureData.class).node = node;
         Iterator<DefaultSyntaxInfos.Structure<?>> iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.STRUCTURE).iterator();
         CheckedIterator var9;
         if (node instanceof SimpleNode) {
            var9 = new CheckedIterator<>(iterator, info -> info != null && info.nodeType().canBeSimple());
         } else {
            var9 = new CheckedIterator<>(iterator, info -> info != null && info.nodeType().canBeSection());
         }

         Iterator<DefaultSyntaxInfos.Structure<?>> var10 = new ConsumingIterator<>(
            var9, info -> ParserInstance.get().getData(Structure.StructureData.class).structureInfo = SyntaxElementInfo.fromModern(info)
         );

         Object var6;
         try (ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler()) {
            Structure structure = SkriptParser.parseStatic(expr, var10, ParseContext.EVENT, defaultError);
            if (structure != null) {
               parseLogHandler.printLog();
               return structure;
            }

            parseLogHandler.printError();
            var6 = null;
         }

         return (Structure)var6;
      }
   }

   @Nullable
   public static Structure parse(String expr, Node node, @Nullable String defaultError, Iterator<? extends StructureInfo<? extends Structure>> iterator) {
      if (!(node instanceof SimpleNode) && !(node instanceof SectionNode)) {
         throw new IllegalArgumentException("only simple or section nodes may be parsed as a structure");
      } else {
         ParserInstance.get().getData(Structure.StructureData.class).node = node;
         CheckedIterator var9;
         if (node instanceof SimpleNode) {
            var9 = new CheckedIterator<>(iterator, item -> item != null && item.nodeType.canBeSimple());
         } else {
            var9 = new CheckedIterator<>(iterator, item -> item != null && item.nodeType.canBeSection());
         }

         Iterator<? extends StructureInfo<? extends Structure>> var10 = new ConsumingIterator<>(
            var9, elementInfo -> ParserInstance.get().getData(Structure.StructureData.class).structureInfo = elementInfo
         );

         Object var6;
         try (ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler()) {
            Structure structure = SkriptParser.parseStatic(expr, var10, ParseContext.EVENT, defaultError);
            if (structure != null) {
               parseLogHandler.printLog();
               return structure;
            }

            parseLogHandler.printError();
            var6 = null;
         }

         return (Structure)var6;
      }
   }

   static {
      ParserInstance.registerData(Structure.StructureData.class, Structure.StructureData::new);
   }

   public static class Priority implements Comparable<Structure.Priority> {
      private final int priority;

      public Priority(int priority) {
         this.priority = priority;
      }

      public int getPriority() {
         return this.priority;
      }

      public int compareTo(@NotNull Structure.Priority o) {
         return Integer.compare(this.priority, o.priority);
      }
   }

   public static class StructureData extends ParserInstance.Data {
      @Internal
      public Node node;
      @Internal
      @Nullable
      public StructureInfo<? extends Structure> structureInfo;

      public StructureData(ParserInstance parserInstance) {
         super(parserInstance);
      }

      @Nullable
      public StructureInfo<? extends Structure> getStructureInfo() {
         return this.structureInfo;
      }
   }
}

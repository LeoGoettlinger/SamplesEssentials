package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.ColorHandler;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NonNull;

public class Parsers implements Iterable<Parser> {
   private final ColorHandler<?> instance;
   private final Set<Parser> parsers;

   public Parsers(ColorHandler<?> instance, Set<Parser> parsers) {
      this.instance = instance;
      this.parsers = parsers;
   }

   public Parsers(ColorHandler<?> instance) {
      this(instance, new HashSet<>());
   }

   public Parsers parser(Parser parser) {
      this.parsers.add(parser);
      return this;
   }

   public Parsers parsers(Collection<Parser> parsers) {
      this.parsers.addAll(parsers);
      return this;
   }

   public Parsers ofType(ParserType type) {
      this.instance.allParsers().stream().filter(parser -> Objects.equals(parser.getType(), type)).forEach(this.parsers::add);
      return this;
   }

   public Parsers color() {
      return this.ofType(ParserType.COLOR);
   }

   public Parsers sound() {
      return this.ofType(ParserType.SOUND);
   }

   public Parsers particle() {
      return this.ofType(ParserType.PARTICLE);
   }

   public Parsers interaction() {
      return this.ofType(ParserType.INTERACTION);
   }

   public Parsers placeholder() {
      return this.ofType(ParserType.PLACEHOLDER);
   }

   public Parsers textFormatting() {
      return this.ofType(ParserType.TEXT_FORMATTING);
   }

   public Parsers defaults() {
      return this.instance.settings().defaultParsers().apply(this);
   }

   public Parsers all() {
      this.parsers.addAll(this.instance.allParsers());
      return this;
   }

   public List<Parser> values() {
      return this.parsers.stream().sorted(comparingPriority()).toList();
   }

   @NonNull
   @Override
   public Iterator<Parser> iterator() {
      return this.parsers.stream().sorted(comparingPriority()).iterator();
   }

   public static Comparator<Parser> comparingPriority() {
      return (c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority());
   }
}

package org.snakeyaml.engine.v2.api.lowlevel;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

public class Compose {
   private final LoadSettings settings;

   public Compose(LoadSettings settings) {
      Objects.requireNonNull(settings, "LoadSettings cannot be null");
      this.settings = settings;
   }

   public Optional<Node> composeReader(Reader yaml) {
      Objects.requireNonNull(yaml, "Reader cannot be null");
      return new Composer(this.settings, new ParserImpl(this.settings, new StreamReader(this.settings, yaml))).getSingleNode();
   }

   public Optional<Node> composeInputStream(InputStream yaml) {
      Objects.requireNonNull(yaml, "InputStream cannot be null");
      return new Composer(this.settings, new ParserImpl(this.settings, new StreamReader(this.settings, new YamlUnicodeReader(yaml)))).getSingleNode();
   }

   public Optional<Node> composeString(String yaml) {
      Objects.requireNonNull(yaml, "String cannot be null");
      return new Composer(this.settings, new ParserImpl(this.settings, new StreamReader(this.settings, new StringReader(yaml)))).getSingleNode();
   }

   public Iterable<Node> composeAllFromReader(Reader yaml) {
      Objects.requireNonNull(yaml, "Reader cannot be null");
      return () -> new Composer(this.settings, new ParserImpl(this.settings, new StreamReader(this.settings, yaml)));
   }

   public Iterable<Node> composeAllFromInputStream(InputStream yaml) {
      Objects.requireNonNull(yaml, "InputStream cannot be null");
      return () -> new Composer(this.settings, new ParserImpl(this.settings, new StreamReader(this.settings, new YamlUnicodeReader(yaml))));
   }

   public Iterable<Node> composeAllFromString(final String yaml) {
      Objects.requireNonNull(yaml, "String cannot be null");
      return new Iterable<Node>() {
         @Override
         public Iterator<Node> iterator() {
            return new Composer(Compose.this.settings, new ParserImpl(Compose.this.settings, new StreamReader(Compose.this.settings, new StringReader(yaml))));
         }
      };
   }
}

package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Experimental
public final class Verbose {
   private static final ConcurrentMap<String, Verbose> INTERNED = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, CopyOnWriteArrayList<Verbose>> DECLARED_BY_CLASS = new ConcurrentHashMap<>();
   private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
   @NotNull
   private final String template;
   private final int version;
   @Nullable
   private volatile Verbose.Parsed parsed;

   private Verbose(@NotNull String template) {
      this.template = template;
      this.version = versionOf(template);
   }

   @NotNull
   public static Verbose of(@NotNull String template) {
      if (template.isEmpty()) {
         throw new IllegalArgumentException("template");
      } else {
         Verbose verbose = intern(template);
         declareFor(declarationCaller(), verbose);
         return verbose;
      }
   }

   @NotNull
   public Verbose or(@NotNull String nextShape) {
      if (nextShape.isEmpty()) {
         throw new IllegalArgumentException("nextShape");
      } else {
         Class<?> owner = declarationCaller();
         Verbose combined = intern(this.template + "|" + nextShape);
         replaceDeclaration(owner, this, combined);
         return combined;
      }
   }

   @ApiStatus.Internal
   @NotNull
   public static List<Verbose> declaredBy(@NotNull Class<?> type, @NotNull Class<?> rootType) {
      List<Verbose> templates = new ArrayList<>(1);

      for (Class<?> cursor = type; cursor != null && rootType.isAssignableFrom(cursor); cursor = cursor.getSuperclass()) {
         List<Verbose> declared = DECLARED_BY_CLASS.get(cursor);
         if (declared != null) {
            for (Verbose verbose : declared) {
               if (!templates.contains(verbose)) {
                  templates.add(verbose);
               }
            }
         }

         if (cursor == rootType) {
            break;
         }
      }

      return List.copyOf(templates);
   }

   @NotNull
   public String template() {
      return this.template;
   }

   public int shapes() {
      return this.parsed().cases.size();
   }

   @NotNull
   public List<VerboseSchema.Field> shapeFields(int shape) {
      return this.parsed().cases.get(shape).fields();
   }

   public int version() {
      return this.version;
   }

   @NotNull
   public VerboseSchema schema() {
      return this.parsed().schema;
   }

   @NotNull
   public byte[] layoutBytes() {
      return VerboseSchema.encodeLayout(this.parsed().schema.fields(), this.template);
   }

   @NotNull
   public Verbose.Writer write(@NotNull VerboseBuf buf) {
      Verbose.Parsed parsed = this.parsed();
      if (parsed.cases.size() > 1) {
         throw new IllegalStateException("verbose template \"" + this.template + "\" defines " + parsed.cases.size() + " shapes; use write(buf, shape)");
      } else {
         return new Verbose.Writer(this, buf.clear(), parsed.cases.get(0).fields());
      }
   }

   @NotNull
   public Verbose.Writer write(@NotNull VerboseBuf buf, int shape) {
      Verbose.Parsed parsed = this.parsed();
      if (parsed.cases.size() <= 1) {
         throw new IllegalStateException("verbose template \"" + this.template + "\" defines one shape; use write(buf)");
      } else if (shape >= 0 && shape < parsed.cases.size()) {
         VerboseBuf cleared = buf.clear();
         cleared.vi(shape);
         return new Verbose.Writer(this, cleared, parsed.cases.get(shape).fields());
      } else {
         throw new IllegalArgumentException("verbose template \"" + this.template + "\" has shapes 0.." + (parsed.cases.size() - 1) + "; got " + shape);
      }
   }

   @NotNull
   public String render(@NotNull byte[] data, @NotNull VerboseRenderContext ctx) {
      StringBuilder out = new StringBuilder(this.template.length() + 16);

      try {
         this.renderTo(VerboseBuf.wrap(data), ctx, out);
      } catch (Throwable var5) {
         return "";
      }

      return out.toString();
   }

   @Nullable
   public static String renderStored(@NotNull String template, @NotNull byte[] data, @NotNull VerboseRenderContext ctx) {
      if (template.isEmpty()) {
         return null;
      } else {
         try {
            Verbose verbose = intern(template);
            StringBuilder out = new StringBuilder(template.length() + 16);
            verbose.renderTo(VerboseBuf.wrap(data), ctx, out);
            return out.toString();
         } catch (Throwable var5) {
            return null;
         }
      }
   }

   @NotNull
   public VerboseFormatter asFormatter() {
      return new VerboseFormatter() {
         @Override
         public int version() {
            return Verbose.this.version;
         }

         @Override
         public void render(@NotNull VerboseBuf in, @NotNull VerboseRenderContext ctx, @NotNull VerboseSink out) {
            StringBuilder text = new StringBuilder(Verbose.this.template.length() + 16);
            Verbose.this.renderTo(in, ctx, text);
            out.text(text);
         }
      };
   }

   private void renderTo(@NotNull VerboseBuf in, @NotNull VerboseRenderContext ctx, @NotNull StringBuilder out) {
      Verbose.Parsed parsed = this.parsed();
      List<Verbose.Node> nodes;
      if (parsed.cases.size() > 1) {
         int shape = in.rvi();
         if (shape >= parsed.cases.size()) {
            throw new IllegalArgumentException("payload shape " + shape + " out of range for template \"" + this.template + "\"");
         }

         nodes = parsed.cases.get(shape).nodes();
      } else {
         nodes = parsed.cases.get(0).nodes();
      }

      renderNodes(nodes, in, ctx, out, new StringBuilder(), true);
   }

   private static void renderNodes(
      @NotNull List<Verbose.Node> nodes,
      @NotNull VerboseBuf in,
      @NotNull VerboseRenderContext ctx,
      @NotNull StringBuilder out,
      @NotNull StringBuilder scratch,
      boolean active
   ) {
      for (Verbose.Node node : nodes) {
         if (node instanceof Verbose.Lit lit) {
            if (active) {
               out.append(lit.text());
            }
         } else if (node instanceof Verbose.Field field) {
            StringBuilder target = active ? out : scratch;
            if (!active) {
               scratch.setLength(0);
            }

            field.tag().renderer().render(in, ctx, target, field.fmt());
         } else if (node instanceof Verbose.Group group) {
            boolean gate = in.rbool();
            renderNodes(group.whenTrue(), in, ctx, out, scratch, active && gate);
            renderNodes(group.whenFalse(), in, ctx, out, scratch, active && !gate);
         }
      }
   }

   @NotNull
   private Verbose.Parsed parsed() {
      Verbose.Parsed cached = this.parsed;
      if (cached == null) {
         cached = Verbose.Parser.parse(this.template, this.version);
         this.parsed = cached;
      }

      return cached;
   }

   @NotNull
   private static Verbose intern(@NotNull String template) {
      return INTERNED.computeIfAbsent(template, Verbose::new);
   }

   @Nullable
   private static Class<?> declarationCaller() {
      return STACK_WALKER.walk(frames -> frames.map(StackFrame::getDeclaringClass).filter(type -> type != Verbose.class).findFirst().orElse(null));
   }

   private static void declareFor(@Nullable Class<?> owner, @NotNull Verbose verbose) {
      if (owner != null) {
         DECLARED_BY_CLASS.computeIfAbsent(owner, ignored -> new CopyOnWriteArrayList<>()).addIfAbsent(verbose);
      }
   }

   private static void replaceDeclaration(@Nullable Class<?> owner, @NotNull Verbose previous, @NotNull Verbose replacement) {
      if (owner != null) {
         CopyOnWriteArrayList<Verbose> templates = DECLARED_BY_CLASS.computeIfAbsent(owner, ignored -> new CopyOnWriteArrayList<>());
         templates.remove(previous);
         templates.addIfAbsent(replacement);
      }
   }

   private static int versionOf(@NotNull String template) {
      int hash = -2128831035;

      for (int i = 0; i < template.length(); i++) {
         hash ^= template.charAt(i);
         hash *= 16777619;
      }

      hash &= Integer.MAX_VALUE;
      return hash == 0 ? 1 : hash;
   }

   private record Case(@NotNull List<Verbose.Node> nodes, @NotNull List<VerboseSchema.Field> fields) {
   }

   private record Field(@NotNull VerboseTags.Tag tag, @Nullable String fmt) implements Verbose.Node {
   }

   private record Group(@NotNull List<Verbose.Node> whenTrue, @NotNull List<Verbose.Node> whenFalse) implements Verbose.Node {
   }

   private record Lit(@NotNull String text) implements Verbose.Node {
   }

   private sealed interface Node permits Verbose.Lit, Verbose.Field, Verbose.Group {
   }

   private record Parsed(@NotNull VerboseSchema schema, @NotNull List<Verbose.Case> cases) {
   }

   private static final class Parser {
      @NotNull
      private final String template;
      private int pos;

      private Parser(@NotNull String template) {
         this.template = template;
      }

      @NotNull
      static Verbose.Parsed parse(@NotNull String template, int version) {
         Verbose.Parser parser = new Verbose.Parser(template);
         List<Verbose.Case> cases = new ArrayList<>();
         int[] groupSeq = new int[1];

         while (true) {
            List<Verbose.Node> nodes = parser.parseNodes(false);
            List<VerboseSchema.Field> fields = new ArrayList<>();
            flatten(nodes, fields, groupSeq);
            cases.add(new Verbose.Case(nodes, List.copyOf(fields)));
            if (parser.pos >= template.length()) {
               nodes = new ArrayList<>();
               if (cases.size() > 1) {
                  nodes.add(new VerboseSchema.Field("shape", VerboseSchema.TypeTag.VI));

                  for (Verbose.Case c : cases) {
                     nodes.addAll(c.fields());
                  }
               } else {
                  nodes.addAll(cases.get(0).fields());
                  if (nodes.isEmpty()) {
                     throw new IllegalArgumentException("verbose template declares no fields: " + template);
                  }
               }

               return new Verbose.Parsed(VerboseSchema.of(version, nodes), List.copyOf(cases));
            }

            char c = template.charAt(parser.pos);
            if (c != '|') {
               throw new IllegalArgumentException("unbalanced ']' at " + parser.pos + " in: " + template);
            }

            parser.pos++;
         }
      }

      @NotNull
      private List<Verbose.Node> parseNodes(boolean inGroup) {
         List<Verbose.Node> nodes = new ArrayList<>();
         StringBuilder literal = new StringBuilder();

         while (this.pos < this.template.length()) {
            char c = this.template.charAt(this.pos);
            if (c == '\\') {
               if (this.pos + 1 >= this.template.length()) {
                  throw new IllegalArgumentException("dangling escape in: " + this.template);
               }

               literal.append(this.template.charAt(this.pos + 1));
               this.pos += 2;
            } else if (c == '{') {
               flushLiteral(nodes, literal);
               nodes.add(this.parsePlaceholder());
            } else if (c == '[') {
               flushLiteral(nodes, literal);
               this.pos++;
               List<Verbose.Node> whenTrue = this.parseNodes(true);
               List<Verbose.Node> whenFalse = List.of();
               if (this.pos < this.template.length() && this.template.charAt(this.pos) == '|') {
                  this.pos++;
                  whenFalse = this.parseNodes(true);
               }

               if (this.pos >= this.template.length() || this.template.charAt(this.pos) != ']') {
                  throw new IllegalArgumentException("unclosed '[' in: " + this.template);
               }

               this.pos++;
               nodes.add(new Verbose.Group(whenTrue, whenFalse));
            } else {
               if (c != '|' && (!inGroup || c != ']')) {
                  if (c == '}') {
                     throw new IllegalArgumentException("unbalanced '}' at " + this.pos + " in: " + this.template);
                  }

                  literal.append(c);
                  this.pos++;
                  continue;
               }
               break;
            }
         }

         flushLiteral(nodes, literal);
         return List.copyOf(nodes);
      }

      @NotNull
      private Verbose.Node parsePlaceholder() {
         int close = this.template.indexOf(125, this.pos);
         if (close < 0) {
            throw new IllegalArgumentException("unclosed '{' at " + this.pos + " in: " + this.template);
         } else {
            String body = this.template.substring(this.pos + 1, close);
            this.pos = close + 1;
            int colon = body.indexOf(58);
            String name = colon < 0 ? body : body.substring(0, colon);
            String fmt = colon < 0 ? null : body.substring(colon + 1);
            if (name.isEmpty()) {
               throw new IllegalArgumentException("empty placeholder in: " + this.template);
            } else {
               VerboseTags.Tag tag = VerboseTags.get(name);
               if (tag == null) {
                  throw new IllegalArgumentException("unknown verbose tag {" + name + "} in: " + this.template);
               } else {
                  return new Verbose.Field(tag, fmt);
               }
            }
         }
      }

      private static void flushLiteral(@NotNull List<Verbose.Node> nodes, @NotNull StringBuilder literal) {
         if (literal.length() > 0) {
            nodes.add(new Verbose.Lit(literal.toString()));
            literal.setLength(0);
         }
      }

      private static void flatten(@NotNull List<Verbose.Node> nodes, @NotNull List<VerboseSchema.Field> fields, @NotNull int[] groupSeq) {
         String pendingLabel = null;

         for (Verbose.Node node : nodes) {
            if (node instanceof Verbose.Lit lit) {
               pendingLabel = labelFrom(lit.text());
            } else if (!(node instanceof Verbose.Field field)) {
               if (node instanceof Verbose.Group group) {
                  fields.add(new VerboseSchema.Field("case." + groupSeq[0]++, VerboseSchema.TypeTag.BOOL));
                  flatten(group.whenTrue(), fields, groupSeq);
                  flatten(group.whenFalse(), fields, groupSeq);
                  pendingLabel = null;
               }
            } else {
               List<VerboseSchema.TypeTag> wire = field.tag().wire();
               String base = pendingLabel != null ? pendingLabel : field.tag().name() + "." + fields.size();

               for (int i = 0; i < wire.size(); i++) {
                  String name = wire.size() == 1 ? base : base + "." + i;
                  fields.add(new VerboseSchema.Field(name, wire.get(i)));
               }

               pendingLabel = null;
            }
         }
      }

      @Nullable
      private static String labelFrom(@NotNull String literal) {
         int end = literal.length();
         if (end == 0) {
            return null;
         } else {
            if (literal.charAt(end - 1) == '=') {
               end--;
            }

            int start = end;

            while (start > 0 && isLabelChar(literal.charAt(start - 1))) {
               start--;
            }

            return start == end ? null : literal.substring(start, end);
         }
      }

      private static boolean isLabelChar(char c) {
         return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-';
      }
   }

   public static final class Writer {
      private static final int MC_POS_LIMIT = 33554431;
      @NotNull
      private final Verbose owner;
      @NotNull
      private final VerboseBuf buf;
      @NotNull
      private final List<VerboseSchema.Field> fields;
      private int next;

      private Writer(@NotNull Verbose owner, @NotNull VerboseBuf buf, @NotNull List<VerboseSchema.Field> fields) {
         this.owner = owner;
         this.buf = buf;
         this.fields = fields;
      }

      @NotNull
      public Verbose.Writer f64(double v) {
         this.expect(VerboseSchema.TypeTag.F64);
         this.buf.f64(v);
         return this;
      }

      @NotNull
      public Verbose.Writer f32(float v) {
         this.expect(VerboseSchema.TypeTag.F32);
         this.buf.f32(v);
         return this;
      }

      @NotNull
      public Verbose.Writer uint(int v) {
         this.expect(VerboseSchema.TypeTag.VI);
         this.buf.vi(v);
         return this;
      }

      @NotNull
      public Verbose.Writer sint(int v) {
         this.expect(VerboseSchema.TypeTag.ZZ);
         this.buf.zz(v);
         return this;
      }

      @NotNull
      public Verbose.Writer ulong(long v) {
         this.expect(VerboseSchema.TypeTag.VL);
         this.buf.vl(v);
         return this;
      }

      @NotNull
      public Verbose.Writer bool(boolean v) {
         this.expect(VerboseSchema.TypeTag.BOOL);
         this.buf.bool(v);
         return this;
      }

      @NotNull
      public Verbose.Writer str(@NotNull CharSequence v) {
         this.expect(VerboseSchema.TypeTag.STR);
         this.buf.str(v);
         return this;
      }

      @NotNull
      public Verbose.Writer mcPos(int x, int y, int z) {
         if (x >= -33554431 && x <= 33554431 && z >= -33554431 && z <= 33554431) {
            this.expect(VerboseSchema.TypeTag.VL);
            this.expect(VerboseSchema.TypeTag.ZZ);
            this.buf.vl(VerboseTags.packMcBlockXZ(x, z)).zz(y);
            return this;
         } else {
            throw new IllegalArgumentException("mcPos x/z out of 26-bit range: " + x + ", " + z);
         }
      }

      @NotNull
      public Verbose.Writer cursor(float x, float y, float z) {
         this.expect(VerboseSchema.TypeTag.F32);
         this.expect(VerboseSchema.TypeTag.F32);
         this.expect(VerboseSchema.TypeTag.F32);
         this.buf.f32(x).f32(y).f32(z);
         return this;
      }

      @NotNull
      public Verbose.Writer slong(long v) {
         this.expect(VerboseSchema.TypeTag.ZZ);
         this.expect(VerboseSchema.TypeTag.ZZ);
         this.buf.zz((int)(v >> 32)).zz((int)v);
         return this;
      }

      @NotNull
      public Verbose verbose() {
         return this.owner;
      }

      @NotNull
      public VerboseBuf end() {
         if (this.next != this.fields.size()) {
            throw new IllegalStateException(
               "verbose template \"" + this.owner.template + "\" expects " + this.fields.size() + " fields but " + this.next + " were written"
            );
         } else {
            return this.buf;
         }
      }

      private void expect(@NotNull VerboseSchema.TypeTag actual) {
         if (this.next >= this.fields.size()) {
            throw new IllegalStateException(
               "verbose template \"" + this.owner.template + "\" declares " + this.fields.size() + " fields; extra " + actual.wireName() + " write"
            );
         } else {
            VerboseSchema.Field expected = this.fields.get(this.next);
            if (expected.type() != actual) {
               throw new IllegalStateException(
                  "verbose template \""
                     + this.owner.template
                     + "\" field "
                     + this.next
                     + " ("
                     + expected.name()
                     + ") is "
                     + expected.type().wireName()
                     + " but callsite wrote "
                     + actual.wireName()
               );
            } else {
               this.next++;
            }
         }
      }
   }
}

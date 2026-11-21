package su.nightexpress.nightcore.command;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.NumberUtil;

@Deprecated
public class CommandResult {
   private final String label;
   private final String[] args;
   private final Map<CommandFlag<?>, String> flags;

   public CommandResult(@NonNull String label, String[] args, @NonNull Map<CommandFlag<?>, StringBuilder> flags) {
      this.label = label;
      this.flags = new HashMap<>();
      this.args = args;
      flags.forEach((flag, content) -> this.flags.put((CommandFlag<?>)flag, content.toString()));
   }

   public int length() {
      return this.args.length;
   }

   @NonNull
   public String getArg(int index) {
      return this.getArgs()[index];
   }

   @NonNull
   public String getArg(int index, @NonNull String def) {
      return index >= this.length() ? def : this.getArgs()[index];
   }

   public int getInt(int index, int def) {
      return NumberUtil.getAnyInteger(this.getArg(index, ""), def);
   }

   public double getDouble(int index, double def) {
      return NumberUtil.getAnyDouble(this.getArg(index, ""), def);
   }

   public boolean hasFlag(@NonNull CommandFlag<?> flag) {
      return this.getFlags().containsKey(flag);
   }

   @Nullable
   public <T> T getFlag(@NonNull CommandFlag<T> flag) {
      String value = this.getFlags().get(flag);
      return value == null ? null : flag.getParser().apply(value);
   }

   @NonNull
   public <T> T getFlag(@NonNull CommandFlag<T> flag, @NonNull T def) {
      T value = this.getFlag(flag);
      return value == null ? def : value;
   }

   @NonNull
   public String getLabel() {
      return this.label;
   }

   public String[] getArgs() {
      return this.args;
   }

   @NonNull
   public Map<CommandFlag<?>, String> getFlags() {
      return this.flags;
   }
}

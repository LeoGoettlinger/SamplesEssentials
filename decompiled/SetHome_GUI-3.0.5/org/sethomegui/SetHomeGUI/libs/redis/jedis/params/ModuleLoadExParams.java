package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.KeyValue;

public class ModuleLoadExParams implements IParams {
   private final List<KeyValue<String, String>> configs = new ArrayList<>();
   private final List<String> args = new ArrayList<>();

   public ModuleLoadExParams moduleLoadexParams() {
      return new ModuleLoadExParams();
   }

   public ModuleLoadExParams config(String name, String value) {
      this.configs.add(KeyValue.of(name, value));
      return this;
   }

   public ModuleLoadExParams arg(String arg) {
      this.args.add(arg);
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      this.configs.forEach(kv -> args.add((Rawable)Protocol.Keyword.CONFIG).add(kv.getKey()).add(kv.getValue()));
      if (!this.args.isEmpty()) {
         args.add((Rawable)Protocol.Keyword.ARGS).addObjects(this.args);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ModuleLoadExParams that = (ModuleLoadExParams)o;
         return Objects.equals(this.configs, that.configs) && Objects.equals(this.args, that.args);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.configs, this.args);
   }
}

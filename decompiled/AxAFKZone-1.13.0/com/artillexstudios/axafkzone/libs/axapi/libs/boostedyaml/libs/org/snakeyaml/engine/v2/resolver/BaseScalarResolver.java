package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.resolver;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class BaseScalarResolver implements ScalarResolver {
   public static final Pattern EMPTY = Pattern.compile("^$");
   public static final Pattern ENV_FORMAT = Pattern.compile("^\\$\\{\\s*(?:(\\w+)(?:(:?[-?])(\\w+)?)?)\\s*\\}$");
   protected Map<Character, List<ResolverTuple>> yamlImplicitResolvers = new HashMap<>();

   public BaseScalarResolver() {
      this.addImplicitResolvers();
   }

   public void addImplicitResolver(Tag tag, Pattern regexp, String first) {
      if (first == null) {
         List<ResolverTuple> curr = this.yamlImplicitResolvers.computeIfAbsent(null, c -> new ArrayList<>());
         curr.add(new ResolverTuple(tag, regexp));
      } else {
         char[] chrs = first.toCharArray();
         int i = 0;

         for (int j = chrs.length; i < j; i++) {
            Character theC = chrs[i];
            if (theC == 0) {
               theC = null;
            }

            List<ResolverTuple> curr = this.yamlImplicitResolvers.get(theC);
            if (curr == null) {
               curr = new ArrayList<>();
               this.yamlImplicitResolvers.put(theC, curr);
            }

            curr.add(new ResolverTuple(tag, regexp));
         }
      }
   }

   abstract void addImplicitResolvers();

   @Override
   public Tag resolve(String value, Boolean implicit) {
      if (!implicit) {
         return Tag.STR;
      } else {
         List<ResolverTuple> resolvers;
         if (value.isEmpty()) {
            resolvers = this.yamlImplicitResolvers.get('\u0000');
         } else {
            resolvers = this.yamlImplicitResolvers.get(value.charAt(0));
         }

         if (resolvers != null) {
            for (ResolverTuple v : resolvers) {
               Tag tag = v.getTag();
               Pattern regexp = v.getRegexp();
               if (regexp.matcher(value).matches()) {
                  return tag;
               }
            }
         }

         if (this.yamlImplicitResolvers.containsKey(null)) {
            for (ResolverTuple vx : this.yamlImplicitResolvers.get(null)) {
               Tag tag = vx.getTag();
               Pattern regexp = vx.getRegexp();
               if (regexp.matcher(value).matches()) {
                  return tag;
               }
            }
         }

         return Tag.STR;
      }
   }
}

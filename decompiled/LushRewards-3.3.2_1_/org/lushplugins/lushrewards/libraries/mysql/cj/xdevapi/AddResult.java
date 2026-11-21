package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.List;

public interface AddResult extends Result {
   List<String> getGeneratedIds();
}

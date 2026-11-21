package de.theredend2000.advancedhunt.lib.cronutils.model;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.CronMapper;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import java.io.Serializable;
import java.util.Map;

public interface Cron extends Serializable {
   CronField retrieve(CronFieldName var1);

   Map<CronFieldName, CronField> retrieveFieldsAsMap();

   String asString();

   CronDefinition getCronDefinition();

   Cron validate();

   boolean equivalent(CronMapper var1, Cron var2);

   boolean equivalent(Cron var1);
}

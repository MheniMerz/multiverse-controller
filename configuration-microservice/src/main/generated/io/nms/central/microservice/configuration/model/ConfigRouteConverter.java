package io.nms.central.microservice.configuration.model;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.nms.central.microservice.configuration.model.ConfigRoute}.
 * NOTE: This class has been automatically generated from the {@link io.nms.central.microservice.configuration.model.ConfigRoute} original class using Vert.x codegen.
 */
public class ConfigRouteConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ConfigRoute obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "cost":
          if (member.getValue() instanceof Number) {
            obj.setCost(((Number)member.getValue()).intValue());
          }
          break;
        case "nextHop":
          if (member.getValue() instanceof Number) {
            obj.setNextHop(((Number)member.getValue()).intValue());
          }
          break;
        case "origin":
          if (member.getValue() instanceof String) {
            obj.setOrigin((String)member.getValue());
          }
          break;
        case "prefix":
          if (member.getValue() instanceof String) {
            obj.setPrefix((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(ConfigRoute obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ConfigRoute obj, java.util.Map<String, Object> json) {
    json.put("cost", obj.getCost());
    json.put("nextHop", obj.getNextHop());
    if (obj.getOrigin() != null) {
      json.put("origin", obj.getOrigin());
    }
    if (obj.getPrefix() != null) {
      json.put("prefix", obj.getPrefix());
    }
  }
}

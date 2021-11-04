package io.nms.central.microservice.digitaltwin;

import java.util.List;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A service interface managing DigitalTwin.
 * <p>
 * This service is an event bus service (aka. service proxy)
 * </p>
 */
@VertxGen
@ProxyGen
public interface DigitalTwinService {

	/**
	 * The name of the event bus service.
	 */
	String SERVICE_NAME = "digitaltwin-eb-service";

	/**
	 * The address on which the service is published.
	 */
	String SERVICE_ADDRESS = "service.digitaltwin";
	
	String FROTNEND_ADDRESS = "mvs.to.frontend";

	String EVENT_ADDRESS = "digitaltwin.event";
	
	@Fluent	
	DigitalTwinService initializePersistence(Handler<AsyncResult<Void>> resultHandler);
}
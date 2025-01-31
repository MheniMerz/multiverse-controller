package io.nms.central.microservice.qnet;

import io.nms.central.microservice.common.BaseMicroserviceVerticle;
import io.nms.central.microservice.topology.TopologyService;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

public class EventHandler extends BaseMicroserviceVerticle {

	private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	
	private final QnetService qnetService;
	private WebClient webClient;

	public EventHandler(QnetService qnetService) {
		this.qnetService = qnetService;
		webClient = WebClient.create(vertx);
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {
		super.start(promise);
		vertx.eventBus().consumer(TopologyService.EVENT_ADDRESS, ar -> {
		});
	}

	private void handleTopologyEvent() {
	}
	
	private void notifyFrontend() {
		vertx.eventBus().publish(QnetService.FROTNEND_ADDRESS, new JsonObject());
	}
	 
	private void notifyQnetChange() {
		vertx.eventBus().publish(QnetService.EVENT_ADDRESS, new JsonObject());
	}
}

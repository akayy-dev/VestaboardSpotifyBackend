package com.vesta.rest_api.patterns;

import com.vesta.rest_api.events.EventPayload;

public interface Observer {
	public void update(EventPayload<?> event);
}

package com.example.rest_api.patterns;

import com.example.rest_api.events.EventPayload;

public interface Observer {
	public void update(EventPayload<?> event);
}

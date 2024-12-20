package com.vesta.rest_api.events;

public class EventPayload<T> {
	private T payload;
	private ObservableEvents event;

	public EventPayload(ObservableEvents event, T payload) {
		this.event = event;
		this.payload = payload;
	}

	public EventPayload(ObservableEvents event) {
		this.event = event;
	}

	public ObservableEvents getType() {
		return event;
	}

	public T getPayload() {
		return payload;
	}
}

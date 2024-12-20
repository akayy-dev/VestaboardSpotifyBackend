package com.example.rest_api.patterns;

import java.util.ArrayList;

import com.example.rest_api.events.EventPayload;
import com.example.rest_api.events.ObservableEvents;

public interface Subject {

	public ArrayList<Observer> observers = new ArrayList<Observer>();

	default void attach(Observer o) {
		observers.add(o);
	};

	default void detach(Observer o) {
		observers.add(o);
	};

	/**
	 * Notify observers with an event payload.
	 * 
	 * @param payload
	 */
	default void notifyObservers(EventPayload<?> payload) {
		for (Observer o : observers) {
			o.update(payload);
		}
	};

	/**
	 * Notify observers without an event payload,
	 * does this by creating an empty payload containing only your event type.
	 * 
	 * @param event
	 */
	default void notifyObservers(ObservableEvents event) {
		EventPayload<?> payload = new EventPayload(event);
		for (Observer o : observers) {
			o.update(payload);
		}
	}
}

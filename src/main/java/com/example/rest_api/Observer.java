package com.example.rest_api;

public interface Observer<S extends Subject> {
	public void update(ObserverEvents event, S subject);
}

package com.example.rest_api.patterns;

import com.example.rest_api.events.EventPayload;
import com.example.rest_api.events.ObservableEvents;


public class SpotifyAPIObserver implements Observer{
	private SpotifyUserSingleton user;

	public SpotifyAPIObserver() {
		user = SpotifyUserSingleton.getInstance();
	}

	@Override
	public void update(EventPayload<?> event) {
		ObservableEvents type = event.getType();

		if (type == ObservableEvents.SPOTIFY_TOKEN_EXPIRED) {
			// refresh spotify token
		}
	}
}
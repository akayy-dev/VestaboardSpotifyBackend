package com.vesta.rest_api.events;

public enum ObservableEvents {
	/**
	 * When the currently playing song is changed.
	 */
	NEW_SONG(),
	/**
	 * When the next song in the queue has changed.
	 */
	QUEUE_UPDATE(),
	/**
	 * When the user logs out of the program.
	 */
	LOGOUT(),
	VESTABOARD_500(),
	SPOTIFY_TOKEN_EXPIRED()
}

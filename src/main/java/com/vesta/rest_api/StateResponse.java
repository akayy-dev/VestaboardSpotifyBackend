package com.vesta.rest_api;

public record StateResponse(Boolean isConnected, String connectedUser, Boolean isPlaying, Song nowPlaying, Song upNext) {
	public Song getNowPlaying() {
		return nowPlaying;
	}

	public Song getUpNext() {
		return upNext;
	}

}

package com.example.rest_api;

public record StateResponse(String connectedUser, Boolean isPlaying, Song nowPlaying, Song upNext) {
	public Song getNowPlaying() {
		return nowPlaying;
	}

	public Song getUpNext() {
		return upNext;
	}

}

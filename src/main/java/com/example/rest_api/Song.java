package com.example.rest_api;

public record Song(String title, String artist, String albumArt) { 
	public String getTitle() {
		return title;
	}
	public String getArtist() {
		return artist;
	}

	public String getAlbumArt() {
		return albumArt;
	}
}

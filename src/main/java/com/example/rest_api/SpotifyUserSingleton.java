package com.example.rest_api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.special.PlaybackQueue;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

public class SpotifyUserSingleton {
	private static SpotifyUserSingleton instance;
	private SpotifyApi spot;
	private boolean isAuthenticated;

	private SpotifyUserSingleton(
			String clientID,
			String clientSecret,
			String redirectURI) {
		try {
			spot = new SpotifyApi.Builder()
					.setClientId(clientID)
					.setClientSecret(clientSecret)
					.setRedirectUri(SpotifyHttpManager.makeUri(redirectURI))
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Make sure your keys are correct.");
		}

		isAuthenticated = false;
	}

	public static SpotifyUserSingleton getInstance(
			String clientID,
			String clientSecret,
			String redirectURI) {
		if (instance == null) {
			instance = new SpotifyUserSingleton(clientID, clientSecret, redirectURI);
		}
		return instance;
	}

	public static SpotifyUserSingleton getInstance() {
		if (instance != null) {
			return instance;
		} else {
			return null;
		}

	}

	/**
	 * Creates an authorization code URI request for Spotify's OAuth 2.0 flow.
	 * 
	 * The request is also configured to show a dialog to the user.
	 * 
	 * @return A {@link String} containing a URL to the Spotify Authentication page.
	 */
	public String getAuthURL() {
		final AuthorizationCodeUriRequest authorizationCodeUriRequest = spot
				.authorizationCodeUri()
				.scope("user-modify-playback-state user-read-playback-state user-read-currently-playing user-read-email user-read-private")
				.show_dialog(true)
				.build();
		final String authURI = authorizationCodeUriRequest.execute().toString();
		return authURI;
	}

	/**
	 * Retrieves the display name of the currently connected Spotify user.
	 *
	 * @return A string representing the display name of the connected user.
	 * @throws SpotifyWebApiException If an error occurs while communicating with
	 *                                the Spotify Web API.
	 * @throws IOException            If an I/O error occurs.
	 * @throws ParseException         If an error occurs while parsing the response.
	 */
	public String getConnectedUser() throws SpotifyWebApiException, IOException, ParseException {
		String username;
		User me;
		me = spot
				.getCurrentUsersProfile()
				.build()
				.execute();
		username = me.getDisplayName().toString();
		return username;
	}

	/**
	 * Uses the provided authorization code to obtain and set the access and refresh
	 * tokens.
	 *
	 * @param auth_code The authorization code received from the Spotify
	 *                  authorization process.
	 * @return true if the tokens were successfully obtained and set.
	 * @throws IOException            If an input or output exception occurs.
	 * @throws ParseException         If a parsing exception occurs.
	 * @throws SpotifyWebApiException If an error occurs while interacting with the
	 *                                Spotify Web API.
	 */
	public boolean useAuthToken(String auth_code) throws IOException, ParseException, SpotifyWebApiException {
		AuthorizationCodeCredentials creds = spot
				.authorizationCode(auth_code)
				.build()
				.execute();
		spot.setAccessToken(creds.getAccessToken());
		spot.setRefreshToken(creds.getRefreshToken());
		isAuthenticated = true;
		return isAuthenticated;
	}

	/**
	 * Searches for tracks based on the given query.
	 *
	 * @param query The search query string.
	 * @return An array of Track objects that match the search query.
	 * @throws IOException            If an input or output exception occurs.
	 * @throws ParseException         If a parsing exception occurs.
	 * @throws SpotifyWebApiException If a Spotify Web API exception occurs.
	 */
	public Track[] searchForTracks(String query) throws IOException, ParseException, SpotifyWebApiException {
		// Search for the song
		final Paging<Track> tracks = spot
				.searchTracks(query)
				.build()
				.execute();
		return tracks.getItems();
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	/**
	 * Retrieves the currently playing song from the Spotify API.
	 *
	 * @return A HashMap containing the song name and artist name if a song is
	 *         currently playing,
	 *         or null if no song is currently playing. The key "name" returns the
	 *         songname, the key "artist" returns the artist.
	 * @throws IOException            If there is an issue with network
	 *                                communication.
	 * @throws ParseException         If there is an issue parsing the response.
	 * @throws SpotifyWebApiException If there is an issue with the Spotify Web API.
	 */
	public HashMap<String, String> getCurrentSong() throws IOException, ParseException, SpotifyWebApiException {
		final GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spot.getUsersCurrentlyPlayingTrack()
				.build();
		final CurrentlyPlaying currentlyPlaying = currentlyPlayingRequest.execute();
		if (currentlyPlaying != null) { // Check if a song is actually playing at the moment.
			String songName = currentlyPlaying.getItem().getName();

			/*
			 * DONE: Developers of this library did not think to add a method to get the
			 * ARTIST of the song.
			 * Have to do this manually.
			 */
			String songID = currentlyPlaying.getItem().getId();
			Track trackObject = spot.getTrack(songID).build().execute();
			String trackArtist = trackObject.getArtists()[0].getName();

			HashMap<String, String> songData = new HashMap<String, String>();
			songData.put("name", songName);
			songData.put("artist", trackArtist);
			return songData;
		} else {
			System.err.println("No song is currently playing");
			return null;
		}
	}

	/**
	 * Retrieves the name of the next item in the user's Spotify playback queue.
	 *
	 * @return the name of the next item in the playback queue.
	 * @throws IOException if an I/O error occurs.
	 * @throws ParseException if a parsing error occurs.
	 * @throws SpotifyWebApiException if an error occurs with the Spotify Web API.
	 */
	public String getNextUp() throws IOException, ParseException, SpotifyWebApiException {
		final PlaybackQueue queue = spot
				.getTheUsersQueue()
				.build()
				.execute();
		final IPlaylistItem nextUp = queue.getQueue().get(0);
		return nextUp.getName();
	}

	/**
	 * Retrieves the current user's Spotify queue.
	 *
	 * @return An array of strings representing the names of the songs in the user's queue.
	 * @throws IOException If an input or output exception occurs.
	 * @throws ParseException If a parsing exception occurs.
	 * @throws SpotifyWebApiException If an error occurs while interacting with the Spotify Web API.
	 */
	public String[] getQueue() throws IOException, ParseException, SpotifyWebApiException {
		final List<IPlaylistItem> queue = spot
				.getTheUsersQueue()
				.build()
				.execute()
				.getQueue();

		List<String> queueList = new ArrayList<String>();
		for (IPlaylistItem song : queue) {
			queueList.add(song.getName());
		}
		return queueList.toArray(new String[0]);
	}

	/**
	 * Resets the authentication tokens and updates the authentication status.
	 * This method sets the access token and refresh token to null, and marks the user as not authenticated.
	 */
	public void resetAuth() {
		spot.setAccessToken(null);
		spot.setRefreshToken(null);
		this.isAuthenticated = false;
	}

	public static void main(String[] args) {
		// Simple test to make sure everything works.
		String clientID = System.getenv("CLIENT_ID");
		String clientSecret = System.getenv("CLIENT_SECRET");
		String redirectURL = System.getenv("REDIRECT_URL");
		Scanner scan = new Scanner(System.in);
		SpotifyUserSingleton singleton = SpotifyUserSingleton.getInstance(clientID, clientSecret, redirectURL);
		System.out.println(singleton.getAuthURL());

		System.out.print("Enter auth code:");
		String authCode = scan.nextLine();
		try {
			singleton.useAuthToken(authCode);
			System.out.println(singleton.getConnectedUser());
			HashMap<String, String> currentSong = singleton.getCurrentSong();
			System.out.println(currentSong.get("name") + " - " + currentSong.get("artist"));
			System.out.println("Next Up: " + singleton.getNextUp());
			System.out.println("Queue:");
			for (String song : singleton.getQueue()) {
				System.out.println(song);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}

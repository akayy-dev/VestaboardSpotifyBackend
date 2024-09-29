package com.example.rest_api;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.special.PlaybackQueue;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

import java.util.Scanner;

public class Spotify extends Vestaboard {
	private SpotifyApi spot;

	// Using this to manually ask for the authorization code, not optimal, should
	// fix this later.
	private Scanner scan = new Scanner(System.in);

	public Spotify(String clientID, String clientSecret, String redirectURI, String vestaboardKey) {
		super(vestaboardKey);
		spot = new SpotifyApi.Builder()
				.setClientId(clientID)
				.setClientSecret(clientSecret)
				.setRedirectUri(SpotifyHttpManager.makeUri(redirectURI))
				.build();

	}

	public String getAuthURL() {
		// Get authorization code.
		// This used to be ONE line in python.
		final AuthorizationCodeUriRequest authorizationCodeUriRequest = spot.authorizationCodeUri()
				.scope("user-modify-playback-state user-read-playback-state")
				.show_dialog(true)
				.build();
		final String authURI = authorizationCodeUriRequest.execute().toString();
		return authURI;
	}

	public boolean useAuthToken(String auth_code) {
		try {
			AuthorizationCodeCredentials creds = spot.authorizationCode(auth_code).build().execute();
			spot.setAccessToken(creds.getAccessToken());
			spot.setRefreshToken(creds.getRefreshToken());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private Track[] searchForTrack(String query) {
		try {
			// Search for the song
			final Paging<Track> tracks = spot.searchTracks(query)
					.build()
					.execute();
			return tracks.getItems();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String[] getCurrentSong() {
		final GetUsersCurrentlyPlayingTrackRequest currentlyPlayingRequest = spot.getUsersCurrentlyPlayingTrack()
				.build();
		try {
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

				return new String[] { songName, trackArtist };
			} else {
				System.err.println("No song is currently playing");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getNextUp() {
		try {
			final PlaybackQueue queue = spot.getTheUsersQueue().build().execute();
			final IPlaylistItem nextUp = queue.getQueue().get(0);
			return nextUp.getName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Song addToQueue(String songName, String songArtist) {
		// Search for the song
		Track[] tracks = searchForTrack(songName + " " + songArtist);

		try {
			// Add the selected track to the queue
			String selectedTrackURI = tracks[0].getUri();
			spot.addItemToUsersPlaybackQueue(selectedTrackURI).build().execute();

			String songNameInQueue = tracks[0].getName();
			String songArtistInQueue = tracks[0].getArtists()[0].getName();

			return new Song(songNameInQueue, songArtistInQueue);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean run() {
		Component nowPlaying = new Component();
		nowPlaying.setAlign("top");
		nowPlaying.setJustify("left");

		String[] currentSong = getCurrentSong();
		String nextUp = getNextUp();
		if (currentSong != null) {
			System.err.println("Updating current song " + currentSong);
			nowPlaying.setBody(
					"{66} Now Playing\n{64}" + currentSong[0] + "\n{64} " + currentSong[1] + "\n{65} Next Up\n{67} " + nextUp);
			String VBML = nowPlaying.getVBML();
			super.sendRaw(VBML);
			return true;
		} else {
			return false;
		}
	}

}

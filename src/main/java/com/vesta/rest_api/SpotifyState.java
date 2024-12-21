package com.vesta.rest_api;

import com.vesta.rest_api.events.EventPayload;
import com.vesta.rest_api.events.ObservableEvents;
import com.vesta.rest_api.patterns.SongChangeObserver;
import com.vesta.rest_api.patterns.Subject;
// Logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the state of the connected Spotify User
 * It's a subject, so it'll use the {@link SongChangeObserver}
 * to sync it's state with the board.
 */
public class SpotifyState implements Subject {

	private Song currentSong;
	private Song nextSong;

	private boolean isPlaying;
	private boolean isConnected;

	private static final Logger LOG = LogManager.getLogger(SpotifyState.class);

	public SpotifyState() {
		LOG.info("Attaching observers to SpotifyState object.");
		SongChangeObserver onSongChange = new SongChangeObserver();
		attach(onSongChange);
		LOG.info("Observers attached!");
	}

	/**
	 * Retrieves the next song in the playlist.
	 *
	 * @return the next {@link Song} in the playlist.
	 */
	public Song getNextSong() {
		return nextSong;
	}

	/**
	 * Sets the next song to be played.
	 *
	 * @param nextSong the Song object representing the next song
	 */
	public void setNextSong(Song nextSong) {
		if (this.nextSong != nextSong) {

			LOG.info("Updating next song in state to " + nextSong.getTitle());
			this.nextSong = nextSong;

			Song[] songs = { this.currentSong, this.nextSong };
			EventPayload<Song[]> payload = new EventPayload<>(ObservableEvents.NEW_SONG, songs);

			notifyObservers(payload);
		}
	}

	public Song getCurrentSong() {
		return currentSong;
	}

	public void setCurrentSong(Song currentSong) {
		if (this.currentSong != currentSong) {

			LOG.info("Updating current song in state to " + currentSong.getTitle());
			this.currentSong = currentSong;

			Song[] songs = { this.currentSong, this.nextSong };
			EventPayload<Song[]> payload = new EventPayload<>(ObservableEvents.NEW_SONG, songs);

			notifyObservers(payload);
		}
	};

	/**
	 * Checks if the Spotify player is currently playing.
	 *
	 * @return true if the player is playing, false otherwise.
	 */
	public boolean isPlaying() {
		return isPlaying;
	}

	/**
	 * Sets the playing state of the Spotify player.
	 *
	 * @param isPlaying a boolean indicating whether the player is currently
	 *                  playing.
	 */
	public void setisPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	/**
	 * Checks if the Spotify service is connected.
	 *
	 * @return true if the service is connected, false otherwise.
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Sets the connection state of the Spotify service.
	 *
	 * @param isConnected a boolean indicating whether the Spotify service is
	 *                    connected (true) or not (false)
	 */
	public void setisConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

}

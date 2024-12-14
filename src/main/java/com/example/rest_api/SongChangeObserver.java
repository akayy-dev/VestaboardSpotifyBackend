package com.example.rest_api;

import java.util.HashMap;

import com.example.rest_api.Subject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SongChangeObserver extends Vestaboard implements Observer<SpotifyIntegration> {
	private static final Logger LOG = LogManager.getLogger(SongChangeObserver.class.getName());

	public SongChangeObserver(String vestaboardKey) {
		super(vestaboardKey);
	}

	private void updateBoard(Song nowPlaying, Song nextUp) {
		Component boardContent = new Component();
		boardContent.setAlign("top");
		boardContent.setJustify("left");
		boardContent.setBody(
				"{66} Now Playing\n{64} " +
						nowPlaying.getTitle() +
						"\n{68} " +
						nowPlaying.getArtist() +
						"\n{65} Next Up\n{67} " +
						nextUp.getTitle());
		String VBML = boardContent.getVBML();
		HashMap<String, String> result = super.sendRaw(VBML);
		LOG.info("Response from Vestaboard API: " + result);

	}

	/**
	 * Send an update to the observer
	 * 
	 * @param event The type of event you are notifying the observer of.
	 * @param s     varargs for the current song and song that's next up.
	 */
	@Override
	public void update(ObserverEvents event, SpotifyIntegration subject) {
		// TODO Auto-generated method stub
		if (event == ObserverEvents.NEW_SONG) {
			LOG.info("New song is playing: " + subject.getSongState()[0].getTitle());
			Song nowPlaying = subject.getSongState()[0];
			Song upNext = subject.getSongState()[1];
			updateBoard(nowPlaying, upNext);
		}
		if (event == ObserverEvents.LOGOUT) {
			LOG.info("Logging out");
		}
	}

	public static void main(String[] args) {
		String clientID = System.getenv("CLIENT_ID");
		String clientSecret = System.getenv("CLIENT_SECRET");
		String redirectURL = System.getenv("REDIRECT_URL");
		String vestaboardKey = System.getenv("VESTABOARD_KEY");
		SongChangeObserver observer = new SongChangeObserver(vestaboardKey);

		Song nowPlaying = new Song("Beat It", "Micheal Jackson", "lol");
		Song upNext = new Song("JumpOutTheHouse", "Playboi Carti", "vamp+!");
	}
}

package patterns;

import java.util.HashMap;

import com.example.rest_api.Component;
import com.example.rest_api.Song;
import com.example.rest_api.SpotifyIntegration;
import com.example.rest_api.Vestaboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SongChangeObserver extends Vestaboard implements Observer<SpotifyIntegration> {
	private static final Logger LOG = LogManager.getLogger(SongChangeObserver.class.getName());

	public SongChangeObserver(String vestaboardKey) {
		super(vestaboardKey);
	}

	private void updateBoard(Song nowPlaying, Song nextUp) {
		Component nowPlayingComponent = new Component();
		nowPlayingComponent.setAlign("top");
		nowPlayingComponent.setJustify("left");
		nowPlayingComponent.setHeight(3);
		nowPlayingComponent.setBody(
				"{66} Now Playing\n{64} " +
						nowPlaying.getTrimmedTitle() +
						"\n{68} " +
						nowPlaying.getArtist());
		Component upNextComponent = new Component();
		upNextComponent.setAlign("top");
		upNextComponent.setJustify("left");
		upNextComponent.setHeight(3);
		upNextComponent.setBody(
				"\n{65} Next Up\n{67} " +
						nextUp.getTrimmedTitle());
		String VBML = Component.compileComponents(nowPlayingComponent, upNextComponent);
		HashMap<String, String> result = super.sendRaw(VBML);
		LOG.info("Response from Vestaboard API: " + result);

	}

	/**
	 * Send an update to the observer
	 * 
	 * @param event   The type of event you are notifying the observer of.
	 * @param subject The SpotifyIntegration object.
	 */
	@Override
	public void update(ObserverEvents event, SpotifyIntegration subject) {
		if (event == ObserverEvents.NEW_SONG) {
			Song nowPlaying = subject.getSongState()[0];
			Song upNext = subject.getSongState()[1];
			updateBoard(nowPlaying, upNext);
		}

		if (event == ObserverEvents.LOGOUT) {
			LOG.info("Logged out, clearing the board");
			sendMessage("");
		}
	}
}
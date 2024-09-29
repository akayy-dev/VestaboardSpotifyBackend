package com.example.rest_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VestaboardController {
	private Spotify spot;

	public VestaboardController() {
		String clientID = System.getenv("CLIENT_ID");
		String clientSecret = System.getenv("CLIENT_SECRET");
		String redirectURL = System.getenv("REDIRECT_URL");
		String vestaboardKey = System.getenv("VESTABOARD_KEY");
		spot = new Spotify(clientID, clientSecret, redirectURL, vestaboardKey);
	}

	@GetMapping("/get_auth_url")
	public String vestaboard() {
		return spot.getAuthURL();
	}

	@GetMapping("/send_auth_token")
	public boolean sendToken(@RequestParam(value = "code") String token) {
		// This assumes the spotify app is configured to send a request to this endpoint.
		// In the future, I will have spotify send the code to my frontend and post it here.

		// BUG: Despite returning true, when using accounts other than mine
		// I can't utilize the API, will work on this later.
		return spot.useAuthToken(token);
	}

	@GetMapping("/current")
	public Response getCurrent() {
		final String[] currentSong = spot.getCurrentSong();
		final String nextUp = spot.getNextUp();
		if (currentSong != null && nextUp != null) {
			Song currentSongObject = new Song(currentSong[0], currentSong[1]);
			Song nextUpObject = new Song(nextUp, "N/A");
			return new Response("success", new Song[]{currentSongObject, nextUpObject});
		} else {
			// Protect against cases where a song isn't playing.
			return new Response("failure", new Song("No song playing", "No artist"));
		}
	}


	@PostMapping("/request_song")
	public Record requestSong(@RequestParam(value = "title") String title,
			@RequestParam(value = "artist") String artist) {
		Song requested = spot.addToQueue(title, artist);
		if (requested != null) {
			return new Response("success", requested);
		} else {
			return new Response("failure", new Song("No song added", "No artist added"));
		}
	}

	// for testing purposes only, this should not make it into prod.
	@GetMapping("/update")
	public boolean update() {
		return spot.run();
	}
}

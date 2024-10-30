package com.example.rest_api;

import java.util.HashMap;

import org.springframework.scheduling.annotation.Scheduled;
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
        // This assumes the spotify app is configured to send a request to this
        // endpoint.
        // In the future, I will have spotify send the code to my frontend and post it
        // here.

        // BUG: Despite returning true, when using accounts other than mine
        // I can't utilize the API, will work on this later.
        return spot.useAuthToken(token);
    }

    @GetMapping("/current")
    public void getCurrent() {
        HashMap<String, String> currentSong = spot.getCurrentSong();
    }

    // @PostMapping("/request_song")
    // public Record requestSong(
    //         @RequestParam(value = "title") String title,
    //         @RequestParam(value = "artist") String artist) {
    //     try {
    //         Song requested = spot.addToQueue(title, artist);
    //         if (requested != null) {
    //             return new Response("success", requested);
    //         } else {
    //             return new Response(
    //                     "failure",
    //                     new Song("No song added", "No artist added"));
    //         }
    //     } catch (Exception e) {
    //         return new Response("failure", e.getMessage());
    //     }
    // }

    /**
     * Endpoint to get the authentication status.
     *
     * @return a Response object containing the status of the authentication.
     */
    @GetMapping("/auth_status")
    public Response getAuthStatus() {
        return new Response("success", spot.getAuthStatus());
    }

    @GetMapping("/connected_user")
    public String getConnectedUser() {
        return spot.getConnectedUser();
    }

    @Scheduled(fixedRate = 5000)
    public void update() {
        // This will run every 5 seconds to update the board.
        System.out.println("Checking for update...");
        try {
            Boolean isUpdated = spot.run();
            if (!isUpdated) {
                System.out.println("No update");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
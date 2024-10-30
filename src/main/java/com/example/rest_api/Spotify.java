package com.example.rest_api;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.ParseException;

import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.special.PlaybackQueue;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

public class Spotify extends Vestaboard {

    private SpotifyUserSingleton spot;
    private String lastSong; // Will be used in run() to track if song changed.

    public Spotify(
            String clientID,
            String clientSecret,
            String redirectURI,
            String vestaboardKey) {
        super(vestaboardKey);
        lastSong = "";
        spot = SpotifyUserSingleton.getInstance(clientID, clientSecret, redirectURI);
    }

    public String getAuthURL() {
        // Get authorization code.
        // This used to be ONE line in python.
        final String authURL = spot.getAuthURL();
        return authURL;
    }

    public boolean useAuthToken(String auth_code) {
        try {
            spot.useAuthToken(auth_code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spot.isAuthenticated();
    }

    // private Track[] searchForTrack(String query) throws NotAuthenticated {
    // if (isAuthenticated) {
    // try {
    // // Search for the song
    // final Paging<Track> tracks = spot
    // .searchTracks(query)
    // .build()
    // .execute();
    // return tracks.getItems();
    // } catch (Exception e) {
    // e.printStackTrace();
    // return null;
    // }
    // } else {
    // throw new NotAuthenticated("Not authenticated yet!");
    // }
    // }

    public String getConnectedUser() {
        String connectedUser;
        try {
            connectedUser = spot.getConnectedUser();
            return connectedUser;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public Boolean getAuthStatus() {
        // TODO: eventually this will return the auth status and the user connected.
        return spot.isAuthenticated();
    }

    private String trimFeatures(String title) {
        // use regex to remove features from the title.
        String pattern = "\\s*\\(.*?\\b(ft|featuring|with|feat)\\b.*?\\)";

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(title);

        return matcher.replaceAll("");
    }

    public HashMap<String, String> getCurrentSong() {
        try {
            HashMap<String, String> currentSong = spot.getCurrentSong();
            return currentSong;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public String getNextUp() {
        try {
            return spot.getNextUp();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    // public Song addToQueue(String songName, String songArtist) throws NotAuthenticated {
    //     if (isAuthenticated) {
    //         // Search for the song
    //         Track[] tracks = searchForTrack(songName + " " + songArtist);

    //         try {
    //             // Add the selected track to the queue
    //             String selectedTrackURI = tracks[0].getUri();
    //             spot
    //                     .addItemToUsersPlaybackQueue(selectedTrackURI)
    //                     .build()
    //                     .execute();

    //             String songNameInQueue = tracks[0].getName();
    //             String songArtistInQueue = tracks[0].getArtists()[0].getName();

    //             return new Song(songNameInQueue, songArtistInQueue);
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //             return null;
    //         }
    //     } else {
    //         throw new NotAuthenticated("Not authenticated yet!");
    //     }
    // }

    public boolean run() throws NotAuthenticated {
        Component nowPlaying = new Component();
        nowPlaying.setAlign("top");
        nowPlaying.setJustify("left");

        try {
            HashMap<String, String> currentSong = getCurrentSong();
            String trackName = currentSong.get("name");
            String trackArtist = currentSong.get("artist");
            String nextUp = getNextUp();
            if (spot.isAuthenticated() && currentSong != null && !trackName.equals(lastSong)) {
                System.err.println("Updating current song " + trackName + " from " + lastSong);
                nowPlaying.setBody(
                        "{66} Now Playing\n{64} " +
                                trackName +
                                "\n{68} " +
                                trackArtist +
                                "\n{65} Next Up\n{67} " +
                                nextUp);
                String VBML = nowPlaying.getVBML();
                String result = super.sendRaw(VBML);
                System.out.println(result);
                if (!result.equals(
                        "<!DOCTYPE html><html lang=\"en\"><head><title>Internal server error</title></head><body><main><h1>Internal server error</h1></main></body></html>")) {
                    // if there isn't a server error.
                    lastSong = trackName;
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

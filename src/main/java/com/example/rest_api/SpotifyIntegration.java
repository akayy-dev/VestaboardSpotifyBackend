package com.example.rest_api;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class SpotifyIntegration extends Vestaboard {

    // TODO: Create an attribute of type song, with the current song playing and the
    // song up next.

    /**
     * What song is currently playing stored in the cache.
     */
    private Song currentSongCached;
    /**
     * What song is up next stored in the cache.
     */
    private Song upNextCached;
    /**
     * The name of the user connected stored in the cache.
     */
    private String connectedUserCached;

    /**
     * Whether or not a user is connected stored in the cache.
     */
    private Boolean isConnectedCached;

    private SpotifyUserSingleton spot;
    private String lastSong; // Will be used in run() to track if song changed.

    private static final Logger LOG = LogManager.getLogger(SpotifyIntegration.class);

    public SpotifyIntegration(String clientID, String clientSecret, String redirectURI, String vestaboardKey) {
        super(vestaboardKey);
        LOG.debug("SpotifyIntegration created.");

        lastSong = ""; // Used to check if song has changed.

        spot = SpotifyUserSingleton.getInstance(clientID, clientSecret, redirectURI);
        LOG.debug("SpotifyUserSingleton has been created.");
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

    public void logout() {
        spot.resetAuth();
        LOG.info("Logged out, clearing the board.");
        sendMessage(" "); // Send an empty string to clear the board.

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

        String strippedTitle = matcher.replaceAll("");
        LOG.trace("Stripped " + title + " to: " + strippedTitle);

        return strippedTitle;
    }

    /**
     * Returns a cached list of the current song and whats next, use this to reduce
     * API calls to spotify.
     * 
     * @return A list of songs, the first being the current, the second being the
     */
    public Song[] getSongState() {
        try {
            Song[] songState = { currentSongCached, upNextCached };
            return songState;
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get state. ERROR_MSG: " + message);
        }
        return null;
    }

    /**
     * @return A boolean variable representing whether or not the user is connected.
     */
    public Boolean isConnectedState() {
        return isConnectedCached;
    }

    /**
     * Retrieves the currently playing song from the Spotify service.
     * 
     * @return the currently playing {@link Song} if available, or {@code null} if
     *         an error occurs or no song is playing.
     * @throws RuntimeException if there is an issue with the Spotify service or
     *                          user authentication.
     */
    private Song getCurrentSong() {
        try {
            Song currentSong = spot.getCurrentSong();
            return currentSong;
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get current song, is the user authenticated? ERROR MSG: " + message);
        }
        return null;
    }

    /**
     * Retrieves the next song in the queue from the Spotify integration.
     * 
     * @return the next song in the queue, or null if an error occurs.
     * @throws Throwable if there is an issue retrieving the next song.
     */
    private Song getNextUp() {
        try {
            return spot.getNextUp();
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get current song, is the user authenticated? ERROR MSG: " + message);
        }
        return null;
    }

    public Song[] getQueue() {
        try {
            return spot.getQueue();
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get current song, is the user authenticated? ERROR MSG: " + message);
        }
        return null;
    }

    /**
     * NOTE: Commented out because I haven't implemented this with thi
     * // singleton pattern, will get to later.
     */

    // public Song addToQueue(String songName, String songArtist) throws
    // NotAuthenticated {
    // if (isAuthenticated) {
    // // Search for the song
    // Track[] tracks = searchForTrack(songName + " " + songArtist);

    // try {
    // // Add the selected track to the queue
    // String selectedTrackURI = tracks[0].getUri();
    // spot
    // .addItemToUsersPlaybackQueue(selectedTrackURI)
    // .build()
    // .execute();

    // String songNameInQueue = tracks[0].getName();
    // String songArtistInQueue = tracks[0].getArtists()[0].getName();

    // return new Song(songNameInQueue, songArtistInQueue);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return null;
    // }
    // } else {
    // throw new NotAuthenticated("Not authenticated yet!");
    // }
    // }

    /**
     * Main function that runs the main program, designed to be run every n seconds,
     * if the song playing is different from the last time it ran, update the board.
     */
    public boolean run() {
        Component nowPlaying = new Component();
        nowPlaying.setAlign("top");
        nowPlaying.setJustify("left");

        try {
            // Won't run if spotify isn't authenticated, that way I won't get any errors.
            if (spot.isAuthenticated()) {
                Song currentSong = getCurrentSong();
                String trackName = trimFeatures(currentSong.getTitle());
                String trackArtist = currentSong.getArtist();
                Song nextUp = getNextUp();
                String nextUpTrimmed = trimFeatures(nextUp.getTitle());

                if (currentSong != null && !trackName.equals(lastSong)) {
                    LOG.info("Updating current song " + trackName + " from " + lastSong);

                    // update the cache.
                    currentSongCached = currentSong;
                    upNextCached = nextUp;
                    LOG.info("Updated cache, current:" + currentSongCached + " up next: " + upNextCached);

                    nowPlaying.setBody(
                            "{66} Now Playing\n{64} " +
                                    trackName +
                                    "\n{68} " +
                                    trackArtist +
                                    "\n{65} Next Up\n{67} " +
                                    nextUpTrimmed);
                    String VBML = nowPlaying.getVBML();
                    HashMap<String, String> result = super.sendRaw(VBML);
                    String status = result.get("status");
                    if (status.equals("ok")) { // Check if the board updated successfully.
                        // if there isn't a server error.
                        lastSong = trackName;
                        return true;
                    } else {
                        // if there is a server error.
                        LOG.error("An error occurred trying to update the song name. RESPONSE DATA: "
                                + result.toString());
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                // System.out.println("Not authenticated, not checking for updates.");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Update the cache.
     */
    public void updateCache() {
        try {
            isConnectedCached = spot.isAuthenticated();
            currentSongCached = spot.getCurrentSong();
            upNextCached = spot.getNextUp();
            connectedUserCached = spot.getConnectedUser();
        } catch (Exception e) {
            LOG.warn("Error updating cache, ERROR MSG: " + e.getMessage());
        }
    }
}

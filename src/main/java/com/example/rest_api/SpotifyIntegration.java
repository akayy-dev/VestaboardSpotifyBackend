package com.example.rest_api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpotifyIntegration extends Vestaboard implements Subject {

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
     * Whether or not a user is connected stored in the cache.
     */
    private Boolean isConnectedCached;

    /**
     * Cache boolean representing whether or not the user is playing anything
     */
    private Boolean isPlayingCached;

    private SpotifyUserSingleton spot;

    private static final Logger LOG = LogManager.getLogger(SpotifyIntegration.class);

    public SpotifyIntegration(String clientID, String clientSecret, String redirectURI, String vestaboardKey) {
        super(vestaboardKey);
        LOG.debug("SpotifyIntegration created.");

        isConnectedCached = false;

        SongChangeObserver observer = new SongChangeObserver(vestaboardKey);
        attach(observer);

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
        isConnectedCached = spot.isAuthenticated();
        return isConnectedCached;
    }

    public void logout() {
        LOG.info("Logged out, resetting spotify auth");
        spot.resetAuth();
        isConnectedCached = false;
        notifyObservers(ObserverEvents.LOGOUT);
    }

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
        return isConnectedCached;
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
    public Boolean isConnected() {
        return isConnectedCached;
    }

    public Boolean isPlaying() {
        return isPlayingCached;
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
    public void run() {
        Component nowPlaying = new Component();
        nowPlaying.setAlign("top");
        nowPlaying.setJustify("left");

        try {
            // Won't run if spotify isn't authenticated, that way I won't get any errors.
            if (isConnectedCached) {
                Song currentSong = getCurrentSong();
                Song upNext = getNextUp();

                /*
                 * BUG: Apparently this if statement doesn't run on the first update when a user
                 * connects,
                 * meaning that the board will only start working after the first song/queue
                 * change,
                 * figure this out later.
                 */
                if (currentSong != null && !currentSong.equals(currentSongCached)) {
                    LOG.info("Now playing changed from " + currentSongCached.getTitle() + " to "
                            + currentSong.getTitle());

                    /*
                     * update the cache to match the current song before
                     * notifying the observer
                     */
                    currentSongCached = currentSong;
                    upNextCached = upNext;

                    notifyObservers(ObserverEvents.NEW_SONG);
                }
                // also update if the queue is updated. will come useful when requests are
                // implemented.
                else if (upNext != null && !upNext.equals(upNextCached)) {

                    LOG.info("Up next changed from " + upNextCached.getTitle() + " to " + upNext.getTitle());

                    // see above
                    currentSongCached = currentSong;
                    upNextCached = upNext;

                    notifyObservers(ObserverEvents.NEW_SONG);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the cache.
     */
    public void updateCache() {
        if (isConnectedCached) {
            try {
                isConnectedCached = spot.isAuthenticated();
                currentSongCached = spot.getCurrentSong();
                upNextCached = spot.getNextUp();
                isPlayingCached = spot.isPlaying();
            } catch (Exception e) {
                LOG.warn("Error updating cache, ERROR MSG: " + e.getMessage());
            }
        }
    }
}

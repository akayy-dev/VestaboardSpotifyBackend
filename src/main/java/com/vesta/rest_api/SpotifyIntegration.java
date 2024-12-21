package com.vesta.rest_api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vesta.rest_api.events.EventPayload;
import com.vesta.rest_api.events.ObservableEvents;
import com.vesta.rest_api.patterns.SongChangeObserver;
import com.vesta.rest_api.patterns.SpotifyUserSingleton;
import com.vesta.rest_api.patterns.Subject;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

/**
 * Respoonsible for facillitating the connection of the
 * Spotify API to the Vestaboard API.
 * 
 * The class only directly interacts with the Spotify API,
 * it's a {@link Subject} to send an update event to the
 * {@link SongChangeObserver }.
 */
public class SpotifyIntegration implements Subject {

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

    /**
     * Cache string representing the connected user.
     */
    private String connectedUserCached;

    private SpotifyUserSingleton spot;

    private static final Logger LOG = LogManager.getLogger(SpotifyIntegration.class);

    public SpotifyIntegration(String clientID, String clientSecret, String redirectURI, String vestaboardKey) {
        LOG.debug("SpotifyIntegration created.");

        isConnectedCached = false;
        isPlayingCached = false;

        SongChangeObserver onSongChange = new SongChangeObserver(vestaboardKey);
        attach(onSongChange);

        spot = SpotifyUserSingleton.getInstance(clientID, clientSecret, redirectURI);
        LOG.debug("SpotifyUserSingleton has been created.");
    }

    /**
     * Get the Authorizaiton URL that gives
     * the "Allow Spotify to connect to" dialog
     * 
     * @return The authorization URL.
     */
    public String getAuthURL() {
        final String authURL = spot.getAuthURL();
        return authURL;
    }

    /**
     * When the user submits an auth token, this method is responsible
     * for "logging in" the user.
     * 
     * @param auth_code
     * @return
     */
    public boolean useAuthToken(String auth_code) {
        try {
            spot.useAuthToken(auth_code);
            updateCache();
            LOG.info("Auth token submitted, logged in as " + connectedUserCached);
        } catch (Exception e) {
            LOG.info("Error submitting auth token, ERROR_MSG: " + e.getMessage());
        }
        isConnectedCached = spot.isAuthenticated();
        return isConnectedCached;
    }

    /** Disconnects the users account from the application. */
    public void logout() {
        LOG.info("Logged out, resetting spotify auth");
        spot.resetAuth();
        isConnectedCached = false;

        notifyObservers(ObservableEvents.LOGOUT);
    }

    public String getConnectedUser() {
        String connectedUser;
        try {
            connectedUser = spot.getConnectedUser();
            return connectedUser;
        } catch (Exception e) {
            LOG.warn("Could not get connected user due to " + e.getClass().getSimpleName() + " ERROR MSG: "
                    + e.getMessage());
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

    /**
     * @return A boolean variable representing whether or not the user is currently
     *         playing a song.
     */
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
            isPlayingCached = true;
            return currentSong;
        } catch (TooManyRequestsException e) {
            Integer retryAfter = e.getRetryAfter();
            LOG.warn("getCurrentSong() raised TooManyRequests exceptions, backing off for " + retryAfter + " seconds.");

            // backoff
            try {
                // NOTE: Not a fan of this backoff implementation,
                // try to see if this can be moved to a RateLimitObserver class.
                Thread.sleep(retryAfter * 1000);
                LOG.info("Backoff finished, retrying.");
                getCurrentSong();
            } catch (InterruptedException i) {
                LOG.warn("Backoff attempt interrupted, ERROR_MSG: " + i.getMessage());
            }

        } catch (SpotifyWebApiException s) {
            String message = s.getLocalizedMessage();
            LOG.warn("Could not get current song due to SpotifyWebApiException ERROR MSG: " + message);

            if (message.equals("The access token expired")) {
                LOG.warn("Expired access token, should create a method to refresh access token.");
                notifyObservers(ObservableEvents.SPOTIFY_TOKEN_EXPIRED);
            }

        } catch (IndexOutOfBoundsException e) {
            // typically is thrown when the user isn't playing anything at all
            LOG.info("Not playing anything.");
            isPlayingCached = false;
        } catch (Exception e) {
            String errorName = e.getClass().getSimpleName();
            String message = e.getLocalizedMessage();
            LOG.warn("Could not get current song due to " + errorName + "ERROR MSG: " + message);
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
        } catch (NullPointerException n) {
            // typically thrown when nothing is playing
            LOG.info("Could not get next up due to NullPointerException, likely user isn't playing anything.");
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get next song, is the user authenticated? ERROR MSG: " + message);
        }
        return null;
    }

    /**
     * Get's the songs in the players QUEUE:
     * NOTE: This is untested.
     */
    public Song[] getQueue() {
        try {
            return spot.getQueue();
        } catch (Throwable t) {
            String message = t.getLocalizedMessage();
            LOG.warn("Could not get queue, is the user authenticated? ERROR MSG: " + message);
        }
        return null;
    }

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
                isPlayingCached = spot.isPlaying();

                // if cached songs are empty, that likely means user just logged in.
                if (currentSongCached == null && upNextCached == null) {
                    LOG.trace("Cached songs are empty, updating currentSongCached and upNextCached");
                    currentSongCached = currentSong;
                    upNextCached = upNext;
                    Song[] songs = new Song[] { currentSongCached, upNextCached };
                    EventPayload<Song[]> payload = new EventPayload<Song[]>(ObservableEvents.NEW_SONG, songs);
                    notifyObservers(payload);
                }

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
                    Song[] songs = new Song[] { currentSongCached, upNextCached };
                    EventPayload<Song[]> payload = new EventPayload<Song[]>(ObservableEvents.NEW_SONG, songs);
                    notifyObservers(payload);
                }
                // also update if the queue is updated. will come useful when requests are
                // implemented.
                else if (upNext != null && !upNext.equals(upNextCached)) {

                    LOG.info("Up next changed from " + upNextCached.getTitle() + " to " + upNext.getTitle());

                    // see above
                    currentSongCached = currentSong;
                    upNextCached = upNext;

                    // Construct payload for observer
                    Song[] songs = new Song[] { currentSong, currentSongCached };
                    EventPayload<Song[]> eventPayload = new EventPayload<Song[]>(ObservableEvents.NEW_SONG, songs);

                    notifyObservers(eventPayload);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the cache.
     * Call this sparingly, as this can result in rate limits.
     */
    public void updateCache() {
        try {
            isPlayingCached = spot.isPlaying();

            if (isPlayingCached) {
                isConnectedCached = spot.isAuthenticated();
                currentSongCached = spot.getCurrentSong();
                connectedUserCached = spot.getConnectedUser();
                upNextCached = spot.getNextUp();
            }
        } catch (Exception e) {
            LOG.warn("Error updating cache, ERROR MSG: " + e.getMessage());
        }
    }
}

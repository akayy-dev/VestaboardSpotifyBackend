package com.example.rest_api;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.special.PlaybackQueue;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

public class Spotify extends Vestaboard {

    private SpotifyApi spot;
    private String lastSong; // Will be used in run() to track if song changed.
    private boolean isAuthenticated = false; // Used to check if user is authenticated.

    public Spotify(
            String clientID,
            String clientSecret,
            String redirectURI,
            String vestaboardKey) {
        super(vestaboardKey);
        lastSong = "";
        try {
            spot = new SpotifyApi.Builder()
                    .setClientId(clientID)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(SpotifyHttpManager.makeUri(redirectURI))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Make sure your keys are correct.");
        }
    }

    public String getAuthURL() {
        // Get authorization code.
        // This used to be ONE line in python.
        final AuthorizationCodeUriRequest authorizationCodeUriRequest = spot
                .authorizationCodeUri()
                .scope("user-modify-playback-state user-read-playback-state")
                .show_dialog(true)
                .build();
        final String authURI = authorizationCodeUriRequest.execute().toString();
        return authURI;
    }

    public boolean useAuthToken(String auth_code) {
        try {
            AuthorizationCodeCredentials creds = spot
                    .authorizationCode(auth_code)
                    .build()
                    .execute();
            spot.setAccessToken(creds.getAccessToken());
            spot.setRefreshToken(creds.getRefreshToken());
            isAuthenticated = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Track[] searchForTrack(String query) throws NotAuthenticated {
        if (isAuthenticated) {
            try {
                // Search for the song
                final Paging<Track> tracks = spot
                        .searchTracks(query)
                        .build()
                        .execute();
                return tracks.getItems();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new NotAuthenticated("Not authenticated yet!");
        }
    }

    public String[] getCurrentSong() throws NotAuthenticated {
        if (isAuthenticated) {
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
        } else {
            throw new NotAuthenticated("Not authenticated yet!");
        }
    }

    public String getNextUp() throws NotAuthenticated {
        if (isAuthenticated) {
            try {
                final PlaybackQueue queue = spot
                        .getTheUsersQueue()
                        .build()
                        .execute();
                final IPlaylistItem nextUp = queue.getQueue().get(0);
                return nextUp.getName();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new NotAuthenticated("Not authenticated yet!");
        }
    }

    public Song addToQueue(String songName, String songArtist) throws NotAuthenticated {
        if (isAuthenticated) {
            // Search for the song
            Track[] tracks = searchForTrack(songName + " " + songArtist);

            try {
                // Add the selected track to the queue
                String selectedTrackURI = tracks[0].getUri();
                spot
                        .addItemToUsersPlaybackQueue(selectedTrackURI)
                        .build()
                        .execute();

                String songNameInQueue = tracks[0].getName();
                String songArtistInQueue = tracks[0].getArtists()[0].getName();

                return new Song(songNameInQueue, songArtistInQueue);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new NotAuthenticated("Not authenticated yet!");
        }
    }

    public boolean run() throws NotAuthenticated {
        if (isAuthenticated) {
            Component nowPlaying = new Component();
            nowPlaying.setAlign("top");
            nowPlaying.setJustify("left");

            try {
                String[] currentSong = getCurrentSong();
                String nextUp = getNextUp();
                if (currentSong != null && !currentSong[0].equals(lastSong)) {
                    System.err.println("Updating current song " + currentSong[0] + " from " + lastSong);
                    nowPlaying.setBody(
                            "{66} Now Playing\n{64} " +
                                    currentSong[0] +
                                    "\n{64} " +
                                    currentSong[1] +
                                    "\n{65} Next Up\n{67} " +
                                    nextUp);
                    String VBML = nowPlaying.getVBML();
                    System.out.println(super.sendRaw(VBML));
                    lastSong = currentSong[0];
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            throw new NotAuthenticated("Not authenticated yet!");
        }
    }
}

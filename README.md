# Overview
The VestaboardSpotify API is an open-source backend service that facilitates the integration of Spotify with a Vestaboard, allowing users to display real-time song information and manage their music queue remotely. This API serves as an intermediary between users and Spotify's API, leveraging authentication and data retrieval to enhance the user experience.

# Features
- Authentication with Spotify: Users can authenticate their Spotify accounts to access the API's functionalities.  
- Current Song Information: Retrieve the currently playing song and the next up in the queue.  
- Add Songs to Queue: Users can request to add songs to their Spotify queue.  
- A#thentication Status: Check the current authentication status of the user.  
- Connected User: Get the Spotify user currently authenticated with the API.  
- Real-Time Updates: The API updates the Vestaboard every 5 seconds to reflect current music activity.  
## Endpoints
The API provides several endpoints to manage the interaction with Spotify:

1. Get Authentication URL  
Endpoint: `/get_auth_url`  
Method: GET  
Description: Returns the authentication URL for Spotify login.  

2. Send Authentication Token  
Endpoint: `/send_auth_token`  
Method: GET  
Parameters: code - The authentication code received from Spotify.  
Description: Accepts the Spotify authentication code and updates the session.  

3. Get Current Playing Song  
Endpoint: `/current`  
Method: GET  
Description: Retrieves the currently playing song and the next up in the playlist.

5. Request a Song  
Endpoint: `/request_song`  
Method: POST  
Parameters:
title - The title of the song to be added.
artist - The artist of the song.
Description: Adds a specified song to the user's Spotify queue.  
6. Get Authentication Status  
Endpoint: `/auth_status`  
Method: GET  
Description: Provides the current authentication status of the user.  

6. Get Connected User  
Endpoint: `/connected_user`  
Method: GET  
Description: Returns information about the currently connected Spotify user.  
# Setup
## Environment Variables
Ensure you set the following environment variables before running the API:  
`CLIENT_ID`: Your Spotify application's client ID.  
`CLIENT_SECRET`: Your Spotify application's client secret.  
`REDIRECT_URL`: The URL to redirect users post-authentication.  
`VESTABOARD_KEY`: Key for controlling the Vestaboard.  
Build and Run: Compile the Java files and run the application on a suitable server or localhost environment.  

Dependencies: This project uses Spring and the [Spotify Web API Java library](https://github.com/spotify-web-api-java/spotify-web-api-java). Ensure to include it in your build configuration.

package com.ekkongames.jdacbl.audio;

import com.ekkongames.jdacbl.bot.BotInfo;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildVoiceController {

    private final AudioPlayerManager manager;

    /**
     * Track scheduler for the player.
     */
    private final MusicScheduler scheduler;
    private final String youtubeToken;
    private final AudioManager audioManager;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildVoiceController(String youtubeToken, AudioPlayerManager manager, AudioManager audioManager) {
        this.youtubeToken = youtubeToken;
        this.audioManager = audioManager;
        AudioPlayer player = manager.createPlayer();
        scheduler = new MusicScheduler(player);
        player.addListener(scheduler);

        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));

        this.manager = manager;
    }

    public void connectToChannel(VoiceChannel channel) {
        audioManager.openAudioConnection(channel);
    }

    public void disconnectFromChannel() {
        audioManager.closeAudioConnection();
    }

    public void playTrack(String trackName) {
        playTrack(trackName, false);
    }

    private String findOnYoutube(String trackName) {
        HttpClient client = HttpClients.createDefault();
        HttpGet get;
        try {
            URIBuilder builder = new URIBuilder("https://www.googleapis.com/youtube/v3/search");
            builder.addParameter("key", youtubeToken);
            builder.addParameter("maxResults", "1");
            builder.addParameter("q", trackName);
            get = new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return trackName;
        }

        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                    // the response should come in as a single line
                    JSONObject obj = new JSONObject(in.lines().collect(Collectors.joining("\n")));
                    JSONArray results = obj.getJSONArray("items");
                    if (results.length() > 0) {
                        return (String) ((JSONObject) ((JSONObject) results.get(0)).get("id")).get("videoId");
                    } else {
                        System.err.println("Failed to get a response from YouTube.");
                    }
                }
            } else {
                System.err.println("Failed to get a response from YouTube.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackName;
    }

    private void playTrack(String trackName, boolean searchYoutube) {
        String loadSource = trackName;
        if (searchYoutube) {
            loadSource = findOnYoutube(trackName);
        }
        manager.loadItem(loadSource, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    scheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {
                if (!searchYoutube && youtubeToken != null) {
                    playTrack(trackName, true);
                    return;
                }
                System.err.println("Failed to locate track!");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (!searchYoutube && youtubeToken != null) {
                    playTrack(trackName, true);
                    return;
                }
                System.err.println("Failed to load track!");
                exception.printStackTrace();
            }
        });
    }

    public void skipTrack() {
        scheduler.nextTrack();
    }

    public void setVolume(int volume) {
        scheduler.setVolume(volume);
    }

    /**
     * @param value True to pause, false to resume
     */
    public void setPaused(boolean value) {
        scheduler.setPaused(value);
    }

    /**
     * Stop currently playing track.
     */
    public void stopTrack() {
        scheduler.stopTrack();
    }
}

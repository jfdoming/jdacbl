package com.ekkongames.jdacbl.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildVoiceController {

    private final AudioPlayerManager manager;

    /**
     * Track scheduler for the player.
     */
    private final MusicScheduler scheduler;
    private final AudioManager audioManager;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildVoiceController(AudioPlayerManager manager, AudioManager audioManager) {
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
        manager.loadItem(trackName, new AudioLoadResultHandler() {
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
                System.err.println("Failed to locate track!");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.err.println("Failed to load track!");
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
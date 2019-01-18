package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.audio.GuildVoiceController;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;

import java.util.prefs.Preferences;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class GuildState {

    private static final Preferences preferences = Preferences.userNodeForPackage(GuildState.class);

    private final Guild guild;
    private final String guildId;
    private final GuildVoiceController voiceController;

    public GuildState(AudioPlayerManager audioPlayerManager, Guild guild) {
        this.guild = guild;
        this.guildId = guild.getId();

        this.voiceController = new GuildVoiceController(audioPlayerManager, guild.getAudioManager());
    }

    public Guild getGuild() {
        return guild;
    }

    public String getId() {
        return guildId;
    }

    public void putPersist(String key, String value) {
        preferences.put(guildId + "-" + key, value);
    }

    public String getPersist(String key, String defaultValue) {
        return preferences.get(guildId + "-" + key, defaultValue);
    }

    public GuildVoiceController getVoiceController() {
        return voiceController;
    }
}

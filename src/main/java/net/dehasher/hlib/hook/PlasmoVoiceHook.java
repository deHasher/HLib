package net.dehasher.hlib.hook;

import org.bukkit.entity.Player;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;

public class PlasmoVoiceHook {
	public static final String SPEAK_PERMISSION = "pv.activation.*";

	public static boolean hasVoiceChat(PlasmoVoiceServer voiceServer, Player player) {
		if (voiceServer == null || player == null || !player.isOnline()) return false;
		return voiceServer.getPlayerManager().getPlayerById(player.getUniqueId()).map(VoicePlayer::hasVoiceChat).orElse(false);
	}
}
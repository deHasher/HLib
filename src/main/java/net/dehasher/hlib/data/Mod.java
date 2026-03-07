package net.dehasher.hlib.data;

import lombok.Getter;

public enum Mod {
    PLASMO_VOICE("PlasmoVoice"),
    SIMPLE_VOICE_CHAT("SimpleVoiceChat"),
    H_CINEMA("HCinema"),
    EMOTECRAFT("Emotecraft");

    @Getter
    private final String value;

    Mod(String value) {
        this.value = value;
    }

    public String getName() {
        return value;
    }
}
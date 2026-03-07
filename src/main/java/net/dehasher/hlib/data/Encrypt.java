package net.dehasher.hlib.data;

// Защита от InJarTraslator.
public enum Encrypt {
    DOMAIN_DEHASHER(new byte[] {100, 101, 104, 97, 115, 104, 101, 114, 46, 110, 101, 116}), // dehasher.net
    TXT_DEV_MODE(new byte[] {72, 67, 111, 114, 101, 68, 101, 118, 77, 111, 100, 101}), // HCoreDevMode
    DEHASHER(new byte[] {100, 101, 72, 97, 115, 104, 101, 114}), // deHasher
    FLUGEGEHEIMEN(new byte[] {70, 108, 117, 103, 101, 103, 101, 104, 101, 105, 109, 101, 110}), // Flugegeheimen
    MUASO(new byte[] {77, 85, 65, 83, 79}), // MUASO
    IP_DNS(new byte[] {56, 46, 56, 46, 56, 46, 56}), // 8.8.8.8
    URL_UPDATER(new byte[] {104, 116, 116, 112, 115, 58, 47, 47, 97, 112, 105, 46, 100, 101, 104, 97, 115, 104, 101, 114, 46, 110, 101, 116, 47, 112, 108, 117, 103, 105, 110, 115}), // https://api.dehasher.net/plugins
    URL_STRICT(new byte[] {104, 116, 116, 112, 115, 58, 47, 47, 97, 112, 105, 46, 100, 101, 104, 97, 115, 104, 101, 114, 46, 110, 101, 116, 47, 115, 116, 114, 105, 99, 116}); // https://api.dehasher.net/strict

    public final String value;

    Encrypt(byte[] bytes) {
        this.value = new String(bytes);
    }
}
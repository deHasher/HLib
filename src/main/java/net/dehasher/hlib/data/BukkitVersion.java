package net.dehasher.hlib.data;

import lombok.Getter;
import java.util.stream.Stream;

// https://ru.minecraft.wiki/w/Версия_протокола
@Getter
public enum BukkitVersion {
    V1_8(47),
    V1_9(107, 108, 109, 110),
    V1_10(210),
    V1_11(315, 316),
    V1_12(335, 338, 340),
    V1_13(393, 401, 404),
    V1_14(477, 480, 485, 490, 498),
    V1_15(573, 575, 578),
    V1_16(735, 736, 751, 753, 754),
    V1_17(755, 756),
    V1_18(757, 758),
    V1_19(759, 760, 761, 762),
    V1_20(763, 764, 765, 766),
    V1_21(767, 768, 769, 770, 771, 772, 773, 774);

    private final String value;
    private final Integer[] protocol;

    BukkitVersion(Integer... protocol) {
        this.value = this.name()
                .replace("V", "")
                .replace("_", ".");
        this.protocol = protocol;
    }

    public static BukkitVersion parseProtocol(int protocol) {
        return Stream.of(values())
                .filter(v -> protocol >= Stream.of(v.getProtocol())
                        .min(Integer::compare)
                        .orElse(Integer.MAX_VALUE)
                        && protocol <= Stream.of(v.getProtocol())
                        .max(Integer::compare)
                        .orElse(Integer.MIN_VALUE))
                .findFirst()
                .orElse(null);
    }

    public enum RevisionVersion {
        R1, R2, R3, R4, R5, R6, R7, R8, R9, R10
    }
}
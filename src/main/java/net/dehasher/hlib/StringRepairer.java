package net.dehasher.hlib;

public class StringRepairer {
    public static String removeInvertChars(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length();) {
            int codePoint = input.codePointAt(i);
            if (!isInvertChar(codePoint)) result.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }

        return result.toString();
    }

    private static boolean isInvertChar(int codePoint) {
        byte directionality = Character.getDirectionality(codePoint);
        return isExplicitBidiControl(codePoint, directionality) || isInInvertScriptRange(codePoint);
    }

    private static boolean isExplicitBidiControl(int codePoint, byte directionality) {
        if (codePoint == 0x200E || codePoint == 0x200F) return true;
        return switch (directionality) {
            case Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
                 Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE,
                 Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
                 Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE,
                 Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT,
                 Character.DIRECTIONALITY_LEFT_TO_RIGHT_ISOLATE,
                 Character.DIRECTIONALITY_RIGHT_TO_LEFT_ISOLATE,
                 Character.DIRECTIONALITY_FIRST_STRONG_ISOLATE,
                 Character.DIRECTIONALITY_POP_DIRECTIONAL_ISOLATE -> true;
            default -> false;
        };
    }

    private static boolean isInInvertScriptRange(int codePoint) {
        return
                inRange(codePoint, 0x0590, 0x05FF) ||		// Hebrew — иврит
                inRange(codePoint, 0x0600, 0x06FF) ||		// Arabic — арабский
                inRange(codePoint, 0x0700, 0x074F) ||		// Syriac — сирийское письмо
                inRange(codePoint, 0x0750, 0x077F) ||		// Arabic Supplement — дополнительные арабские символы
                inRange(codePoint, 0x0780, 0x07BF) ||		// Thaana — тана, используется для дивехи
                inRange(codePoint, 0x07C0, 0x07FF) ||		// NKo — письмо нко
                inRange(codePoint, 0x0800, 0x083F) ||		// Samaritan — самаритянское письмо
                inRange(codePoint, 0x0840, 0x085F) ||		// Mandaic — мандейское письмо
                inRange(codePoint, 0x0860, 0x086F) ||		// Syriac Supplement — дополнение к сирийскому письму
                inRange(codePoint, 0x0870, 0x089F) ||		// Arabic Extended-B — расширение арабского письма
                inRange(codePoint, 0x08A0, 0x08FF) ||		// Arabic Extended-A — расширение арабского письма

                inRange(codePoint, 0xFB1D, 0xFB4F) ||		// Hebrew Presentation Forms — presentation forms для иврита
                inRange(codePoint, 0xFB50, 0xFDFF) ||		// Arabic Presentation Forms-A — presentation forms для арабского
                inRange(codePoint, 0xFE70, 0xFEFF) ||		// Arabic Presentation Forms-B — presentation forms для арабского

                inRange(codePoint, 0x10840, 0x1085F) ||		// Imperial Aramaic — имперское арамейское письмо
                inRange(codePoint, 0x10860, 0x1087F) ||		// Palmyrene — пальмирское письмо
                inRange(codePoint, 0x10880, 0x108AF) ||		// Nabataean — набатейское письмо
                inRange(codePoint, 0x108E0, 0x108FF) ||		// Hatran — хатранское письмо
                inRange(codePoint, 0x10900, 0x1091F) ||		// Phoenician — финикийское письмо
                inRange(codePoint, 0x10920, 0x1093F) ||		// Lydian — лидийское письмо
                inRange(codePoint, 0x109A0, 0x109FF) ||		// Meroitic Cursive — мероитив курсив

                inRange(codePoint, 0x10A00, 0x10A5F) ||		// Kharoshthi — кхароштхи
                inRange(codePoint, 0x10A60, 0x10A7F) ||		// Old South Arabian — древнеюжноаравийское письмо
                inRange(codePoint, 0x10A80, 0x10A9F) ||		// Old North Arabian — древнесевероаравийское письмо
                inRange(codePoint, 0x10AC0, 0x10AFF) ||		// Manichaean — манихейское письмо
                inRange(codePoint, 0x10B00, 0x10B3F) ||		// Avestan — авестийское письмо
                inRange(codePoint, 0x10B40, 0x10B5F) ||		// Inscriptional Parthian — парфянское эпиграфическое письмо
                inRange(codePoint, 0x10B60, 0x10B7F) ||		// Inscriptional Pahlavi — пехлевийское эпиграфическое письмо
                inRange(codePoint, 0x10B80, 0x10BAF) ||		// Psalter Pahlavi — псалтырное пехлеви
                inRange(codePoint, 0x10C00, 0x10C4F) ||		// Old Turkic — древнетюркское письмо
                inRange(codePoint, 0x10C80, 0x10CFF) ||		// Old Hungarian — старовенгерское письмо

                inRange(codePoint, 0x10D00, 0x10D3F) ||		// Hanifi Rohingya — письмо рохинджа
                inRange(codePoint, 0x10D40, 0x10D8F) ||		// Garay — письмо гарай
                inRange(codePoint, 0x10E80, 0x10EBF) ||		// Yezidi — езидское письмо
                inRange(codePoint, 0x10EC0, 0x10EFF) ||		// Arabic Extended-C — расширение арабского письма
                inRange(codePoint, 0x10F00, 0x10F2F) ||		// Old Sogdian — древнесогдийское письмо
                inRange(codePoint, 0x10F30, 0x10F6F) ||		// Sogdian — согдийское письмо
                inRange(codePoint, 0x10F70, 0x10FAF) ||		// Old Uyghur — староуйгурское письмо
                inRange(codePoint, 0x10FB0, 0x10FDF) ||		// Chorasmian — хорезмийское письмо
                inRange(codePoint, 0x10FE0, 0x10FFF) ||		// Elymaic — элимейское письмо

                inRange(codePoint, 0x1E800, 0x1E8DF) ||		// Mende Kikakui — письмо менде кикакуи
                inRange(codePoint, 0x1E900, 0x1E95F) ||		// Adlam — адлам, используется для языка фула

                inRange(codePoint, 0x10800, 0x1083F) ||		// Cypriot — кипрское слоговое письмо
                inRange(codePoint, 0x10940, 0x1095F) ||		// Sidetic — сидетское письмо
                inRange(codePoint, 0x10980, 0x1099F) ||		// Meroitic Hieroglyphs — мероитские иероглифы

                inRange(codePoint, 0x1EC70, 0x1ECBF) ||		// Indic Siyaq Numbers — индийские siyaq-числа
                inRange(codePoint, 0x1ED00, 0x1ED4F) ||		// Ottoman Siyaq Numbers — османские siyaq-числа
                inRange(codePoint, 0x1EE00, 0x1EEFF);		// Arabic Mathematical Alphabetic Symbols — арабские математические алфавитные символы
    }

    private static boolean inRange(int codePoint, int fromInclusive, int toInclusive) {
        return codePoint >= fromInclusive && codePoint <= toInclusive;
    }
}
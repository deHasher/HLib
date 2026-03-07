package net.dehasher.hlib;

import lombok.Getter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Rusificator {
    @Getter
    private static final Map<Character, Character> map = new ConcurrentHashMap<>();

    public static String replace(String input) {
        if (getMap().isEmpty()) {
            getMap().put('q', 'й'); getMap().put('w', 'ц');
            getMap().put('e', 'у'); getMap().put('r', 'к');
            getMap().put('t', 'е'); getMap().put('y', 'н');
            getMap().put('u', 'г'); getMap().put('i', 'ш');
            getMap().put('o', 'щ'); getMap().put('p', 'з');
            getMap().put('a', 'ф'); getMap().put('s', 'ы');
            getMap().put('d', 'в'); getMap().put('f', 'а');
            getMap().put('g', 'п'); getMap().put('h', 'р');
            getMap().put('j', 'о'); getMap().put('k', 'л');
            getMap().put('l', 'д'); getMap().put('z', 'я');
            getMap().put('x', 'ч'); getMap().put('c', 'с');
            getMap().put('v', 'м'); getMap().put('b', 'и');
            getMap().put('n', 'т'); getMap().put('m', 'ь');

            getMap().put('Q', 'Й'); getMap().put('W', 'Ц');
            getMap().put('E', 'У'); getMap().put('R', 'К');
            getMap().put('T', 'Е'); getMap().put('Y', 'Н');
            getMap().put('U', 'Г'); getMap().put('I', 'Ш');
            getMap().put('O', 'Щ'); getMap().put('P', 'З');
            getMap().put('A', 'Ф'); getMap().put('S', 'Ы');
            getMap().put('D', 'В'); getMap().put('F', 'А');
            getMap().put('G', 'П'); getMap().put('H', 'Р');
            getMap().put('J', 'О'); getMap().put('K', 'Л');
            getMap().put('L', 'Д'); getMap().put('Z', 'Я');
            getMap().put('X', 'Ч'); getMap().put('C', 'С');
            getMap().put('V', 'М'); getMap().put('B', 'И');
            getMap().put('N', 'Т'); getMap().put('M', 'Ь');
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character character = getMap().get(input.charAt(i));
            builder.append(character != null ? character : input.charAt(i));
        }
        return builder.toString();
    }
}
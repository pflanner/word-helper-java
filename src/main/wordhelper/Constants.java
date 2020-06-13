package wordhelper;

import org.apache.commons.collections.set.UnmodifiableSet;

import java.util.Set;
import java.util.stream.Collectors;

public class Constants {
    public static final String ALPHABET_STRING = "abcdefghijklmnopqrstuvwxyz";
    public static final Set<Character> ALPHABET_SET = UnmodifiableSet.decorate(ALPHABET_STRING.chars().mapToObj((i) -> Character.valueOf((char)i)).collect(Collectors.toSet()));
}

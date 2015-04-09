package petrglad.msgsort;

public class Util {
    public static <T extends Comparable<? super T>> T max(T a, T b) {
        return a.compareTo(b) < 0 ? b : a;
    }
}

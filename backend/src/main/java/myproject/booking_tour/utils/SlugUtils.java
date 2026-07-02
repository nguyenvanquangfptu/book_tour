package myproject.booking_tour.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) return "";
        
        // Remove special Vietnamese characters specifically if needed (optional but recommended)
        String noTone = input.replaceAll("đ", "d").replaceAll("Đ", "D");
        
        String nowhitespace = WHITESPACE.matcher(noTone.toLowerCase()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        
        // Remove accents
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        // Replace multiple hyphens with a single hyphen
        slug = slug.replaceAll("-+", "-");
        // Remove trailing or leading hyphens
        slug = slug.replaceAll("^-|-$", "");
        
        return slug;
    }
}

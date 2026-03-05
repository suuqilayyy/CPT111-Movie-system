package movietracker.backend;

/**
 * SpeculativeMovie represents imaginative genres (e.g., sci-fi, fantasy, most action).
 */
public class SpeculativeMovie extends Movie {
    public SpeculativeMovie(String id, String title, String genre, int year, double rating) {
        super(id, title, genre, year, rating);
    }

    @Override
    public String getType() {
        return "SpeculativeMovie";
    }
}

package movietracker.backend;

/**
 * RealisticMovie represents reality-oriented genres (e.g., drama, crime, war).
 */
public class RealisticMovie extends Movie {
    public RealisticMovie(String id, String title, String genre, int year, double rating) {
        super(id, title, genre, year, rating);
    }

    @Override
    public String getType() {
        return "RealisticMovie";
    }
}

package movietracker.backend;

//Movie class.Subclasses: SpeculativeMovie, RealisticMovie, etc.
public class Movie {
    private final String id;
    private final String title;
    private final String genre;
    private final int year;
    private final double rating;

    public Movie(String id, String title, String genre, int year, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public double getRating() {
        return rating;
    }

    /**
     * Gets the type of movie (e.g., GeneralMovie, SpeculativeMovie, RealisticMovie).
     * Subclasses override this to return their specific type.
     * @return The movie type as a string
     */
    public String getType() {
        return "GeneralMovie";
    }

    /**
     * Returns a string representation of this movie.
     * @return A formatted string containing movie details
     */
    @Override
    public String toString() {
        return "Movie{id='" + id + "', title='" + title + "', genre='" + genre 
                + "', year=" + year + ", rating=" + rating + "}";
    }
}

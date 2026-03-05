package movietracker.backend;

import java.time.LocalDate;

/**
 * Represents a single entry in a user's watch history.
 * Each entry contains a movie ID and the date when the movie was watched.
 */
public class WatchHistoryEntry {
    private final String movieId;
    private final LocalDate date;

    /**
     * Creates a new watch history entry.
     * @param movieId The ID of the watched movie
     * @param date The date when the movie was watched
     */
    public WatchHistoryEntry(String movieId, LocalDate date) {
        this.movieId = movieId;
        this.date = date;
    }

    /**
     * Gets the movie ID for this entry.
     * @return The movie ID
     */
    public String getMovieId() {
        return movieId;
    }

    /**
     * Gets the date when the movie was watched.
     * @return The watch date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Compares this entry to another entry by date in descending order.
     * @param other The other entry to compare to
     * @return Negative if this entry is more recent, positive if older, zero if same date
     */
    public int compareToEntry(WatchHistoryEntry other) {
        return other.date.compareTo(this.date); // descending by date
    }

    public String toCsvToken() {
        return movieId + "@" + date;
    }

    public static WatchHistoryEntry fromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String[] parts = token.split("@", 2);
        if (parts.length != 2) {
            return null;
        }
        return new WatchHistoryEntry(parts[0], LocalDate.parse(parts[1]));
    }
}

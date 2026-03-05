package movietracker.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * MovieManager handles loading and managing the movie database.
 * It reads movie data from CSV files and provides methods for:
 * - Loading movies from file
 * - Retrieving movies by ID, genre, or title search
 * - Sorting movies by rating
 *
 * CSV Format: id,title,genre,year,rating[,type]
 * Type is optional and defaults to SpeculativeMovie/RealisticMovie based on genre.
 */
public class MovieManager {
    private static final int DEFAULT_INT_VALUE = 0;
    private final ArrayList<Movie> movies = new ArrayList<>();

    /**
     * Loads movies from a CSV file into the movie database.
     * Clears any existing movies before loading.
     * 
     * CSV parsing handles:
     * - Quoted fields with commas inside titles
     * - Header row detection (skipped if present)
     * - Empty lines (skipped)
     * - Missing fields (handled gracefully)
     * 
     * @param csv The CSV file to load movies from
     * @throws IOException If file cannot be read
     */
    public void loadMovies(File csv) throws IOException {
        movies.clear();
        if (csv == null || !csv.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (CsvParser.isEmptyLine(line)) {
                    continue;
                }
                // Skip header
                if (first) {
                    first = false;
                    if (CsvParser.isHeaderLine(line, "id,")) {
                        continue;
                    }
                }
                // Use manual CSV parsing to handle commas in titles
                String[] parts = CsvParser.parseCsvLine(line);
                if (parts.length < 5) {
                    continue;
                }
                Movie movie = createMovieFromParts(parts);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        }
    }
    
    /**
     * Creates a Movie instance from parsed CSV parts.
     * @param parts The parsed CSV parts
     * @return A new Movie instance, or null if invalid data
     */
    private Movie createMovieFromParts(String[] parts) {
        String id = parts[0].trim();
        String title = parts[1].trim();
        String genre = parts[2].trim();
        int year = parseInt(parts[3].trim());
        double rating = parseDouble(parts[4].trim());
        String type = parts.length > 5 ? parts[5].trim() : inferType(genre);
        return createMovieByType(id, title, genre, year, rating, type);
    }

    /**
     * Returns a copy of all movies in the database.
     * @return A new ArrayList containing all loaded movies
     */
    public ArrayList<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    /**
     * Finds a movie by its unique ID.
     * @param id The movie ID to search for
     * @return The matching Movie, or null if not found
     */
    public Movie getMovieById(String id) {
        for (Movie m : movies) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Finds all movies matching a specific genre.
     * Comparison is case-insensitive.
     * @param genre The genre to search for
     * @return ArrayList of movies matching the genre
     */
    public ArrayList<Movie> getMoviesByGenre(String genre) {
        ArrayList<Movie> res = new ArrayList<>();
        for (Movie m : movies) {
            if (m.getGenre().equalsIgnoreCase(genre)) {
                res.add(m);
            }
        }
        return res;
    }

    /**
     * Searches for movies by title keyword.
     * Performs case-insensitive substring matching.
     * @param keyword The search keyword
     * @return ArrayList of movies whose titles contain the keyword
     */
    public ArrayList<Movie> searchByTitle(String keyword) {
        ArrayList<Movie> res = new ArrayList<>();
        if (keyword == null) {
            return res;
        }
        String kw = keyword.toLowerCase(Locale.ROOT);
        for (Movie m : movies) {
            if (m.getTitle().toLowerCase(Locale.ROOT).contains(kw)) {
                res.add(m);
            }
        }
        return res;
    }

    /**
     * Returns all movies sorted by rating in descending order.
     * Uses bubble sort algorithm (course-compliant, avoids Collections.sort).
     * @return A new ArrayList of movies sorted by rating (highest first)
     */
    public ArrayList<Movie> sortByRatingDesc() {
        ArrayList<Movie> res = new ArrayList<>(movies);
        for (int i = 0; i < res.size(); i++) {
            for (int j = i + 1; j < res.size(); j++) {
                if (res.get(j).getRating() > res.get(i).getRating()) {
                    Movie tmp = res.get(i);
                    res.set(i, res.get(j));
                    res.set(j, tmp);
                }
            }
        }
        return res;
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return DEFAULT_INT_VALUE;
        }
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0d;
        }
    }

    private Movie createMovieByType(String id, String title, String genre, int year, double rating, String type) {
        if (type == null || type.isEmpty()) {
            type = inferType(genre);
        }

        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("spec")) {
            return new SpeculativeMovie(id, title, genre, year, rating);
        }
        if (normalized.startsWith("real")) {
            return new RealisticMovie(id, title, genre, year, rating);
        }
        return new Movie(id, title, genre, year, rating);
    }

    private String inferType(String genre) {
        if (genre == null) {
            return "SpeculativeMovie";
        }
        String g = genre.trim().toLowerCase(Locale.ROOT);
        if (g.contains("drama") || g.contains("crime") || g.contains("mystery") || g.contains("war")) {
            return "RealisticMovie";
        }
        if (g.contains("sci-fi") || g.contains("science fiction") || g.contains("fantasy")
                || g.contains("horror") || g.contains("thriller") || g.contains("action")
                || g.contains("animation")) {
            return "SpeculativeMovie";
        }
        // Default to speculative when unknown.
        return "SpeculativeMovie";
    }
    
}

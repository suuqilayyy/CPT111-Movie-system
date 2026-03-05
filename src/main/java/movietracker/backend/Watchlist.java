package movietracker.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Watchlist class to manage a user's list of movies to watch.Stores movie IDs as strings.

public class Watchlist {
    private ArrayList<String> movieIds;

    public Watchlist() {
        this.movieIds = new ArrayList<>();
    }

    public Watchlist(ArrayList<String> movieIds) {
        this.movieIds = movieIds != null ? new ArrayList<>(movieIds) : new ArrayList<>();
    }

    public void add(String movieId) {
        if (movieId != null && !movieIds.contains(movieId)) {
            movieIds.add(movieId);
        }
    }

    public void remove(String movieId) {
        movieIds.remove(movieId);
    }

    public boolean contains(String movieId) {
        return movieIds.contains(movieId);
    }

    public void clear() {
        movieIds.clear();
    }

    public int size() {
        return movieIds.size();
    }

    public boolean isEmpty() {
        return movieIds.isEmpty();
    }

    public List<String> getMovieIds() {
        return new ArrayList<>(movieIds);
    }

    public String toCsvString() {
        if (movieIds == null || movieIds.isEmpty()) {
            return "";
        }
        String result = "";
        for (int i = 0; i < movieIds.size(); i++) {
            if (i > 0) {
                result += ';';
            }
            result += movieIds.get(i).replace(",", " ").replace(";", " ");
        }
        return result;
    }

    public static Watchlist fromCsvString(String csvString) {
        Watchlist watchlist = new Watchlist();
        if (csvString == null || csvString.isEmpty()) {
            return watchlist;
        }
        String[] items = csvString.split(";");
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                watchlist.add(trimmed);
            }
        }
        return watchlist;
    }

    public Map<String, Integer> getGenreCounts(MovieManager movieManager) {
        Map<String, Integer> counts = new HashMap<>();
        for (String movieId : movieIds) {
            Movie m = movieManager.getMovieById(movieId);
            if (m != null) {
                String g = m.getGenre();
                if (counts.containsKey(g)) {
                    counts.put(g, counts.get(g) + 1);
                } else {
                    counts.put(g, 1);
                }
            }
        }
        return counts;
    }
}


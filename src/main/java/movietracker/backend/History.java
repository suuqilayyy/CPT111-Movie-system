package movietracker.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//History class to manage a user's watch history.Stores WatchHistoryEntry objects.
public class History {
    private ArrayList<WatchHistoryEntry> entries;

    public History() {
        this.entries = new ArrayList<>();
    }

    public History(ArrayList<WatchHistoryEntry> entries) {
        if (entries == null) {
            this.entries = new ArrayList<>();
        } else {
            this.entries = new ArrayList<>(entries);
        }
        sortEntries();
    }

    public void add(WatchHistoryEntry entry) {
        if (entry != null) {
            entries.add(entry);
            sortEntries();
        }
    }

    public void add(String movieId, LocalDate date) {
        if (movieId != null && date != null) {
            add(new WatchHistoryEntry(movieId, date));
        }
    }

    public void remove(String movieId) {
        if (movieId == null) {
            return;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            WatchHistoryEntry entry = entries.get(i);
            if (entry != null && movieId.equals(entry.getMovieId())) {
                entries.remove(i);
            }
        }
    }

    public boolean contains(String movieId) {
        for (WatchHistoryEntry entry : entries) {
            if (entry != null && movieId.equals(entry.getMovieId())) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<WatchHistoryEntry> getEntries() {
        // Return a new ArrayList copy
        return new ArrayList<>(entries);
    }

    private void sortEntries() {
        for (int i = 0; i < entries.size() - 1; i++) {
            for (int j = 0; j < entries.size() - 1 - i; j++) {
                WatchHistoryEntry e1 = entries.get(j);
                WatchHistoryEntry e2 = entries.get(j + 1);
                // Compare dates using custom comparison method
                if (e1.compareToEntry(e2) > 0) {
                    // Swap positions
                    entries.set(j, e2);
                    entries.set(j + 1, e1);
                }
            }
        }
    }

    public String toCsvString() {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        String result = "";
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                result += ';';
            }
            result += entries.get(i).toCsvToken();
        }
        return result;
    }

    public static History fromCsvString(String csvString) {
        History history = new History();
        if (csvString == null || csvString.isEmpty()) {
            return history;
        }
        String[] items = csvString.split(";");
        for (String item : items) {
            WatchHistoryEntry entry = WatchHistoryEntry.fromToken(item.trim());
            if (entry != null) {
                history.add(entry);
            }
        }
        return history;
    }

    public Map<String, Integer> getGenreCounts(MovieManager movieManager) {
        Map<String, Integer> counts = new HashMap<>();
        for (WatchHistoryEntry entry : entries) {
            Movie m = movieManager.getMovieById(entry.getMovieId());
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


    public int getTotalYearSum(MovieManager movieManager) {
        int sum = 0;
        for (WatchHistoryEntry entry : entries) {
            Movie m = movieManager.getMovieById(entry.getMovieId());
            if (m != null) {
                sum += m.getYear();
            }
        }
        return sum;
    }
    
}


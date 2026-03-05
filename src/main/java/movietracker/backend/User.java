package movietracker.backend;

//Base User class.Subclasses: BasicUser, PremiumUser.

import java.time.LocalDate;

public class User {
    private static final int RECOMMENDATION_LIMIT = 6;
    private String username;
    private String password;
    private Watchlist watchlist;
    private History history;

    protected User(String username, String password, Watchlist watchlist, History history) {
        this.username = username;
        this.password = password;
        this.watchlist = watchlist != null ? watchlist : new Watchlist();
        this.history = history != null ? history : new History();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getUserType() {
        return "BasicUser"; // Default to Basic user type
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public History getHistory() {
        return history;
    }

    public void addToWatchlist(String movieId) {
        if (watchlist != null && movieId != null && !movieId.isEmpty()) {
            watchlist.add(movieId);
        }
    }

    public void removeFromWatchlist(String movieId) {
        if (watchlist != null && movieId != null) {
            watchlist.remove(movieId);
        }
    }

    public void clearWatchlist() {
        if (watchlist != null) {
            watchlist.clear();
        }
    }
    
    /**
     * Marks a movie as watched, automatically removing it from watchlist if present
     * and adding it to history with the current date.
     * @param movieId The ID of the movie to mark as watched
     * @param date The date when the movie was watched
     */
    public void markAsWatched(String movieId, LocalDate date) {
        if (movieId == null || date == null || movieId.isEmpty()) {
            return;
        }
        
        // Remove from watchlist if present
        removeFromWatchlist(movieId);
        
        // Add to history
        addHistory(movieId, date);
    }
    
    /**
     * Checks if a movie is in the user's watchlist.
     * @param movieId The ID of the movie to check
     * @return True if the movie is in the watchlist, false otherwise
     */
    public boolean hasInWatchlist(String movieId) {
        return watchlist != null && movieId != null && watchlist.contains(movieId);
    }
    
    /**
     * Checks if a movie is in the user's history.
     * @param movieId The ID of the movie to check
     * @return True if the movie is in history, false otherwise
     */
    public boolean hasInHistory(String movieId) {
        return history != null && movieId != null && history.contains(movieId);
    }
    
    /**
     * Gets the number of movies in the user's watchlist.
     * @return The size of the watchlist
     */
    public int getWatchlistSize() {
        return watchlist != null ? watchlist.size() : 0;
    }
    
    /**
     * Gets the number of movies in the user's history.
     * @return The size of the history
     */
    public int getHistorySize() {
        return history != null ? history.size() : 0;
    }

    public int getRecommendationLimit() {
        return RECOMMENDATION_LIMIT;
    }

    public boolean requiresPasswordHashing() {
        return false;
    }

    public void addHistory(WatchHistoryEntry entry) {
        if (history != null) {
            history.add(entry);
        }
    }

    public void addHistory(String movieId, LocalDate date) {
        if (history != null) {
            history.add(movieId, date);
        }
    }

    public void clearHistory() {
        if (history != null) {
            history.clear();
        }
    }

    /**
     * Converts this user to a CSV line for file storage.
     * Format: username,password,userType,watchlist,history
     * @return A CSV-formatted string representing this user
     */
    public String toCsvLine() {
        return "\"" + escapeCsv(username) + "\","
                + "\"" + escapeCsv(password) + "\","
                + getUserType() + "," 
                + (watchlist != null ? watchlist.toCsvString() : "") + "," 
                + (history != null ? history.toCsvString() : "");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }

    /**
     * Returns a string representation of this user.
     * Note: Password is not included for security reasons.
     * @return A formatted string containing user details
     */
    @Override
    public String toString() {
        return "User{username='" + username + "', type='" + getUserType() 
                + "', watchlistSize=" + getWatchlistSize() 
                + ", historySize=" + getHistorySize() + "}";
    }
}

package movietracker.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * UserManager manages the lifecycle of users in the system.
 * It handles registration, login, data persistence, and user session management.
 * Users, along with their watchlists and histories, are stored in a CSV file.
 */
public class UserManager {
    private static final String USER_CSV_PATH = "CW3_Data_Files/data/users.csv";
    private static final String HASH_PREFIX = "CUST$";

    private final ArrayList<User> users = new ArrayList<>();
    private User currentUser;

    public UserManager() {
        try {
            loadUsers();
        } catch (IOException ex) {
            System.out.println("Failed to load users: " + ex.getMessage());
        }
    }

    public synchronized void loadUsers() throws IOException {
        users.clear();
        File file = prepareUserFile();
        if (file.length() == 0) {
            return;
        }

        boolean changed = processUserFile(file);
        if (changed) {
            persistSilently();
        }
    }
    
    /**
     * Prepares the user file, creating it if necessary.
     * @return The prepared file
     * @throws IOException If an I/O error occurs
     */
    private File prepareUserFile() throws IOException {
        File file = new File(USER_CSV_PATH);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
        }
        return file;
    }
    
    /**
     * Processes the user file, reading and creating users.
     * @param file The user file to process
     * @return True if any users were modified, false otherwise
     * @throws IOException If an I/O error occurs
     */
    private boolean processUserFile(File file) throws IOException {
        boolean changed = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (CsvParser.isEmptyLine(line)) {
                    continue;
                }
                // Skip header row if present
                if (first) {
                    first = false;
                    if (CsvParser.isHeaderLine(line, "username,")) {
                        continue;
                    }
                }
                User user = createUserFromLine(line);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return changed;
    }
    
    /**
     * Creates a User instance from a CSV line.
     * @param line The CSV line to process
     * @return A new User instance, or null if invalid data
     */
    private User createUserFromLine(String line) {
        // Use manual CSV parsing to handle commas in passwords
        String[] parts = CsvParser.parseCsvLine(line);
        if (parts.length < 2) {
            return null;
        }
        String username = parts[0].trim();
        if (username.isEmpty()) {
            return null;
        }
        String password = parts[1];
        String userType = parts.length > 2 ? parts[2].trim() : "BasicUser";
        Watchlist watchlist = Watchlist.fromCsvString(parts.length > 3 ? parts[3] : "");
        History history = History.fromCsvString(parts.length > 4 ? parts[4] : "");
        
        // Hash password if needed
        if (!password.isEmpty() && isPremium(userType) && !isHashed(password)) {
            password = hashPassword(password);
        }
        
        return createUserByType(username, password, watchlist, history, userType);
    }

    /**
     * Saves all users to the CSV file.
     * The file format is: username,password,type,watchlist,history
     * @throws IOException If an I/O error occurs during writing
     */
    public synchronized void saveUsers() throws IOException {
        File file = new File(USER_CSV_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("username,password,type,watchlist,history");
            for (User user : users) {
                writer.println(user.toCsvLine());
            }
        }
    }

    /**
     * Authenticates a user with the given credentials.
     * @param username The username to check
     * @param password The password to check
     * @return True if login succeeds, false otherwise
     */
    public synchronized boolean login(String username, String password) {
        if (isBlankOrHasSpace(username) || isBlankOrHasSpace(password)) {
            return false;
        }
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (verifyPassword(user, password)) {
                    currentUser = user;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Creates a new user account if the username is unique and inputs are valid.
     * @param username The new username
     * @param password The new password
     * @param userType The type of user ("BasicUser" or "PremiumUser")
     * @return True if the account was created successfully, false otherwise
     */
    public synchronized boolean createAccount(String username, String password, String userType) {
        if (isBlankOrHasSpace(username) || isBlankOrHasSpace(password)) {
            return false;
        }
        if (validateUsernameExists(username)) {
            return false;
        }
        String storedPwd = isPremium(userType) ? hashPassword(password) : password;
        User newUser = createUserByType(username, storedPwd, new Watchlist(), new History(), userType);
        if (newUser == null) {
            return false;
        }
        users.add(newUser);
        currentUser = newUser;
        try {
            saveUsers();
        } catch (IOException ex) {
            System.out.println("Failed to save users: " + ex.getMessage());
        }
        return true;
    }

    /**
     * Checks if a username already exists in the system.
     * @param username The username to check
     * @return True if the username exists, false otherwise
     */
    public synchronized boolean validateUsernameExists(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlankOrHasSpace(String text) {
        return text == null || text.trim().isEmpty() || text.matches(".*\\s+.*");
    }

    /**
     * Retrieves the currently logged-in user.
     * @return The current User object, or null if no user is logged in
     */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Watchlist operations
    // Watchlist operations
    
    /**
     * Adds a movie to the current user's watchlist.
     * Persists changes immediately.
     * @param movieId The ID of the movie to add
     */
    public synchronized void addToWatchlist(String movieId) {
        if (currentUser == null || movieId == null) {
            return;
        }
        if (!currentUser.getWatchlist().contains(movieId)) {
            currentUser.addToWatchlist(movieId);
            persistSilently();
        }
    }

    /**
     * Removes a movie from the current user's watchlist.
     * Persists changes immediately.
     * @param movieId The ID of the movie to remove
     */
    public synchronized void removeFromWatchlist(String movieId) {
        if (currentUser == null || movieId == null) {
            return;
        }
        currentUser.removeFromWatchlist(movieId);
        persistSilently();
    }

    /**
     * Retrieves the current user's watchlist.
     * @return A list of movie IDs in the watchlist
     */
    public synchronized ArrayList<String> getWatchlist() {
        if (currentUser == null || currentUser.getWatchlist() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentUser.getWatchlist().getMovieIds());
    }

    /**
     * Clears the entire watchlist for the current user.
     * Persists changes immediately.
     */
    public synchronized void clearWatchlist() {
        if (currentUser == null) {
            return;
        }
        currentUser.clearWatchlist();
        persistSilently();
    }

    // History operations
    // History operations

    /**
     * Adds a history entry for the current user.
     * Persists changes immediately.
     * @param movieId The ID of the movie watched
     * @param date The date the movie was watched
     */
    public synchronized void addHistory(String movieId, LocalDate date) {
        if (currentUser == null || movieId == null || date == null) {
            return;
        }
        currentUser.addHistory(new WatchHistoryEntry(movieId, date));
        persistSilently();
    }
    
    /**
     * Marks a movie as watched, automatically removing it from watchlist if present
     * and adding it to history with the current date.
     * @param movieId The ID of the movie to mark as watched
     */
    public synchronized void markAsWatched(String movieId) {
        if (currentUser == null || movieId == null) {
            return;
        }
        currentUser.markAsWatched(movieId, LocalDate.now());
        persistSilently();
    }

    /**
     * Retrieves the current user's watch history.
     * @return A list of WatchHistoryEntry objects
     */
    public synchronized ArrayList<WatchHistoryEntry> getHistory() {
        if (currentUser == null || currentUser.getHistory() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(currentUser.getHistory().getEntries());
    }

    /**
     * Clears the entire watch history for the current user.
     * Persists changes immediately.
     */
    public synchronized void clearHistory() {
        if (currentUser == null) {
            return;
        }
        currentUser.clearHistory();
        persistSilently();
    }

    /**
     * Verifies if the provided password matches the current user's password.
     * @param rawPassword The password to verify
     * @return True if correct, false otherwise
     */
    public synchronized boolean verifyCurrentPassword(String rawPassword) {
        return verifyPassword(currentUser, rawPassword);
    }

    /**
     * Changes the current user's password.
     * Hashes the password if the user is a Premium user.
     * @param newPassword The new password to set
     */
    public synchronized void changePassword(String newPassword) {
        if (currentUser == null || newPassword == null) {
            return;
        }
        String stored = currentUser.requiresPasswordHashing() ? hashPassword(newPassword) : newPassword;
        currentUser.setPassword(stored);
        persistSilently();
    }

    private boolean verifyPassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }
        String stored = user.getPassword();
        if (stored == null) {
            return false;
        }
        if (isHashed(stored)) {
            return stored.equals(hashPassword(rawPassword));
        }
        boolean matches = stored.equals(rawPassword);
        if (matches && user.requiresPasswordHashing() && !isHashed(stored)) {
            user.setPassword(hashPassword(rawPassword));
            persistSilently();
        }
        return matches;
    }

    /**
     * Checks if a password string is already hashed.
     * @param password The password string
     * @return True if it starts with the known hash prefix or SHA256 marker
     */
    public boolean isHashed(String password) {
        return isCustomHashed(password) || (password != null && password.startsWith("SHA256$"));
    }

    private boolean isCustomHashed(String password) {
        return password != null && password.startsWith(HASH_PREFIX);
    }

    private boolean isPremium(String userType) {
        return "PremiumUser".equalsIgnoreCase(userType);
    }

    /**
     * Hashes a password using a custom algorithm.
     * @param rawPassword The plain text password
     * @return The hashed password string starting with CUST$
     */
    private String hashPassword(String rawPassword) {
        String input = rawPassword == null ? "" : rawPassword;
        int seed = 131;
        long hash = 7;
        for (int i = 0; i < input.length(); i++) {
            hash = hash * seed + (input.charAt(i) + i * 17);
        }
        long value = Math.abs(hash);
        String result = HASH_PREFIX;
        if (value == 0) {
            result += "0";
        } else {
            while (value > 0) {
                long nib = value & 0xF;
                result += Long.toHexString(nib);
                value >>= 4;
            }
        }
        return result;
    }

    private void persistSilently() {
        try {
            saveUsers();
        } catch (IOException ex) {
            System.out.println("Failed to save users: " + ex.getMessage());
        }
    }

    /**
     * Create a User instance based on userType string.
     * Uses inheritance: BasicUser or PremiumUser.
     */
    private User createUserByType(String username, String password, Watchlist watchlist, History history, String userType) {
        if (isPremium(userType)) {
            return new PremiumUser(username, password, watchlist, history);
        } else {
            return new BasicUser(username, password, watchlist, history);
        }
    }
}

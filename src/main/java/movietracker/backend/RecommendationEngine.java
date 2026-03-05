package movietracker.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

/**
 * RecommendationEngine provides movie recommendations based on user preferences.
 * Supports multiple recommendation strategies:
 * - Genre-Based: Recommends movies matching user's most-watched genres
 * - Rating-Based: Recommends highest-rated movies user hasn't seen
 * - Year-Based: Recommends movies from similar time periods as user preferences
 * - Premium Mix (Hybrid): Combines genre, year, and rating factors with weighted scoring
 * 
 * Algorithm Overview:
 * 1. Build exclusion set from user's watchlist and history
 * 2. Score candidate movies based on selected strategy
 * 3. Sort candidates using bubble sort (course-compliant)
 * 4. Return top N recommendations
 */
public class RecommendationEngine {
    private final MovieManager movieManager;
    private final UserManager userManager;

    /**
     * Creates a new RecommendationEngine.
     * @param movieManager The movie manager for accessing movie data
     * @param userManager The user manager for accessing user preferences
     */
    public RecommendationEngine(MovieManager movieManager, UserManager userManager) {
        this.movieManager = movieManager;
        this.userManager = userManager;
    }

    /**
     * Recommends movies based on user's top genres (unlimited).
     * @return List of recommended movies sorted by genre match and rating
     */
    public ArrayList<Movie> recommendByTopGenre() {
        return recommendByTopGenre(Integer.MAX_VALUE);
    }

    /**
     * Recommends movies based on user's top genres.
     * Algorithm:
     * 1. Analyze user's history and watchlist to find top 2 genres
     * 2. Score genres: history count * 2 + watchlist count
     * 3. Filter movies matching top genres
     * 4. Sort by genre score (descending), then by rating
     * 
     * @param limit Maximum number of recommendations to return
     * @return List of recommended movies sorted by relevance
     */
    public ArrayList<Movie> recommendByTopGenre(int limit) {
        User user = userManager.getCurrentUser();
        if (user == null) return new ArrayList<>();

        Map<String, Integer> historyCount = user.getHistory().getGenreCounts(movieManager);
        if (historyCount.isEmpty()) return new ArrayList<>();

        Map<String, Integer> watchlistCount = user.getWatchlist().getGenreCounts(movieManager);
        Map<String, Integer> subHistoryCount = buildSubCountsFromHistory(user);
        Map<String, Integer> subWatchlistCount = buildSubCountsFromWatchlist(user);
        List<String> topGenres = pickTopGenres(historyCount);

        Set<String> exclude = buildExclusion();
        Set<String> seen = new HashSet<>();
        ArrayList<Movie> candidates = new ArrayList<>();

        for (Movie m : movieManager.getAllMovies()) {
            if (topGenres.contains(m.getGenre()) && !exclude.contains(m.getId()) && seen.add(m.getId())) {
                candidates.add(m);
            }
        }

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                int scoreA = genreScore(candidates.get(i).getGenre(), historyCount, watchlistCount);
                int scoreB = genreScore(candidates.get(j).getGenre(), historyCount, watchlistCount);
                if (scoreB > scoreA) {
                    Movie tmp = candidates.get(i);
                    candidates.set(i, candidates.get(j));
                    candidates.set(j, tmp);
                } else if (scoreB == scoreA) {
                    int subScoreA = subCategoryScore(candidates.get(i), subHistoryCount, subWatchlistCount, scoreA);
                    int subScoreB = subCategoryScore(candidates.get(j), subHistoryCount, subWatchlistCount, scoreB);
                    boolean swap = false;
                    if (subScoreB > subScoreA) {
                        swap = true;
                    } else if (subScoreB == subScoreA && candidates.get(j).getRating() > candidates.get(i).getRating()) {
                        swap = true;
                    }
                    if (swap) {
                        Movie tmp = candidates.get(i);
                        candidates.set(i, candidates.get(j));
                        candidates.set(j, tmp);
                    }
                }
            }
        }

        if (candidates.size() > limit) {
            ArrayList<Movie> trimmed = new ArrayList<>();
            for(int i=0; i<limit; i++) trimmed.add(candidates.get(i));
            candidates = trimmed;
        }

        fillWithRemainingMovies(candidates, limit, exclude, seen);

        return candidates;
    }

    /**
     * Recommends movies sorted by rating (unlimited).
     * @return List of highest-rated movies user hasn't seen
     */
    public ArrayList<Movie> recommendByRating() {
        return recommendByRating(Integer.MAX_VALUE);
    }

    /**
     * Recommends movies sorted by rating (descending).
     * Algorithm:
     * 1. Get all movies sorted by rating (highest first)
     * 2. Exclude movies already in watchlist or history
     * 3. Return top N movies
     * 
     * @param limit Maximum number of recommendations to return
     * @return List of highest-rated movies user hasn't seen
     */
    public ArrayList<Movie> recommendByRating(int limit) {
        Set<String> exclude = buildExclusion();
        ArrayList<Movie> sorted = movieManager.sortByRatingDesc();
        ArrayList<Movie> res = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Movie m : sorted) {
            if (!exclude.contains(m.getId()) && seen.add(m.getId())) {
                res.add(m);
                if (limit > 0 && res.size() >= limit) {
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Recommends movies using premium hybrid algorithm (unlimited).
     * @return List of movies scored by weighted combination of factors
     */
    public ArrayList<Movie> recommendPremiumMix() {
        return recommendPremiumMix(Integer.MAX_VALUE);
    }

    /**
     * Recommends movies using premium hybrid algorithm with default config.
     * @param limit Maximum number of recommendations
     * @return List of movies scored by weighted combination of factors
     */
    public ArrayList<Movie> recommendPremiumMix(int limit) {
        return recommendPremiumMix(limit, new PremiumMixConfig());
    }

    /**
     * Recommends movies using premium hybrid algorithm.
     * Algorithm:
     * 1. Calculate user's average preferred year from history
     * 2. For each candidate movie, compute weighted score:
     *    - Genre weight: 50% (if enabled)
     *    - Year weight: 20% (if enabled)
     *    - Rating weight: 30% (if enabled)
     * 3. Sort by total score (descending)
     * 
     * @param limit Maximum number of recommendations
     * @param config Configuration for which factors to include
     * @return List of movies scored by weighted combination of factors
     */

    public ArrayList<Movie> recommendPremiumMix(int limit, PremiumMixConfig config) {
        User user = userManager.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }

        PremiumMixConfig cfg = new PremiumMixConfig(config);
        Map<String, Integer> historyCount = user.getHistory().getGenreCounts(movieManager);
        Map<String, Integer> watchlistCount = user.getWatchlist().getGenreCounts(movieManager);

        double targetYear = Double.NaN;
        int totalYear = user.getHistory().getTotalYearSum(movieManager);
        int count = user.getHistory().size();
        if (count > 0) targetYear = (double) totalYear / count;

        Set<String> exclude = buildExclusion();
        Set<String> seen = new HashSet<>();
        ArrayList<Movie> candidates = new ArrayList<>();

        for (Movie m : movieManager.getAllMovies()) {
            if (m == null) continue;
            if (!exclude.contains(m.getId()) && seen.add(m.getId())) {
                candidates.add(m);
            }
        }

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                double scoreA = premiumScore(candidates.get(i), historyCount, watchlistCount, targetYear, cfg);
                double scoreB = premiumScore(candidates.get(j), historyCount, watchlistCount, targetYear, cfg);
                if (scoreB > scoreA || (scoreB == scoreA && candidates.get(j).getRating() > candidates.get(i).getRating())) {
                    Movie tmp = candidates.get(i);
                    candidates.set(i, candidates.get(j));
                    candidates.set(j, tmp);
                }
            }
        }

        if (candidates.size() > limit) {
            ArrayList<Movie> trimmed = new ArrayList<>();
            for(int i=0; i<limit; i++) trimmed.add(candidates.get(i));
            candidates = trimmed;
        }

        fillWithRemainingMovies(candidates, limit, exclude, seen);

        return candidates;
    }

    public ArrayList<Movie> recommendByYear() {
        return recommendByYear(Integer.MAX_VALUE);
    }

    public ArrayList<Movie> recommendByYear(int limit) {
        User user = userManager.getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
        double targetYear = averagePreferenceYear();
        if (Double.isNaN(targetYear)) {
            return new ArrayList<>();
        }
        double windowRadius = 5.0;
        int historyBandCount = countHistoryInWindow(targetYear, windowRadius);
        int watchlistBandCount = countWatchlistInWindow(targetYear, windowRadius);

        Set<String> exclude = buildExclusion();
        Set<String> seen = new HashSet<>();
        ArrayList<Movie> candidates = new ArrayList<>();
        for (Movie m : movieManager.getAllMovies()) {
            if (!exclude.contains(m.getId()) && seen.add(m.getId())) {
                candidates.add(m);
            }
        }
        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                double scoreA = yearRecommendationScore(candidates.get(i), targetYear, windowRadius, historyBandCount, watchlistBandCount);
                double scoreB = yearRecommendationScore(candidates.get(j), targetYear, windowRadius, historyBandCount, watchlistBandCount);
                if (scoreB > scoreA || (scoreB == scoreA && candidates.get(j).getRating() > candidates.get(i).getRating())) {
                    Movie tmp = candidates.get(i);
                    candidates.set(i, candidates.get(j));
                    candidates.set(j, tmp);
                }
            }
        }
        if (limit > 0 && candidates.size() > limit) {
            return new ArrayList<>(candidates.subList(0, limit));
        }
        return candidates;
    }

    private double yearRecommendationScore(Movie movie, double targetYear, double windowRadius, int historyBandCount, int watchlistBandCount) {
        double yearScore = Math.max(0, 1 - Math.abs(movie.getYear() - targetYear) / 20.0);
        boolean inWindow = isWithinYearWindow(movie.getYear(), targetYear, windowRadius);
        int history = inWindow ? historyBandCount : 0;
        int watchlist = inWindow ? watchlistBandCount : 0;
        return yearScore * 2 + history * 2 + watchlist;
    }

    /**
     * Checks if a movie year falls within a specified window around a target year.
     * @param year The movie's release year
     * @param center The target year (center of the window)
     * @param radius The radius of the window (e.g., 5 years)
     * @return True if the year is within [center - radius, center + radius]
     */
    private boolean isWithinYearWindow(int year, double center, double radius) {
        return year >= center - radius && year <= center + radius;
    }

    /**
     * Counts how many movies in the user's history fall within the given year window.
     * @param center The target year
     * @param radius The window radius
     * @return The count of matching history entries
     */
    private int countHistoryInWindow(double center, double radius) {
        int count = 0;
        for (WatchHistoryEntry h : userManager.getHistory()) {
            Movie m = movieManager.getMovieById(h.getMovieId());
            if (m == null) continue;
            if (isWithinYearWindow(m.getYear(), center, radius)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many movies in the user's watchlist fall within the given year window.
     * @param center The target year
     * @param radius The window radius
     * @return The count of matching watchlist entries
     */
    private int countWatchlistInWindow(double center, double radius) {
        int count = 0;
        for (String id : userManager.getWatchlist()) {
            Movie m = movieManager.getMovieById(id);
            if (m == null) continue;
            if (isWithinYearWindow(m.getYear(), center, radius)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Builds a set of movie IDs to exclude from recommendations.
     * Includes all movies currently in the user's watchlist and history.
     * @return A Set of movie IDs to exclude
     */
    private Set<String> buildExclusion() {
        Set<String> set = new HashSet<>();
        User u = userManager.getCurrentUser();
        if (u != null) {
            set.addAll(userManager.getWatchlist());
            for (WatchHistoryEntry h : userManager.getHistory()) {
                set.add(h.getMovieId());
            }
        }
        return set;
    }

    /**
     * Calculates a weighted score for the Premium Mix strategy.
     * Combines genre, year, and rating scores based on the configuration.
     * @param movie The candidate movie
     * @param historyCount Genre counts from history
     * @param watchlistCount Genre counts from watchlist
     * @param targetYear The user's average preferred year
     * @param config The strategy configuration
     * @return The calculated weighted score
     */
    private double premiumScore(Movie movie, Map<String, Integer> historyCount, Map<String, Integer> watchlistCount, double targetYear, PremiumMixConfig config) {
        double baseGenreWeight = config.isUseGenre() ? 5.0 : 0.0;
        double baseYearWeight = config.isUseYear() ? 2.0 : 0.0;
        double baseRatingWeight = config.isUseRating() ? 3.0 : 0.0;
        double totalWeight = baseGenreWeight + baseYearWeight + baseRatingWeight;
        if (totalWeight <= 0) {
            return 0;
        }
        double wGenre = baseGenreWeight / totalWeight;
        double wYear = baseYearWeight / totalWeight;
        double wRating = baseRatingWeight / totalWeight;

        double genreScoreVal = genreScore(movie.getGenre(), historyCount, watchlistCount);
        double yearScore = 0;
        if (!Double.isNaN(targetYear)) {
            yearScore = Math.max(0, 1 - Math.abs(movie.getYear() - targetYear) / 20.0);
        }
        double ratingScore = movie.getRating();
        return genreScoreVal * wGenre + yearScore * wYear + ratingScore * wRating;
    }

    private String pickTopSubCategory(Map<String, Integer> subHistory, Map<String, Integer> subWatchlist) {
        String best = null;
        int bestScore = -1;
        String[] subs = new String[] {"SpeculativeMovie", "RealisticMovie"};
        for (int i = 0; i < subs.length; i++) {
            String key = subs[i];
            int score = subScore(key, subHistory, subWatchlist);
            if (score > bestScore) {
                bestScore = score;
                best = key;
            }
        }
        if (bestScore <= 0) {
            return null;
        }
        return best;
    }

    /**
     * Computes a raw score for a sub-category based on history and watchlist presence.
     * History is weighted double.
     * @param subCategory The sub-category name
     * @param subHistory map of sub-category counts from history
     * @param subWatchlist map of sub-category counts from watchlist
     * @return The computed score
     */
    private int subScore(String subCategory, Map<String, Integer> subHistory, Map<String, Integer> subWatchlist) {
        int history = subHistory.getOrDefault(subCategory, 0);
        int watch = subWatchlist.getOrDefault(subCategory, 0);
        return history * 2 + watch;
    }


    /**
     * Maps a movie to a sub-category (RealisticMovie or SpeculativeMovie).
     * Uses explicit type if available, otherwise infers from genre keywords.
     * @param movie The movie to map
     * @return The sub-category name, or null if unknown
     */
    private String mapSubCategory(Movie movie) {
        String type = movie.getType();
        if (type != null && !type.isEmpty()) {
            if ("SpeculativeMovie".equalsIgnoreCase(type)) {
                return "SpeculativeMovie";
            }
            if ("RealisticMovie".equalsIgnoreCase(type)) {
                return "RealisticMovie";
            }
        }
        String genre = movie.getGenre();
        String g = genre == null ? "" : genre.toLowerCase(Locale.ROOT);
        if (g.contains("drama") || g.contains("crime") || g.contains("mystery") || g.contains("war")) {
            return "RealisticMovie";
        }
        if (g.contains("sci-fi") || g.contains("science fiction") || g.contains("fantasy")
                || g.contains("thriller") || g.contains("horror") || g.contains("action")
                || g.contains("animation")) {
            return "SpeculativeMovie";
        }
        return null; // unknown subcategory counts as 0
    }

    /**
     * Builds a map of sub-category counts based on the user's history.
     * @param user The current user
     * @return A map of sub-category names to counts
     */
    private Map<String, Integer> buildSubCountsFromHistory(User user) {
        Map<String, Integer> counts = new HashMap<>();
        History history = user.getHistory();
        if (history != null) {
            for (WatchHistoryEntry h : history.getEntries()) {
                Movie m = movieManager.getMovieById(h.getMovieId());
                String sub = m == null ? null : mapSubCategory(m);
                if (sub != null) {
                    counts.put(sub, counts.getOrDefault(sub, 0) + 1);
                }
            }
        }
        return counts;
    }

    /**
     * Builds a map of sub-category counts based on the user's watchlist.
     * @param user The current user
     * @return A map of sub-category names to counts
     */
    private Map<String, Integer> buildSubCountsFromWatchlist(User user) {
        Map<String, Integer> counts = new HashMap<>();
        Watchlist watchlist = user.getWatchlist();
        if (watchlist != null) {
            for (String id : watchlist.getMovieIds()) {
                Movie m = movieManager.getMovieById(id);
                String sub = m == null ? null : mapSubCategory(m);
                if (sub != null) {
                    counts.put(sub, counts.getOrDefault(sub, 0) + 1);
                }
            }
        }
        return counts;
    }

    /**
     * Calculates a sub-category tie-breaker score.
     * The score is capped at maxScore to prevent it from overriding the primary genre score.
     * @param movie The movie to score
     * @param subHistory History counts
     * @param subWatchlist Watchlist counts
     * @param maxScore The primary genre score (used as a cap)
     * @return The sub-category score
     */
    private int subCategoryScore(Movie movie, Map<String, Integer> subHistory, Map<String, Integer> subWatchlist, int maxScore) {
        String sub = mapSubCategory(movie);
        if (sub == null || sub.isEmpty()) {
            return 0;
        }
        int raw = subScore(sub, subHistory, subWatchlist);
        // Cap to avoid outweighing genre score
        return Math.min(raw, maxScore);
    }

    /**
     * Computes the average release year of movies in the user's history and watchlist.
     * History items have full weight (1.0), watchlist items have half weight (0.5).
     * @return The weighted average year, or NaN if no data exists
     */
    private double averagePreferenceYear() {
        int historyTotal = 0;
        int historyCount = 0;
        for (WatchHistoryEntry h : userManager.getHistory()) {
            Movie m = movieManager.getMovieById(h.getMovieId());
            if (m == null) continue;
            historyTotal += m.getYear();
            historyCount++;
        }

        int watchlistTotal = 0;
        int watchlistCount = 0;
        for (String id : userManager.getWatchlist()) {
            Movie m = movieManager.getMovieById(id);
            if (m == null) continue;
            watchlistTotal += m.getYear();
            watchlistCount++;
        }

        double weightedTotal = historyTotal * 1.0 + watchlistTotal * 0.5;
        double weightedCount = historyCount * 1.0 + watchlistCount * 0.5;
        if (weightedCount == 0) {
            return Double.NaN;
        }
        return weightedTotal / weightedCount;
    }




    /**
     * Identifies the top 1 or 2 genres favored by the user.
     * @param historyCount A map of genre preference scores
     * @return A list containing the top 1 or 2 genres
     */
    private List<String> pickTopGenres(Map<String, Integer> historyCount) {
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(historyCount.entrySet());
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).getValue() > list.get(i).getValue()) {
                    Map.Entry<String, Integer> tmp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, tmp);
                }
            }
        }
        ArrayList<String> top = new ArrayList<>();
        if (!list.isEmpty()) {
            top.add(list.get(0).getKey());
        }
        if (list.size() > 1) {
            top.add(list.get(1).getKey());
        }
        return top;
    }

    /**
     * Calculates a genre preference score.
     * Formula: (History Count * 2) + Watchlist Count.
     * @param genre The genre to score
     * @param historyCount Map of history counts per genre
     * @param watchlistCount Map of watchlist counts per genre
     * @return The calculated score
     */
    private int genreScore(String genre, Map<String, Integer> historyCount, Map<String, Integer> watchlistCount) {
        int history = historyCount.getOrDefault(genre, 0);
        int watchlist = watchlistCount.getOrDefault(genre, 0);
        return history * 2 + watchlist;
    }

    /**
     * Fills the candidate list with additional movies if the limit hasn't been reached.
     * Adds movies that are not in the exclusion set and haven't been added yet.
     * @param candidates The current list of candidates
     * @param limit The target number of recommendations
     * @param exclude The set of excluded movie IDs
     * @param seen The set of movie IDs already in the candidate list
     */
    private void fillWithRemainingMovies(ArrayList<Movie> candidates, int limit, Set<String> exclude, Set<String> seen) {
        if (candidates.size() >= limit) return;

        for (Movie m : movieManager.getAllMovies()) {
            if (candidates.size() >= limit) break;
            if (m == null) continue;

            String id = m.getId();
            if (!exclude.contains(id) && !seen.contains(id)) {
                candidates.add(m);
                seen.add(id);
            }
        }
    }
}

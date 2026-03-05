package movietracker.backend;

public final class PremiumMixConfig {
    private boolean useGenre;
    private boolean useYear;
    private boolean useRating;

    public PremiumMixConfig() {
        this.useGenre = true;
        this.useYear = true;
        this.useRating = true;
    }

    public PremiumMixConfig(PremiumMixConfig other) {
        if (other == null) {
            this.useGenre = true;
            this.useYear = true;
            this.useRating = true;
        } else {
            this.useGenre = other.useGenre;
            this.useYear = other.useYear;
            this.useRating = other.useRating;
        }
    }

    public boolean isUseGenre() {
        return useGenre;
    }

    public void setUseGenre(boolean useGenre) {
        this.useGenre = useGenre;
    }

    public boolean isUseYear() {
        return useYear;
    }

    public void setUseYear(boolean useYear) {
        this.useYear = useYear;
    }

    public boolean isUseRating() {
        return useRating;
    }

    public void setUseRating(boolean useRating) {
        this.useRating = useRating;
    }
}

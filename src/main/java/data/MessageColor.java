package data;

public enum MessageColor {
    GREEN(":greenlight: "),
    ORANGE(":orangelight: "),
    YELLOW(":yellow_circle: "),
    QUESTION(":question: ");

    private final String emoji;

    MessageColor(String s) {
        this.emoji = s;
    }

    public String getEmoji() {
        return emoji;
    }
}

import com.google.common.collect.Ordering;
import data.Issue;
import data.IssueGroup;
import data.MessageColor;
import org.kohsuke.github.GHMilestone;

import java.util.Comparator;
import java.util.Set;

import static data.MessageColor.GREEN;
import static data.MessageColor.ORANGE;
import static data.MessageColor.QUESTION;
import static data.MessageColor.YELLOW;
import static java.util.Comparator.comparing;

public class MessageAssembler {
    private static final Comparator<MessageLine> BY_COLOR =
            comparing(MessageLine::getMessageColor, Ordering.explicit(GREEN, ORANGE, YELLOW, QUESTION));

    private static final String BASE_JIRA_URL = ""; // TODO: make generic
    private static final String URL_PATTERN = "[%s](%s)"; // [text](link)

    public MessageAssembler() {
    }

    public String createMessage(Set<IssueGroup> issueGroups, GHMilestone milestone) {
        return createHeader(milestone) + issueGroups
                .stream()
                .flatMap(issueGroup -> issueGroup.getIssues().stream())
                .map(this::getIssueByColor)
                .sorted(BY_COLOR)
                .map(MessageLine::getMessage)
                .reduce("", (a, b) -> a + "\n" + b) + createLegend();
    }

    private String createHeader(GHMilestone milestone) {
        String milestoneDescription = URL_PATTERN.formatted("(see milestone)", milestone.getHtmlUrl().toString());
        return  "Hello everyone, this week we release milestone: *" + milestone.getTitle() + " " + milestoneDescription
                + "*\n" + "If you don’t see your PR in this list, feel free to contact me, so I can include it. :cattyping:";
    }

    private String createLegend() {
        return "\n\n*Legend:*\n" + GREEN.getEmoji() + "- Verified\n" + ORANGE.getEmoji() + "- Merged\n" + YELLOW.getEmoji() + "- In PR\n" + QUESTION.getEmoji() + "- No idea\n";
    }

    private MessageLine getIssueByColor(Issue issue) {
        final String message = createMessageWithLink(issue.getTitle());
        return switch (issue.getJiraStatus()) {
            case VERIFIED -> new MessageLine(GREEN.getEmoji() + message, GREEN);
            case IN_CODE_REVIEW, IN_PROGRESS -> new MessageLine(YELLOW.getEmoji() + message, YELLOW);
            case READY_FOR_TEST -> new MessageLine(ORANGE.getEmoji() + message, ORANGE);
            case BACKLOG, UNKNOWN -> new MessageLine(QUESTION.getEmoji() + message, QUESTION);
        };
    }

    private String createMessageWithLink(String issueTitle) {
        String[] parts = issueTitle.split(" ", 2);
        return URL_PATTERN.formatted(parts[0], BASE_JIRA_URL + parts[0]) + " " + parts[1];
    }

    static class MessageLine {
        private final String message;
        private final MessageColor messageColor;

        MessageLine(String message, MessageColor messageColor) {
            this.message = message;
            this.messageColor = messageColor;
        }

        public String getMessage() {
            return message;
        }

        public MessageColor getMessageColor() {
            return messageColor;
        }
    }
}

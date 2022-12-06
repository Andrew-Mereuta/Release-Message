import data.JiraStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static data.JiraStatus.BACKLOG;
import static data.JiraStatus.IN_CODE_REVIEW;
import static data.JiraStatus.IN_PROGRESS;
import static data.JiraStatus.READY_FOR_TEST;
import static data.JiraStatus.UNKNOWN;
import static data.JiraStatus.VERIFIED;

public class JiraClient {
    private static final String BASE_URL = ""; // TODO: make generic
    private final WebClient webClient;

    public JiraClient(String jiraToken) {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Cookie", "cloud.session.token=" + jiraToken)
                .build();
    }

    public Mono<JiraStatus> getIssueStatus(String issueCode) {
        return retrieveIssue(issueCode)
                .map(this::getJiraStatus);
    }

    private JiraStatus getJiraStatus(Map<String, Object> response) {
        return Optional.ofNullable(response.get("fields"))
                .map(fields -> (Map<String, Object>) fields)
                .flatMap(fields -> Optional.ofNullable(fields.get("status")))
                .map(status -> (Map<String, String>) status)
                .flatMap(status -> Optional.ofNullable(status.get("name")))
                .map(this::parseJiraStatus)
                .orElse(UNKNOWN);
    }

    private Mono<Map<String, Object>> retrieveIssue(String issueCode) {
        return webClient
                .get()
                .uri("{issueCode}", issueCode)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {});
    }

    private JiraStatus parseJiraStatus(String status) {
        return switch (status) {
            case "In Code Review" -> IN_CODE_REVIEW;
            case "Verified" -> VERIFIED;
            case "In Progress" -> IN_PROGRESS;
            case "Ready for Test" -> READY_FOR_TEST;
            case "Backlog" -> BACKLOG;
            default -> UNKNOWN;
        };
    }
}

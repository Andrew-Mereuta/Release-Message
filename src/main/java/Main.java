import data.Issue;
import data.IssueGroup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

public class Main {
    private static final String TOKEN = "";

    private static final JiraClient jiraClient = new JiraClient(TOKEN);
    private static final RepositoryReader reader = new RepositoryReader();
    private static final MessageAssembler assembler = new MessageAssembler();

    public static void main(String[] args) {
        String message =
                reader.getIssueGroups()
                .flatMapMany(Flux::fromIterable)
                .flatMap(Main::setJiraStatus)
                .collect(Collectors.toSet())
                .zipWith(reader.getClosestMilestone())
                .map(tuple -> assembler.createMessage(tuple.getT1(), tuple.getT2()))
                .block(Duration.ofSeconds(2));
        System.out.println(message);
    }

    private static Mono<IssueGroup> setJiraStatus(IssueGroup issueGroup) {
        return switch (issueGroup.getGitHubStatus()) {
            case MERGED -> Flux.fromIterable(issueGroup.getIssues())
                    .flatMap(Main::setJiraStatus)
                    .collect(Collectors.toSet())
                    .map(group -> new IssueGroup(group, issueGroup.getGitHubStatus()));
            case OPEN -> Mono.fromCallable(() -> issueGroup);
        };
    }

    private static Mono<Issue> setJiraStatus(Issue issue) {
        return jiraClient.getIssueStatus(issue.getTitle().split(" ", 2)[0])
                .map(jiraStatus -> new Issue(issue.getTitle(), issue.getGitHubStatus(), jiraStatus));
    }
}

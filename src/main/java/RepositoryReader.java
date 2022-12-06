import data.Issue;
import data.IssueGroup;
import data.GitHubStatus;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RepositoryReader {
    private static final String TOKEN = System.getenv("GITHUB_TOKEN");
    private static final String REPOSITORY_NAME = ""; // TODO: make generic
    private final GHRepository repository;

    public RepositoryReader() {
        try {
            this.repository = new GitHubBuilder()
                    .withAppInstallationToken(TOKEN)
                    .build()
                    .getRepository(REPOSITORY_NAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<Set<IssueGroup>> getIssueGroups() {
        return getClosestMilestone()
                .flatMap(this::getIssueGroups);
    }

    public Mono<GHMilestone> getClosestMilestone() {
        return Mono.defer(new Supplier<Mono<GHMilestone>>() {
            @Override
            public Mono<GHMilestone> get() {
                return Mono.fromCallable(() -> repository
                        .listMilestones(GHIssueState.OPEN)
                        .toList()
                        .stream()
                        .min(new MilestoneComparator())
                        .orElseThrow());
            }
        });
    }


    public Mono<Set<IssueGroup>> getIssueGroups(GHMilestone milestone) {
        return getIssuesByState(milestone)
                .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
                .map(e -> new IssueGroup(e.getValue(), e.getKey()))
                .collect(Collectors.toSet());
    }

    private Mono<Map<GitHubStatus, Set<Issue>>> getIssuesByState(GHMilestone milestone) {
        return Mono.defer(new Supplier<Mono<Map<GitHubStatus, Set<Issue>>>>() {
            @Override
            public Mono<Map<GitHubStatus, Set<Issue>>> get() {
                return Mono.fromCallable(() -> repository.getIssues(GHIssueState.ALL, milestone)
                        .stream()
                        .map(issue -> new Issue(issue.getTitle().trim(), getIssueState(issue)))
                        .filter(exclude("PSM", "PRP"))
                        .collect(Collectors.groupingBy(Issue::getGitHubStatus, Collectors.toSet())));
            }
        });
    }

    private GitHubStatus getIssueState(GHIssue issue) {
        return issue.getState() == GHIssueState.CLOSED ? GitHubStatus.MERGED : GitHubStatus.OPEN;
    }

    private Predicate<Issue> exclude(String... prefixes) {
        return issue -> {
            for (String prefix : prefixes) {
                if (issue.getTitle().startsWith(prefix)) return false;
            }
            return true;
        };
    }
}

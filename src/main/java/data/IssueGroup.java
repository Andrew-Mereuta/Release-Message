package data;

import java.util.Set;

public class IssueGroup {
    private final Set<Issue> issues;
    private final GitHubStatus gitHubStatus;

    public IssueGroup(Set<Issue> issues, GitHubStatus gitHubStatus) {
        this.issues = issues;
        this.gitHubStatus = gitHubStatus;
    }

    public Set<Issue> getIssues() {
        return issues;
    }

    public GitHubStatus getGitHubStatus() {
        return gitHubStatus;
    }
}

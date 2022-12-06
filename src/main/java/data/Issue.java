package data;

public class Issue {
    private final String title;
    private final GitHubStatus gitHubStatus;
    private final JiraStatus jiraStatus;

    public Issue(String title, GitHubStatus gitHubStatus, JiraStatus jiraStatus) {
        this.title = title;
        this.gitHubStatus = gitHubStatus;
        this.jiraStatus = jiraStatus;
    }

    public Issue(String title, GitHubStatus gitHubStatus) {
        this.title = title;
        this.gitHubStatus = gitHubStatus;
        this.jiraStatus = switch (gitHubStatus) {
            case MERGED -> JiraStatus.UNKNOWN;
            case OPEN -> JiraStatus.IN_CODE_REVIEW;
        };
    }

    public String getTitle() {
        return title;
    }

    public GitHubStatus getGitHubStatus() {
        return gitHubStatus;
    }

    public JiraStatus getJiraStatus() {
        return jiraStatus;
    }
}

import org.kohsuke.github.GHMilestone;

import java.util.Comparator;

public class MilestoneComparator implements Comparator<GHMilestone> {

    @Override
    public int compare(GHMilestone o1, GHMilestone o2) {
        return o1.getDueOn().compareTo(o2.getDueOn());
    }
}

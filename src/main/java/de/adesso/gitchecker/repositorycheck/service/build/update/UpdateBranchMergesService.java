package de.adesso.gitchecker.repositorycheck.service.build.update;

import de.adesso.gitchecker.repositorycheck.domain.BitBucketRepository;
import de.adesso.gitchecker.repositorycheck.domain.Branch;
import de.adesso.gitchecker.repositorycheck.domain.Commit;
import de.adesso.gitchecker.repositorycheck.domain.Ruleset;
import de.adesso.gitchecker.repositorycheck.port.driver.UpdateBranchMergesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class UpdateBranchMergesService implements UpdateBranchMergesUseCase {

    private final Ruleset ruleset;

    @Override
    public void update(BitBucketRepository repository) {
        if (areBranchMergesRequired()) {
            repository.getMergeCommits().values().forEach(this::assignBranchMergesOfMergeCommit);
        }
    }

    private void assignBranchMergesOfMergeCommit(Commit mergeCommit) {
        Branch mergedInto = mergeCommit.getParentCommits().get(0).getCreatorBranch();

        for (int i = 1; i < mergeCommit.getParentCommits().size(); i++) {

            if (!isCommitOfRemovedBranch(mergeCommit.getParentCommits().get(0))
                    && !isCommitOfRemovedBranch(mergeCommit.getParentCommits().get(i))
                    && isDifferentBranch(mergeCommit.getParentCommits().get(0), mergeCommit.getParentCommits().get(i))) {

                updateBranchMergedInto(mergeCommit.getParentCommits().get(i).getCreatorBranch(), mergedInto);
            }
        }
    }

    private void updateBranchMergedInto(Branch from, Branch to) {
        from.getMergedInto().add(to);
    }

    private boolean isCommitOfRemovedBranch(Commit commit) {
        return commit.getCreatorBranch().getBranchType() != null
                && commit.getCreatorBranch().getBranchType().equalsIgnoreCase("REMOVED");
    }

    private boolean isDifferentBranch(Commit from, Commit to) {
        return !from.getCreatorBranch().equals(to.getCreatorBranch());
    }

    private boolean areBranchMergesRequired() {
        return nonNull(ruleset.getAllowedBranchMerges());
    }
}

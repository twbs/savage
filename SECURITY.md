## DISCLAIMER
The author is not a security expert and this project has not been subjected to a third-party security audit.

## Responsible disclosure; Security contact info

The security of Savage is important to us. We encourage you to report security problems to us responsibly.

Please report all security bugs to `savage {AT} rebertia [DOT] com`. We aim to respond (with at least an acknowledgment) within one business day. We will keep you updated on the bug's status as we work towards resolving it.

We will disclose a problem to the public once it has been confirmed and a fix has been made available. At that point, you will be credited for your discovery in the documentation, in the release announcements, and (if applicable) in the code itself.

As Savage currently lacks corporate backing, we are unfortunately unable to offer bounty payments at this time.

We thank you again for helping ensure the security of Savage by responsibly reporting security problems.

## System model

### System operation
(Note: PR = pull request)

```
[GitHub]  >>>(Webhook notification of new/updated PR)>>>  [Savage]
* Savage verifies that the notification was really from GitHub (and not an impostor)
    by verifying the HMAC-SHA1 computed using the web hook secret key previously configured with GitHub.

[GitHub]  <<<(Request details about the PR using the PR's HEAD commit's SHA)<<<  [Savage]
[GitHub]  >>>(Response with details about the PR)>>>  [Savage]
* Savage checks list of files modified by the PR against the whitelist
  * If any files are outside of the whitelist, stop further processing.

[GitHub]  <<<(Request for Git data for the PR's HEAD commit via its SHA)<<<  [Savage]
[GitHub]  >>>(Response with Git data for the PR's HEAD commit)>>>  [Savage]
* Savage generates a new branch name using the PR number and a specified prefix

[GitHub]  >>>(Fetch refs from PR's GitHub repo)>>> [Savage]
[GitHub]  <<<(Pushes new branch to test repository using the PR's HEAD commit, referenced via its SHA)<<<  [Savage]
[GitHub]  >>>(Notifies Travis of the test repository's newly-pushed branch)>>>  [Travis CI]
* Travis CI runs the build with the privileges of the test repository
  * Notably, it has access to Travis CI secure environment variables

[Travis CI] >>>(Outcome of build)>>> [Savage]
* Savage verifies that the notification was really from Travis CI (and not an impostor)
    by verifying the signature in the `Authorization` header using the secret Travis user token.

[GitHub]  <<<(Post comment on PR regarding build outcome)<<<  [Savage]
[GitHub]  <<<(Delete branch from test repository)<<<  [Savage]
```

Remarks:
At no point do we use the PR's branch name directly. We also delete all fetched branches after the push is completed. This avoids maliciously crafted branch names which could be misinterpreted by other systems and also ensures that the attacker cannot change the contents of the branch out from under us, thus avoiding [TOCTTOU](http://en.wikipedia.org/wiki/Time_of_check_to_time_of_use) vulnerabilities.

## Threat model

### Assumptions
(These are admittedly generous.)
* We trust the machine that Savage is running on
* We trust GitHub
* We trust Travis CI
* We trust that the EGit-GitHub library communicates with GitHub securely
* We assume that the git command binaries are secure so long as they are only invoked with secure arguments
* We assume that our build scripts are secure (this is outside the scope and control of Savage itself)
* We assume that the filename whitelist is correct

### Architecture-based threat analysis
Out of scope per our assumptions:
* Compromise of GitHub
* Compromise of Travis CI API
* Compromise of the machine on which Savage resides
* Compromise of out outbound communications with GitHub
* Allowing modification of a sensitive file due to incorrect whitelist settings

Within scope:
* Impersonating GitHub and delivering a malicious webhook notification
  * Prevented by our checking of the HMAC-SHA1 signature of the webhook payload
* Impersonating Travis and delivering a malicious webhook notification
  * Prevented by our checking of the SHA-256 signature of the webhook payload
* Shell-related vulnerabilities
  * Avoided by not using the shell when invoking git; we use Java's `ProcessBuilder`/`Process` instead
* Compromising the git fetch/push command via malicious input
  * Avoided by checking that the relevant git-related data isn't fishy
* Compromising the git branch deletion command via malicious input
  * The command involves only a Savage-generated branch name, whose computation is simple and which is checked for validity. We believe this thus avoids the vulnerability.
* Compromising the contents of the posted GitHub comment via malicious input
  * Avoided by checking that the relevant data from Travis isn't fishy

### Asset-centric threat analysis
Assets:
* Savage's GitHub credentials
  * We don't believe this information is leaked by Savage itself.
  * We don't believe the git commands can be induced to access the relevant configuration file that has the credentials.
  * Travis deserializes the API responses as vanilla JSON; it doesn't `eval()` them; spray-json doesn't have any deserialization features that allow the execution of arbitrary code (contrast this with YAML and some of its implementations).
* Write access to the test GitHub repo
  * We believe that the various checks that Savage performs on the inputs and the fact that it is only capable of performing a couple git operations prevents malicious access to the test repo.
* Commenting ability on the main GitHub repo
  * Savage only uses the commit SHA and the Travis build URL in its comment text, and both of these are checked for validity/safety.
* Credentials stored in Travis secure environment variables
  * Under our somewhat generous assumptions, this should be impossible.

## Notes on securing build scripts
* Beware malicious Git input (branch names, commit messages, author info, etc.)
* Beware malicious Travis input (e.g. environment variables)
* Beware potentially-executable data files (e.g. `eval()`ing of JSON, YAML custom type deserialization hooks)
* Beware the addition of files with maliciously-chosen names
* Ensure that build scripts are absent from the whitelist
* Ensure package management control files are absent from the whitelist, to prevent the installation of malicious packages

# Detailed setup guide for Savage

This guide covers the current best (although not only) way to setup a Savage instance for running Sauce Labs tests on pull requests.

If you're want to run Savage in [Docker](https://www.docker.com), you might want to consult Bootstrap's [`setup_droplet.sh`](https://github.com/twbs/savage/blob/master/setup_droplet.sh) and [Dockerfile](https://github.com/twbs/savage/blob/master/Dockerfile) as references.

1. For maximum security, create a [Sauce sub-account](https://saucelabs.com/sub-accounts) for use by Savage.
2. In your project's main GitHub repo, setup the ["Travis webhook configuration"](https://github.com/twbs/savage#travis-webhook-configuration) to your `.travis.yml` .
3. Create a GitHub user account for Savage.
4. While logged in as your Savage user, fork your project's main GitHub repo. This fork will be used as the *test repo* referred to in Savage's README.
5. For convenience, you should probably add yourself or your project's team as Collaborators on the *test repo*.
6. While logged into GitHub as your Savage user, log into [Travis CI](https://travis-ci.org) and enable Travis CI on the *test repo* via https://travis-ci.org/profile
7. In your project's main GitHub repo, [add Travis secure environment variables](http://docs.travis-ci.com/user/environment-variables/) for your Sauce username and access key, but make sure to **encrypt these for the *test repo***, not for your main repo; (e.g. `travis encrypt --repo your-savage-user/name-of-repo SAUCE_USERNAME=the-username`).
8. On your local computer (or on your server), clone the `twbs/savage` git repo.
9. Edit `src/main/resources/application.conf` to configure your instance appropriately. See [the README](https://github.com/twbs/savage/blob/master/README.md#usage) for descriptions of all the available settings.
10. Build Savage's all-in-one "assembly" JAR. See ["How do I generate a single self-sufficient JAR that includes all of the necessary dependencies?"](https://github.com/twbs/savage/blob/master/CONTRIBUTING.md#how-do-i-generate-a-single-self-sufficient-jar-that-includes-all-of-the-necessary-dependencies) for instructions on how to do that.
11. Copy the Savage JAR to the location where you'll be running it on your server.
12. Install Java 7+, Git, and OpenSSH in the environment where you will be running Savage.
13. Create a Unix user account for Savage. Use whatever username you want.
14. As Savage's Unix user, run `ssh-keyscan -t rsa github.com > ~/.ssh/known_hosts` to grab and confirm GitHub's SSH public key.
15. Generate an SSH key for Savage's Unix user.
16. Add the SSH key to Savage's GitHub account via https://github.com/settings/ssh
17. On your server, clone your main project's GitHub repo to a location that is writable by Savage's Unix user.
18. As Savage's Unix user, with the working directory set to the clone of the main project's GitHub repo, run `java -jar savage-assembly-1.0.jar` . You may want to setup an initscript or other automation for this. Savage will output logs to stdout/stderr.
19. In your main project's GitHub repo, setup the GitHub webhook for Savage, as explained in ["GitHub webhook configuration"](https://github.com/twbs/savage#github-webhook-configuration).
20. Your Savage instance should now be ready to use! Go ahead and test it out.

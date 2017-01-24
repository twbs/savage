# Detailed setup guide for Savage

This guide covers the current best (although not only) way to setup a Savage instance for running Sauce Labs tests on pull requests.

If you're want to run Savage in [Docker](https://www.docker.com), you might want to consult Bootstrap's [`setup_droplet.sh`](https://github.com/twbs/savage/blob/master/setup_droplet.sh) and [Dockerfile](https://github.com/twbs/savage/blob/master/Dockerfile) as references.

1. For maximum security, create a [Sauce sub-account](https://saucelabs.com/sub-accounts) for use by Savage. (In the event of a security vulnerability in Savage, only the sub-account will be liable to compromise; your main account will remain secure, and you can then simply reset the credentials for the sub-account, or delete the sub-account and create a fresh sub-account.)
2. In your project's main GitHub repo, setup the ["Travis webhook configuration"](https://github.com/twbs/savage#travis-webhook-configuration) to your `.travis.yml` . (This way, the hook configuration will be propagated when we fork the repo later.)
3. Create a GitHub user account for Savage. (The account will be used to post GitHub comments and own a GitHub fork of the main project's repo.)
4. While logged in as your Savage user, fork your project's main GitHub repo. This fork will be used as the *test repo* referred to in Savage's README.
5. For convenience, you should probably add yourself or your project's team as Collaborators on the *test repo*.
6. While logged into GitHub as your Savage user, log into [Travis CI](https://travis-ci.org) and enable Travis CI on the *test repo* via https://travis-ci.org/profile
7. In your project's main GitHub repo, [add Travis secure environment variables](http://docs.travis-ci.com/user/environment-variables/) for your Sauce username and access key, but make sure to **encrypt these for the** ***test repo***, NOT for your main repo; (e.g. `travis encrypt --repo your-savage-user/name-of-repo SAUCE_USERNAME=the-username`). (This will grant the *test repo's* Travis builds access to Sauce.)
8. Push the commit from step 7 to the *test repo*. (Step 5 can't be combined into Step 2 because Travis can't encrypt variables for a not-yet-existent GitHub repo.)
9. On your local computer (or on your server), clone the `twbs/savage` git repo.
10. Edit `src/main/resources/application.conf` to configure your instance appropriately. See [the README](https://github.com/twbs/savage/blob/master/README.md#usage) for descriptions of all the available settings.
11. Build Savage's all-in-one "assembly" JAR. See ["How do I generate a single self-sufficient JAR that includes all of the necessary dependencies?"](https://github.com/twbs/savage/blob/master/CONTRIBUTING.md#how-do-i-generate-a-single-self-sufficient-jar-that-includes-all-of-the-necessary-dependencies) for instructions on how to do that.
12. Copy the Savage JAR to the location where you'll be running it on your server.
13. Install Java 8+, Git, and OpenSSH in the environment where you will be running Savage.
14. Create a Unix user account for Savage. Use whatever username you want.
15. As Savage's Unix user, run `ssh-keyscan -t rsa github.com > ~/.ssh/known_hosts` to grab and confirm GitHub's SSH public key.
16. Generate an SSH key for Savage's Unix user. (This will be used to securely pull-from/push-to GitHub.)
17. Add the SSH key to Savage's GitHub account via https://github.com/settings/ssh
18. On your server, clone your main project's GitHub repo to a location that is writable by Savage's Unix user. (Savage uses this as a scratch repo to fetch pull requests into and then push them to the *test repo*.)
19. As Savage's Unix user, with the working directory set to the clone of the main project's GitHub repo, run `java -jar savage-assembly-1.0.jar` . You may want to setup an initscript or other automation for this. Savage will output logs to stdout/stderr.
20. In your main project's GitHub repo, setup the GitHub webhook for Savage, as explained in ["GitHub webhook configuration"](https://github.com/twbs/savage#github-webhook-configuration). (This webhook is used to notify Savage when new pull requests are created or existing pull requests are updated.)
21. Your Savage instance should now be ready to use! Go ahead and test it out.

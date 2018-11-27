## Contributing

All contributions are welcome.

Take a look at the [Main Board] and see if there is anything in the `To do` column which you would like to work on.
The highest priority issues are at the top.

You may also look for any issues that are labeled with [help wanted].

Just because an issue does not have the [help wanted] label does not mean that help is not appreciated but just be aware
that we may already have plans to work on it.

Add a comment to the ticket to say that you are working on the issue and we will assign it to you and update the
relevant boards.

### Verified Commits

Make sure that you sign all your commits. See [Github - Signing Commits] for more details.

### Testing

#### Unit Tests

Run `test` for regular unit tests.

#### Plugin Tests

There are [sbt script tests][Testing Plugins] for each supported GnuPG version.
There is a configuration for each GnuPG version.
For example, there is a `V2_2` configuration for GnuPG version 2.2.
The `scripted` task with the `Compile` configuration scope will not work you must scope it to a particular
version configuration.

The tests are also organized into separate directories which correspond to the configuration name. This is automatically
provided to the `scritped` task so you only need to specify the test name (or `*` for all tests for that version).

For example:
```sbtshell
V2_2/scripted sign-message
```

The plugin tests use [Docker] to run the different GnuPG versions so ensure that you have it installed.

The details on how this works is all in [ScriptedGpgDockerPlugin](project/ScriptedGpgDockerPlugin.scala).

##### Debugging

You may need to debug the tests or to run commands in the Docker container such as generating new keys when the expire.

The easiest way to do that is to edit one of the scripted tests and add `$ pause` to the `test` script. Then run the
test and it will pause.
```sbtshell
Running v2_1 / sign-message
Pausing in /var/folders/_x/rggg7p_16qq5p4fsjpg14r340000gp/T/sbt_cced8d4c/sign-message
Press enter to continue.
```

Next change to the test directory and run the docker command. For example:
```bash
docker run -it --mount type=bind,source=/private$(pwd),target=/root/sbt-gpg nz.co.bottech/sbt-gpg:xenial /bin/bash
```

This will run bash in the Docker container that uses Ubuntu Xenial with GnuPG version 2.1.
It will also mount the current directory to `/root/sbt-gpg` inside the container. You can copy files here to then use
once you exit the container.

For example:
```bash
# Generate a key.
gpg2 --full-gen-key
# Cleanup any remaining sockets.
gpgconf --kill gpg-agent
rm -f /root/.gnupg/S.gpg-agent*
# Copy the files back to the target directory.
cp -R /root/.gnupg /root/sbt-gpg/target/
```

### Versioning

Version numbers are determined automatically using [dwijnand/sbt-dynver].

To create a new version add a new git annotated tag:
```bash
git tag -as v1.1.0
```

NOTE: The tag must be annotated (`-a`) and signed (`-s`).

### Continuous Integration

Continuous integration builds are done with [Travis CI].

### Publishing

The Travis CI build will automatically publish to [Bintray] and [GitHub] for all tagged commits on master.

#### Signed Artifacts

The artifacts are signed with key belonging to [BotTech]. Reach out to us if there are any issues with this.

#### Bintray

This uses [sbt/sbt-bintray] to publish artifacts to Bintray and [ohnosequences/sbt-github-release] to publish to GitHub.

[Bintray]: https://bintray.com
[BotTech]: https://github.com/BotTech
[Docker]: https://www.docker.com
[dwijnand/sbt-dynver]: https://github.com/dwijnand/sbt-dynver
[GitHub]: https://github.com
[GitHub - Signing Commits]: https://help.github.com/articles/signing-commits/
[help wanted]: https://github.com/BotTech/sbt-gpg/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22
[Main Board]: https://github.com/BotTech/sbt-gpg/projects/1
[ohnosequences/sbt-github-release]: https://github.com/ohnosequences/sbt-github-release
[sbt/sbt-bintray]: https://github.com/sbt/sbt-bintray
[Testing sbt Plugins]: http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html
[Travis CI]: https://travis-ci.org

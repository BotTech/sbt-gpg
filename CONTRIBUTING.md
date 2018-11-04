## Contributing

TODO.

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

Version numbers are determined automatically using [sbt-dynver].

To create a new version add a new git annotated tag:
```bash
git tag -a v1.1.0
```

### Continuous Integration

Continuous integration builds are done with [Travis CI].

### Publishing

The Travis CI build will automatically publish to [Bintray] and [GitHub] for all tagged commits on master.

#### Signed Artifacts

TODO.

#### Bintray

This uses [sbt-bintray] to publish artifacts to Bintray.

[Bintray]: https://bintray.com
[Docker]: https://www.docker.com
[Github]: https://github.com
[sbt-bintray]: https://github.com/sbt/sbt-bintray
[sbt-dynver]: https://github.com/dwijnand/sbt-dynver
[Testing sbt Plugins]: http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html
[Travis CI]: https://travis-ci.org

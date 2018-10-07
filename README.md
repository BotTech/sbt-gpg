# sbt-gpg

[![Build Status]](https://travis-ci.org/BotTech/sbt-gpg)
[![Download]](https://bintray.com/bottech/sbt-plugins/sbt-gpg/_latestVersion)

An sbt plugin to sign artifacts using the [GNU Privacy Guard][GnuPG] (GnuPG).

| GnuPG Version | Tasks |
| ------------- | ----- |
| 2.0           | Does not support the `gpgAddKey` task |
| 2.1           | Does not support the `gpgAddKey` task |
| 2.2           | All |

## Setup

This plugin requires sbt 1.0.0+.

To use the plugin you must first [download][GnuPG Download] and install GnuPG.

Add this plugin to your `project/plugins.sbt` file:
```scala
addSbtPlugin("nz.co.bottech" % "sbt-gpg" % "1.0.0")
```

## Usage

The common scenario for using this plugin is:
1. Generate a master key (unless you already have one).
1. Add a new subkey for your project.
1. Export the subkey.
1. Encrypt the subkey and commit it to the project repository.
1. During the automated build, decrypt and then import the subkey.
1. Build the project artifacts and sign them.

The referenced settings from all the main tasks have been scoped to that task to make it easier to override just the
ones that you care about without impacting other tasks. For example:
```sbtshell
gpgExportSubkey / gpgHomeDir := file("~/.gnupg")
gpgImportKey / gpgHomeDir := target.value / ".gnupg"
```

Use `inspect` to see what the scope of various settings are. See [sbt - Inspecting the Build] for more details.

### GnuPG Home Directory

All tasks can use a specific home directory for GnuPG by setting `gpgHomeDir`.

If this is not set (or set to `None`) then GnuPG will use the default home directory `~/.gnupg`.

### Passphrase

The following will be used to determine the passphrase:
1. `gpgPassphrase` setting.
1. `credentials` task.
1. pinentry.

If you are just running the tasks from an interactive sbt session then it is best to not specify the passphrase in sbt
or a credentials file and instead let GnuPG prompt you for it using pinentry.

If you need to run a task from a non-interactive session, such as during an automated build, then it is best to use the
`credentials` task. See [sbt - Credentials] for more details. The `host` must be `gpg`. The `realm` and `user` are not
used. Make sure to secure any credentials file appropriately.

### Generate Key

`gpgGenerateKey` - Generates a new key pair.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgNameReal` | The name to use in the key. | &#X2714; |  |
| `gpgNameEmail` | The email to to use in the key. | &#X2714; |  |
| `gpgKeyLength` | The length of the generated key in bits. | &#X2718; | 4096 |
| `gpgKeyType` | The OpenPGP algorithm number or name to use for the key. | &#X2718; | RSA |
| `gpgKeyUsage` | The list of key usages. | &#X2718; | `Set()` (will use GnuPG default) |
| `gpgSubkeyLength` | The length of the generated subkey in bits. | &#X2718; | 4096 |
| `gpgSubkeyType` | The OpenPGP algorithm number or name to use for the subkey. | &#X2718; | RSA |
| `gpgSubkeyUsage` | The list of subkey usages. | &#X2718; | sign |
| `gpgExpireDate` | The expiration date for the key (and the subkey). | &#X2718; | 0 (does not expire) |

Comments are not encouraged and are therefore not provided as an option.

WARNING - If you set `gpgGenerateKey / gpgSelectPassphrase` then this will end up in the generated `gpgParametersFile`.
You should take care to secure this file and delete it when it is no longer needed (e.g. `clean`).

### List Keys

`gpgListKeys` - List the existing keys.

### Add Key

`gpgAddKey` - Adds a subkey to an existing key.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the master key. | &#X2718; | default |
| `gpgSubkeyLength` | The length of the generated subkey in bits. | &#X2718; | 4096 |
| `gpgSubkeyType` | The OpenPGP algorithm number or name to use for the subkey. | &#X2718; | RSA |
| `gpgSubkeyUsage` | The list of subkey usages. | &#X2718; | sign |

Use `gpgListKeys` to find the key fingerprint.

The best practice is to not keep your master private key on the key ring of the machine that you use but to instead use
subkeys. You can read a good introduction to this on [Debian Wiki - Subkeys].

### Export Key

`gpgExportSubkey` - Exports a subkey without the primary secret key.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgArmor` | Create ASCII armored output. | &#X2718; | true |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the subkey. | &#X2714; |  |
| `gpgKeyFile` | The output key file. | &#X2714; |  |

### Import Key

`gpgImportKey` - Imports a key to the keyring.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgKeyFile` | The input key file. | &#X2714; |  |

### Sign Message

`gpgSign` - Sign a message.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgArmor` | Create ASCII armored output. | &#X2718; | true |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key with signing capabilities. | &#X2714; |  |
| `gpgMessage` | The message to sign. | &#X2714; |  |
| `gpgSignatureFile` | The output signature file. | &#X2714; |  |

### Signed Artifacts

`gpgSignedArtifacts` - Packages all artifacts for publishing, signs them, and then maps the Artifact definition to the
generated file.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgArmor` | Create ASCII armored output. | &#X2718; | true |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key with signing capabilities. | &#X2714; |  |

Note that these should be scoped to the `gpgSigner` task.

## Examples

There are a bunch of examples in the [sbt tests](src/sbt-test).

## Travis CI

TODO.

## Credits

This plugin was generated from the [BotTech/sbt-autoplugin.g8][sbt-autoplugin] Giter8 template.

Special thanks to:
* [Docker] for providing the containerization.
* [GitHub] for hosting the git repository.
* [GnuPG] for providing a free implementation of the OpenPGP standard.
* [JFrog] for distributing the releases on Bintray.
* [Lightbend] for [Scala], [sbt] and distributing the plugin in the community sbt repository.
* [scalacenter] for [Scala] and indexing this project in the [Scaladex].
* [Travis CI] for running the build.
* All the other OSS contributors who made this project possible.

[Build Status]: https://travis-ci.org/BotTech/sbt-gpg.svg?branch=master
[Debian Wiki - Subkeys]: https://wiki.debian.org/Subkeys
[Docker]: https://www.docker.com
[Download]: https://api.bintray.com/packages/bottech/sbt-plugins/sbt-gpg/images/download.svg
[Github]: https://github.com
[GnuPG]: https://www.gnupg.org/index.html
[GnuPG Download]: https://www.gnupg.org/download/index.html
[JFrog]: https://jfrog.com
[Lightbend]: https://www.lightbend.com
[sbt]: https://www.scala-sbt.org
[sbt - Credentials]: https://www.scala-sbt.org/1.x/docs/Publishing.html#Credentials
[sbt - Inspecting the Build]: https://www.scala-sbt.org/1.x/docs/Howto-Inspect-the-Build.html
[sbt-autoplugin]: https://github.com/BotTech/sbt-autoplugin.g8
[Scala]: https://www.scala-lang.org
[scalacenter]: https://scala.epfl.ch
[Scaladex]: https://index.scala-lang.org
[Travis CI]: https://travis-ci.org

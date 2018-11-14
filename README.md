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
1. Generate a primary key (unless you already have one).
1. Add a new subkey for your project.
1. Export the subkey.
1. Encrypt the subkey and commit it to the project repository.
1. During the automated build, decrypt and then import the subkey.
1. Build the project artifacts and sign them.

For detailed instructions see [Travis CI](#travis-ci).

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
1. Pinentry.

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
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the primary key. | &#X2718; | default |
| `gpgSubkeyLength` | The length of the generated subkey in bits. | &#X2718; | 4096 |
| `gpgSubkeyType` | The OpenPGP algorithm number or name to use for the subkey. | &#X2718; | RSA |
| `gpgSubkeyUsage` | The list of subkey usages. | &#X2718; | sign |

Use `gpgListKeys` to find the key fingerprint.

The best practice is to not keep your primary private key on the key ring of the machine that you use but to instead use
subkeys. You can read a good introduction to this on [Debian Wiki - Subkeys].

### Export Key

`gpgExportKey` - Exports a key with the secret key.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgArmor` | Create ASCII armored output. | &#X2718; | true |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key. | &#X2714; |  |
| `gpgKeyFile` | The output key file. | &#X2714; |  |

### Export Subkey

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

### Change Key Passphrase

`gpgChangeKeyPassphrase` - Changes the passphrase of a key.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgKeyFile` | The key file. | &#X2714; |  |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key. | &#X2714; |  |

This cannot be used with a passphrase, it can only be used with Pinentry.

WARNING - this will overwrite the key file with the new key with the new passphrase.

### Change Subkey Passphrase

`gpgChangeSubkeyPassphrase` - Changes the passphrase of a subkey.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgKeyFile` | The key file. | &#X2714; |  |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key. | &#X2714; |  |

This cannot be used with a passphrase, it can only be used with Pinentry.

WARNING - this will overwrite the key file with the new key with the new passphrase.

### Trust Key

`gpgTrustKey` - Trusts a key.

| Setting | Description | Required | Default |
| ------- | ----------- | :------: | ------- |
| `gpgKeyFingerprint` | The SHA-1 fingerprint of the key. | &#X2714; |  |
| `gpgTrustLevel` | How far you trust the key. | &#X2714; | 5 |

This is commonly used after importing an exported key.

WARNING - Only use this common on your own keys that you completely trust.

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
| `gpgSignArtifacts` | Whether to sign artifacts. | &#X2718; | true |

Note that these should be scoped to the `gpgSigner` task.

## Examples

There are a bunch of examples in the [sbt tests](src/sbt-test).

### Travis CI

#### Generate a Primary Key

If you already have a primary key and you are following the best practices then you should mount the device that contains
the key now, then set `gpgHomeDir` to the GnuPG home directory on that device.

If you do not already have a primary key then you need to generate one.

```sbtshell
set gpgNameReal := "Your (Organization) Name"
set gpgNameEmail := "your@email.com"
gpgGenerateKey
```

Pinentry should appear and ask you to enter passphrase.

You should see the fingerprint of your new primary key.

```sbtshell
[info] Generated your new primary key: 84C263516A75C26F1ADD723FC148D2D9D807D63F
```

By default it will have also generated a new subkey with signing capabilities.
You should only use this key in your build and never use your primary key.

#### Add a Subkey

You may want to use another subkey if you have already used the default one and want to minimise the impact of a
compromised subkey.

```sbtshell
set gpgAddKey/gpgKeyFingerprint := "84C263516A75C26F1ADD723FC148D2D9D807D63F"
gpgAddKey
```

Where `gpgKeyFingerprint` is the fingerprint of your primary key.
If you do not know the fingerprint then read [Find the Fingerprint](#find-the-fingerprint).
Look for the row starting with `pub` which matches your primary key.
Underneath that is a row starting with `fpr`.
The fingerprint is the hexadecimal value in that row.

#### Find the Fingerprint

To find the fingerprint of a key then you need to find it in the key listings:
```sbtshell
gpgListKeys
```

Which will output something similar to:
```sbtshell
[info] pub:u:4096:1:C148D2D9D807D63F:1538989071:::u:::escaESCA:::+:::23::0:
[info] fpr:::::::::84C263516A75C26F1ADD723FC148D2D9D807D63F:
[info] grp:::::::::0DEC20F89D3657E3DD606B9EAEA3A96EE9ED162C:
[info] uid:u::::1538989071::A05C2A6F40589222BAA65BEF1C1507E9261D2AF0::BotTech <bottechnz@gmail.com>::::::::::0:
[info] sub:u:4096:1:674FFAE89237F93F:1538989071::::::s:::+:::23:
[info] fpr:::::::::8BD27F291CB15ABD0DEFA583674FFAE89237F93F:
[info] grp:::::::::F243E10E8ACABDF33113B24A2D420FFAC7C71125:
```

The fingerprint of a key is the hexadecimal value in the row starting with `fpr`.
In this example there are two fingerprints, one for the primary key `84C263516A75C26F1ADD723FC148D2D9D807D63F` and one
for the subkey `8BD27F291CB15ABD0DEFA583674FFAE89237F93F`.

#### Export the Key

Now that you have the fingerprint of primary key you can export it.

We have to export the primary key because we need the primary secret key in order to change the passphrase of the subkey
in the next step.

```sbtshell
set gpgExportKey/gpgKeyFingerprint := "84C263516A75C26F1ADD723FC148D2D9D807D63F"
show gpgExportKey
```

Pinentry will ask you for the passphrase.

This will show you the location of the key file. If you forgot to use the `show` command then you can get it afterwards
by using:
```sbtshell
show gpgKeyFile
```

#### Change the Subkey passphrase

The subkey that was exported in the previous step will have the same passphrase as the primary key.
This is not ideal because we need to commit this passphrase (encrypted of course) to the build and so that increases
the chances that it may get compromised. We need to change the passphrase so that in the worst case, only this subkey
is compromised.

Unfortunately GnuPG does not have a good way of doing this. We must import the key into a temporary home directory,
then change the passphrase and then export it back out again.

```sbtshell
set gpgChangeSubkeyPassphrase/gpgKeyFingerprint := "8BD27F291CB15ABD0DEFA583674FFAE89237F93F"
gpgChangeSubkeyPassphrase
```

Pinentry should appear initially for the current passphrase and then again for the new passphrase.

Remember to use the _subkey_ fingerprint here and not the primary key.

#### Encrypt the Subkey

Since we will commit the subkey to source code repository it is a good idea to also encrypt it just in case something
went wrong and the key was exported without a passphrase or included the primary secret key or perhaps your passphrase
was weak.

##### Travis GitHub Token

We will use the Travis CLI to encrypt all the secrets to be used in the build.

Go to GitHub and create a Personal access token with the following scopes:
* `user:email`
* `read:org`
* `repo_deployment`
* `repo:status`
* `write:repo_hook`

See [Travis CI for open source projects][Travis OSS] on what these scopes are used for.

Save the token somewhere safe as you will need it to login to the Travis CLI and if you forget it you will need to
generate a new one.

##### Encrypt the GPG Secret Key

Next encrypt the GPG secret key using the instructions on [encrypting files][Travis Encrypting Files].

Install the Travis CLI:
```bash
gem install travis
```

Login using the GitHub Token:
```bash
travis login -g YOUR_GITHUB_TOKEN
```

Encrypt the secret key:
```bash
travis encrypt-file target/.gnupg/key.asc
```

Add the output to the `before_deploy` section of the `.travis.yml` file. For example:
```yaml
before_deploy:
- openssl aes-256-cbc -K $encrypted_12345abcdef -iv $encrypted_12345abcdef -in travis/key.asc.enc -out travis/key.asc -d
```

Move the encrypted secret key:
```bash
mv key.asc.enc travis/
```

Delete the unencrypted secret key:
```bash
rm target/.gnupg/key.asc
```

Now encrypt the GPG passphrase using the instructions on [encryption keys][Travis Encryption Keys].
```bash
travis encrypt
PGP_PASS=YOUR_PGP_PASSPHRASE
```

Add the output to the `env.global` section of the `.travis.yml` file.

#### Add the Signed Artifacts

The last few steps are to configure the key and add the signed artifacts so that they are published.

First add the GPG passphrase from the environment variable set in the previous step:
```sbt
gpgPassphrase := Option(System.getenv("PGP_PASS"))
```

Then add the path to the secret key (the output of the `openssl` command above):
```sbt
gpgKeyFile := Some(file("travis") / "key.asc")
```

And lastly add the fingerprint of the subkey:
```sbt
gpgKeyFingerprint := "8BD27F291CB15ABD0DEFA583674FFAE89237F93F!"
```

NOTE - The fingerprint must end with a `!` in order to get GnuPG to use the subkey.

Now when you run `publish` the artifacts will be published along with their signatures.

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
[Travis OSS]: https://docs.travis-ci.com/user/github-oauth-scopes/#travis-ci-for-open-source-projects
[Travis Encrypting Files]: https://docs.travis-ci.com/user/encrypting-files
[Travis Encryption Keys]: https://docs.travis-ci.com/user/encryption-keys

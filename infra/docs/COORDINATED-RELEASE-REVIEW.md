# GoEvent coordinated production change review

Prepared after PR #73 merged. This document requests review, not authorization.
Production MySQL, application release policy and ingress remain unchanged.

## Verified source and artifacts

Current main is `6da0930ea9e8ba891cf7f27f020c8fec087810fb`.
[Trusted publication 44142529629](https://github.com/VinuraY/goevent/actions/runs/44142529629)
completed successfully. All four image reports contain zero HIGH/CRITICAL findings;
all eight scan/SBOM files matched the trusted evidence hashes. Cryptographic
backend/gateway/Caddy attestations verified the exact signer/source commit,
workflow and main ref, rejecting self-hosted builders. Reviewed images were
pulled into the VM cache without starting containers.

**Do not activate the backend/gateway digests below.** Later full Spring TLS
checks found that Boot's separate Flyway datasource drops the configured JDBC
truststore password, the Redis session property prefix is obsolete, and the
default gateway Redis health probe requires the intentionally denied INFO command.
PR #74 includes the configuration fixes. Merge the reviewed fixes and obtain new
trusted-main images before application release;
the older digests remain provenance evidence, not approved production candidates.

| Component | Digest in acrgoevent.azurecr.io/goevent/<component> |
| --- | --- |
| backend | sha256:8575cab20bfc3cd7e9eb201ffa474ec93467aebf3a22f451cec3abd732748207 |
| gateway | sha256:634ab1810619688cbf9486f2ffe0d082744bbe7499270650478edfe282b0165b |
| caddy | sha256:4772cb538cbd4e89bfdd2efcad02ce02b201150fb0617c2089d4f1aede9266c7 |

If main changes, obtain its new successful trusted publication and rebind the
manifest/migration approval. Never label these images with a newer commit.

## Completed compatibility and recovery checks

- The actual November 17 AES-256-GCM backup was hash-checked, authenticated and
  restored into a disposable local MySQL clone using the production MySQL digest.
  No plaintext SQL file was created. The clone started with zero application tables.
- All seven exact-main Flyway files migrated and validated over JDBC
  VERIFY_IDENTITY using only SELECT/INSERT/UPDATE/DELETE/CREATE/ALTER/INDEX/REFERENCES.
  Flyway clean/repair/baseline were not used in this acceptance check.
- Runtime SELECT/INSERT/UPDATE/DELETE succeeded; runtime DDL was rejected.
- Native MySQL verified TLS accepted mysql and rejected the IP hostname mismatch.
  Secure transport enforcement rejected plaintext while preserving root socket
  access. A verified-TLS mysqldump with the DML-only account succeeded.
- An additional negative test showed unescaped database grants also match
  goeventXdb when partial_revokes=OFF. The corrected escaped grant denied that
  database and retained successful migrations, DML and backup checks.
- The live app grant is already stored with the underscore escaped; no live
  grant was changed. Roles and table/column/routine grants for the app were absent.
- Private evidence is in
  `/root/goevent-mysql-review/review-20261117/combined-ready-01/mysql-restored-clone-acceptance.json`.
- Existing backup and both prior isolated restore volumes remain preserved.
  Recheck backup creation/verification age against 86,400 seconds before release.

## Bootstrap and application-configuration defects

The original helper sets umask 077, then creates service directories with mkdir
0750; the umask reduces them to 0700, which the helper subsequently rejects.
It now explicitly chmods only newly created component directories to 0750 after
ownership assignment. Existing directories, secret contents, approval checks,
non-overwriting publication and cleanup behavior are preserved.

The regression failed before the fix and passed afterward. All 42 infrastructure
tests, configuration/schema checks, owner-runbook shell syntax and diff whitespace
checks passed. No business code, dependency, schema file or image definition changed.

The full Spring startup check also reproduced a migration TLS failure in the
attested main artifact. The password binds correctly, but Boot configures a
SimpleDriverDataSource separately before applying Flyway JDBC properties; those
properties do not reach that datasource. FlywayConfig now merges the configured
JDBC properties into that existing migration datasource. It does not create or
replace the runtime datasource, change identities, put the password in a URL or
weaken TLS. The driver-level regression checks the truststore property, preserved
connection properties, unchanged URL and migration credentials. The targeted
backend test and candidate executable packaging passed. New-main publication is
required after review; the current main backend remains unsuitable for activation.

Boot 4 uses spring.session.data.redis. The old spring.session.redis namespace
was ignored, so session writes targeted spring:session keys outside the backend
ACL. The application default and production template now explicitly bind the
GoEvent namespace and the production template binds configure-action=none using
the supported prefix. A native Boot property-binding regression checks both.
ACL keyspace permissions remain unchanged; existing local default-prefix sessions
will require reauthentication. Production applications have not been started.

The default gateway Redis health contributor issues INFO, which the gateway ACL
does not permit. The replacement contributor only PINGs the configured authenticated
TLS connection, closes it, and returns DOWN on errors/empty/unexpected replies.
It does not disable a health check or grant additional Redis permissions. Both
health regressions and the full gateway suite passed (four tests total).

The final isolated stack check passed with the locally built PR backend/gateway
candidates and the production configuration templates. It restored the authenticated
backup, kept MySQL secure transport ON, used the restricted migration/runtime users,
and used the actual issued MySQL/Redis certificates and separate Redis ACL identities.
Backend/gateway startup, routed test/content reads, Redis-backed CSRF token creation,
explicit credentialed CORS and missing-token rejection passed. This is candidate
source validation, not attestation of a newly published main image. Production
requires a successful trusted build after the reviewed application fixes merge.

The fixed helper generated all 16 actual pinned-secret configtree files under
`/run/goevent-acceptance-6da0930`, separate from the active runtime location.
Files are root:10001 0640, component directories root:10001 0750, single-link
regular files. Actual MySQL/Redis certificate checks and PKCS12 generation passed.
The real Google audience matches the deployed frontend's public JavaScript.
The active bootstrap has not been installed/executed; both review flags are false.

The executable bundle used for this rehearsal is
`/opt/goevent/releases/review-combined-20261117`, archive SHA-256
`9bea32d78572df3599df9a655990a3421ff855748af51fbb7677c15a2bb44e4c`.
This snapshot predates the final documentation edits; reviewed source hashes
identify the bootstrap code. Do not infer bundle identity from a later Git HEAD.

## Vercel preview completed

Existing project astroid/goevent retains root frontend, Next.js defaults and main
as its production branch. Future builds now use:

- Install: `bunx bun@1.4.4 install --frozen-lockfile`.
- Build: `bunx bun@1.4.4 run build`.
- API_URL and BACKEND_URL: `https://api.goevent.kyrolabs.dev`.
- Existing NEXT_PUBLIC_GOOGLE_CLIENT_ID retained unchanged.

[Exact-main preview](https://goevent-bg5h9ky25-astroid.vercel.app) is READY;
[build inspection](https://vercel.com/astroid/goevent/HMhiyw49M7Kb7MPrhgMemHnUUGSV)
records source main/6da0930, Bun 1.4.4 and a successful frozen install/build.
An authenticated Vercel CLI request returned the GoEvent homepage with HTTP 200,
and its browser chunks contain the approved API origin. Deployment Protection
remains enabled; authenticated automation access is not proof of public access.
The existing production domain was not promoted to this preview. API functionality
is not accepted while production backend/ingress remain off.

Vercel's native project commands pin install/build without adding repository
dependencies. See [package-manager configuration](https://vercel.com/docs/package-managers).
Environment edits affect new deployments only; existing deployments retain their
previous build configuration.

## Inactive maintenance candidates

Root-only directory:
`/root/goevent-mysql-review/review-20261117/combined-ready-01`.

- mysql TLS candidate is the earlier `compose.candidate.json`: only two added
  read-only external mounts; current live root:root 0644 is preserved. Original
  generated datadir certificates and protected files are unchanged.
- Account SQL proposes goevent_migration@% with REQUIRE SSL and the tested
  escaped-schema migration grants. Runtime SQL restricts the existing app to DML
  and REQUIRE SSL. Credentials are private and pinned to Key Vault versions.
- Recorded-grant/TLS rollback SQL, original backup script and enforcement config
  are prepared. No SQL was executed against production.
- Backup-script candidate adds mysql hostname, VERIFY_IDENTITY and the reviewed
  CA; its production source is unchanged. Health/admin/backup acceptance must run
  before secure transport enforcement is enabled.
- Container metadata guard rejects forwarded traffic to 169.254.169.254 through
  DOCKER-USER. Its oneshot and release-unit dependency are inactive. A simulated
  command test proved idempotence/exact rule arguments; shellcheck passed.
- NSG candidates allow only TCP 80 and 443, preserving the SSH rule and keeping
  MySQL/Redis/backend/gateway ports private. No rule was applied.
- Production Caddy hostname configuration passed validation in the attested image
  with no network or published ports. Its persistent state directory and actual
  public certificate issuance remain activation steps.
- Bootstrap and operator policy drafts remain disabled; commit-bound migration
  approval is false. No live policy/current pointer/service unit was installed.
- The distribution GitHub CLI cannot verify attestations. Official CLI 2.104.0 was
  downloaded and verified against its published archive SHA-256
  98cb101ad87dbbd3f6b2f2bfcbd43197ccd07643140121d8a9cfaa91eadbc24e, then staged at
  /opt/goevent/tools/gh-2.104.0/bin/gh without replacing /usr/bin/gh. The release-unit
  candidate scopes PATH to that binary. All three current-main image attestations
  passed on the actual VM using temporary existing operator GitHub authentication
  and fresh Managed Identity/registry caches. No GitHub token file was installed;
  authorized release needs transient root-only operator authentication. Missing
  authentication fails verification rather than skipping it.

## Consolidated owner decision

The original task explicitly reserves these live operations for owner approval:

1. Install reviewed external certificates, replace the protected MySQL Compose
   with the two-mount candidate while preserving 0644, and restart the existing
   owning service; transport initially OFF.
2. Create the proposed migration account, restrict runtime grants after client
   compatibility passes, update the reviewed backup client, and enable/restart
   secure transport only after all affected clients and a fresh recovery check pass.
3. Apply the container IMDS guard and TCP 80/443 NSG changes; bootstrap real runtime
   secrets and start approved Redis/Caddy dependencies in the reviewed order.
4. Authorize the initial V1–V7 migration and digest-pinned backend/gateway release
   through the existing GitHub environment, Blob intent and VM pull procedure.
5. Review release-intent retention locking: its seven-day policy is currently
   Unlocked. Locking is irreversible and has not been performed.

Initial-schema migration is classified rollback_safe=false. Failure stops the
application pair and preserves the database/uploads; there is no automatic DDL
rollback or approved previous application revision. Recovery requires the reviewed
backup/restore procedure, not Flyway clean/repair or a database reset.

The literal promotion kill switch and production operator policy remain disabled.
Enabling promotion still requires a reviewed activation change. The production
environment prevents self-review and VinuraY is its reviewer. Therefore another
authorized repository collaborator must initiate promotion for VinuraY to approve;
the agent cannot fabricate or bypass that GitHub review using VinuraY's token.

After authorized activation, browser login/registration/logout, real Google login,
CORS/cookies/CSRF, upload persistence, public HTTPS, post-change backup, container
replacement and rollback/reboot acceptance still need execution. The Google
Console must authorize https://goevent.kyrolabs.dev; real-user login is not proven
by a matching client ID. Offline CA recovery custody remains an owner task.

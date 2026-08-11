#!/bin/sh
# Tiers 2 and 3: the shaded jar meets a real server, and a real client plays.
#
# Tier 1 (src/test, MockBukkit) proves the plugin's logic against a mock. It
# cannot prove the artifact starts: a green build, green CI and a green tier-1
# suite have all been observed over a jar that died in onEnable on Paper. This
# script puts a real Paper server between the build and the claim, and then puts
# a real client in front of the server.
#
# What it does, in order:
#   1. resolves a PINNED Paper build through fill.papermc.io/v3 and verifies the
#      published SHA-256 (the v2 API is sunset),
#   2. resolves a PINNED BKCommonLib build from its CI and verifies its SHA-256,
#   3. builds the in-server test plugin from it/ (a sibling Maven project, not a
#      module of the root pom) and installs it-client/'s pinned npm tree,
#   4. assembles a throwaway server under target/, boots it --nogui with stdin
#      closed, waits for the harness to say it is armed, and launches the bot,
#   5. waits for the test plugin to write a result file,
#   6. fails the run on a timeout, on any severe console line, on a client that
#      never logged in, on a failed scenario, on a scenario count that does not
#      match what was expected, or on a bot that exited non-zero.
#
# Run it the same way CI does:  sh ci/integration.sh
#
# THE SEQUENCING, because it is the one piece of engineering here that is not
# obvious. Tier 2's plugin shuts the server down when its scenarios end; a bot
# needs the server alive and needs to have connected first. So in client mode the
# plugin does not shut down after tier 2 - it arms the tier-3 step machine and
# prints a marker. This script waits for THAT LINE, not for an interval, before
# it starts the bot; the bot's last act is `/itstep finish`, which is what makes
# the plugin write its result and stop the server. Nothing sleeps and nothing
# guesses.
#
# Proving the gate can fail: set TS_IT_INDUCE to one of
#   assert   - a scenario asserts something false
#   severe   - every scenario passes but the plugin logs a severe line
#   missing  - a scenario is silently not run
#   hang     - the marker is never written, so the run must time out
#   noclient - the bot is never launched, so the tier-3 steps never run
# Each must turn the run red. A gate that has only ever been green has not been
# shown to work, so the hook is a permanent part of the harness rather than an
# edit someone makes and reverts.

set -eu

# ---------------------------------------------------------------------------
# Pins. Exactly one Paper version is wired and it is a variable with one value;
# the cross-version matrix is a separate piece of work and building it here
# would take scope from it.
# ---------------------------------------------------------------------------
#
# The three pin values and the JVM the SERVER runs on are overridable from the
# environment so that the same harness can be pointed at a second Paper without
# the pin being edited and forgotten in that state. CI sets none of them and
# therefore runs exactly the pinned combination below. Overriding one of the
# three means overriding all three: a version, a build and the checksum that
# build publishes belong together, and the download is still verified against
# whatever PAPER_SHA256 says. Every other dependency this script pins and
# downloads - BKCommonLib below - follows the same rule for the same reason;
# see its own comment for where its shape has to differ.
PAPER_PROJECT=${PAPER_PROJECT:-paper}
PAPER_VERSION=${PAPER_VERSION:-1.21.11}
PAPER_BUILD=${PAPER_BUILD:-132}
PAPER_SHA256=${PAPER_SHA256:-5ffef465eeeb5f2a3c23a24419d97c51afd7dbb4923ff42df9a3f58bba1ccfba}

# The JVM the server runs under, which is NOT the one this repository builds
# with: the plugin and the tests target 21, and a newer Paper may require a
# newer runtime. Only the server process is affected.
JAVA_BIN=${TS_IT_JAVA:-java}

# TradeShop cannot enable without BKCommonLib: Setting.<clinit> reaches
# com/bergerkiller/bukkit/common/config/JsonSerializer and dies with
# NoClassDefFoundError if it is absent. The Maven artifacts are NOT usable as
# plugins - they carry a plugin.yml but omit the shaded internals and die at
# load on .../softdependency/SoftServiceDependency. The CI build is the
# installable one, and it is pinned to a build number rather than
# lastSuccessfulBuild so that a run is reproducible.
#
# Overridable from the environment for the same reason as the Paper pins
# above: validating a fix against a different BKCommonLib build must not mean
# editing this file and risking the edit being committed in that state. The
# version is its own variable rather than folded into BKCL_BUILD because it is
# baked into the jar name (BKCommonLib-<version>-<build>.jar) - a build on a
# new version line (2.0.3-SNAPSHOT, say) cannot be named by changing the build
# number alone. BKCL_JAR is overridable on top of that, for the day the naming
# convention changes and the version/build derivation no longer holds it.
#
# As with Paper, the download is verified against whatever BKCL_SHA256 says,
# default or overridden: there is no separate "skip the check" path, so an
# override of the version or build without a matching override of the hash
# does not go quiet - it fails the checksum comparison below with both values
# in the message. Unlike Paper, there is no registry to cross-check the pin
# against before spending the download: fill.papermc.io's v3 API publishes a
# checksum per build that fetch_paper compares the pin to (see the disagree-
# with-the-registry die below); BKCommonLib's CI exposes no equivalent
# manifest, only the jar. So overriding BKCL_SHA256 correctly is on whoever
# does the override - the same trust boundary this script already applies to
# the Paper pin itself, which is compared to the registry but never replaced
# by it ("do not paper over it by trusting the registry").
BKCL_VERSION=${BKCL_VERSION:-2.0.2-SNAPSHOT}
BKCL_BUILD=${BKCL_BUILD:-2031}
BKCL_JAR=${BKCL_JAR:-BKCommonLib-${BKCL_VERSION}-${BKCL_BUILD}.jar}
BKCL_URL=https://ci.mg-dev.eu/job/BKCommonLib/${BKCL_BUILD}/artifact/build/${BKCL_JAR}
BKCL_SHA256=${BKCL_SHA256:-e7b15d76898834a0b7e8a080982a3f24c69b4a82e87a1e5ec29bce8d17045c46}

# A scenario that did not run is a failure, so the count is asserted here rather
# than read out of the report the run itself produced. It is two halves - the
# in-server scenarios registered by it/IntegrationPlugin.java, and eight driven by
# a real client from it/ClientPhase.java - and both declare themselves before they
# run, so the plugin records every step the client never reached as a failure by
# name rather than leaving the suite looking smaller. What each change to the
# total bought is the running log below; the totals in it are the record.
#
# W4 phase 1 landed and this suite is expected to be GREEN. The matrix rows that
# were written to fail - the useMeta gate, potions, one-sided firework effects,
# the save-and-reload - all assert the behaviour a shop owner is entitled to and
# now get it. The count went 28 -> 29 with the pre-component migration row, which
# loads a real 1.20.4-era shop file off disk.
#
# W4 phase 3 took it 29 -> 34 with five scenarios that tier 1 cannot hold: pale
# oak does not exist in MockBukkit's 1.21.1 material set, MockBukkit will not
# build a block state for a hanging sign of either mounting, and BKCommonLib's
# item registry cannot be reached without a real server under it. This server is
# 1.21.11 with BKCommonLib installed, so all five are real here.
#
# W9 IS ADDING ROWS THAT ARE WRITTEN TO FAIL. They are the defects a code review
# reported and nobody ever ran, and the branch carrying them is red on purpose:
# the tests land before the fixes so that each one is known to have failed first.
# A green run on this branch is the thing to distrust. See it/DefectRows.java,
# and it/IssueRows.java for the same pattern applied to reports off the tracker.
#
# 34 -> 36 with The per-item setting, the per-item setting an older config.yml is missing.
# 36 -> 37 with The chest linkage, the chest linkage that removeChest cannot remove.
# 37 -> 39 with The five-slot storage types, the two permitted storage types that hold five slots.
# 39 -> 41 with The double chest, the double chest whose Z branch compares an X against a Z,
#          and the X-axis control that is expected to stay green beside it.
# 41 -> 42 with The cost side, the cost side a shop loses the first time it is read off disk.
#          That one was not in the review: it was noticed as `cost: []` in a shop file
#          left behind by a run, characterised afterwards, and pinned here.
#
# 42 -> 44 with the two per-item comparison toggles a mock cannot answer honestly:
#          the shulker box's contents, which need a BlockStateMeta, and a toggle read
#          back off a real shop file. The other fourteen settings are switched off and
#          back on in the tier-1 matrix. See it/SettingToggleRows.java.
# 44 -> 45 with the client-driven half of the same thing: a comparison turned off by
#          clicking it in the edit GUI, and the trade that answer decides. That one is
#          a tier-3 STEP rather than an in-server scenario - it needs a hand to click
#          with - so it raises the client's count from seven to eight.
# 45 -> 46 with The removed chest, upstream #160's shop that has lost its storage block.
# 46 -> 49 with The editable sign, upstream #152: the five shop states a sign has to be
#          protected in, the trade and the ordinary sign that protecting it must not cost,
#          and the plain-Bukkit PlayerSignOpenEvent that refuses the editor - named, so
#          that a return to the Paper-only guard fails rather than passes quietly.
# 49 -> 52 with The sign side: a header written on the BACK of a sign, the player who has
#          to be told it did nothing, and the front-side control that must not move.
# 52 -> 54 with The broken sign: a shop stored against a sign whose text no longer reads as
#          one, which today outlives the only block that could have found it again, and the
#          control that the ordinary sign break - a shop's and a plain sign's - does not move.
# 54 -> 60 with The config and metrics rows: an operator's tuned settings surviving a boot
#          that writes the file, the shop counter that always answered zero, the chunk
#          data handed out twice, the double chest unlinked by halves, the status stored
#          before it was recomputed, and a missing per-item key read as a lock.
# 60 -> 62 with The shared shop, which cannot be loaded at all: a shop with a manager read
#          back off disk, and one with a member read out of the storage layer's own
#          in-memory copy by the chunk search. Both paths threw, and every shop this
#          harness had ever built was owned by one player and shared with nobody - so the
#          save-and-reload row at site 10 now carries a manager as well, which is where
#          this class of defect stops being invisible.
# 62 -> 65 with allow-sign-break: the shop the setting leaves behind when its sign goes, the
#          same thing for the owner's own break and an admin's, and the control that with the
#          setting OFF the refusal, the owner's break and the admin's are the ones that shipped.
EXPECTED_SCENARIOS=65

# Tier 3. The client is not optional: a run that boots a server, plays nothing
# and exits 0 is the vacuous pass this project treats as the worst possible
# output, so an absent Node toolchain is a failed run and never a skipped tier.
#
# How long the server waits for the client before recording every unreached step
# as a failure. Generous, because login, chunk load and window open are all
# timing on a cold runner. TS_IT_CLIENT_DEADLINE shortens it when deliberately
# proving the noclient path; nothing in CI sets it.
CLIENT_DEADLINE=${TS_IT_CLIENT_DEADLINE:-180000}

# Boot to "Done" is ~10s bare and ~24s with BKCommonLib on the reference
# machine. The timeout is generous because a timeout is a failure and never a
# skip: a cold CI runner that is merely slow must not be reported as a bug.
# TS_IT_TIMEOUT is for shortening it when deliberately testing the timeout path;
# nothing in CI sets it.
RUN_TIMEOUT=${TS_IT_TIMEOUT:-300}

# online-mode=false is required for a headless harness to drive the server at
# all, which makes it a security property rather than a convenience. The server
# is therefore bound to loopback on a high port and lives for seconds. A copy of
# this script run against a public interface is an unauthenticated server.
BIND_ADDRESS=127.0.0.1

# The port is chosen per run rather than fixed, because a fixed one is a shared
# resource nobody declared. Two runs on one machine - two worktrees, two agents,
# or a run started before the last one had let go - raced for 25599, and the
# loser died with Paper's "**** FAILED TO BIND TO PORT!" buried in a console
# this script summarised as "the server never finished starting". That reads as
# a broken build. It was a busy machine.
#
# The window below is searched from an offset that is this checkout's own path
# hashed, so one worktree gets the same port on every run - reproducible in a
# log, and reachable by hand - while two worktrees begin their search in
# different places and in practice never meet. The first port in the window
# nothing is listening on wins; if every one of them is taken the run fails
# saying exactly that, which is a machine that is too busy and not a server that
# is broken. TS_IT_PORT pins a port instead and skips the search, for a run that
# has to be reached at an address agreed in advance.
PORT_BASE=25599
PORT_SPAN=100

ROOT=$(cd "$(dirname "$0")/.." && pwd)
CACHE=$ROOT/target/it-cache
SERVER=$ROOT/target/it-server
CONSOLE=$SERVER/console.log
MARKER=$SERVER/it-result.txt
PAPER_JAR=$SERVER/paper.jar
CLIENT_DIR=$ROOT/it-client
CLIENT_LOG=$SERVER/client.log

say() { printf '[integration] %s\n' "$*"; }
die() { printf '[integration] FAIL: %s\n' "$*" >&2; exit 1; }

need() {
    command -v "$1" >/dev/null 2>&1 || die "$1 is required and is not on PATH"
}

# sha256 of $1, or empty if the file is not there.
sum_of() {
    [ -f "$1" ] || return 0
    sha256sum "$1" | cut -d' ' -f1
}

# ---------------------------------------------------------------------------
# 1. Paper, pinned and checksummed.
# ---------------------------------------------------------------------------
fetch_paper() {
    cached=$CACHE/${PAPER_PROJECT}-${PAPER_VERSION}-${PAPER_BUILD}.jar

    if [ "$(sum_of "$cached")" = "$PAPER_SHA256" ]; then
        say "Paper ${PAPER_VERSION} build ${PAPER_BUILD} already cached, checksum matches"
        return 0
    fi

    api=https://fill.papermc.io/v3/projects/${PAPER_PROJECT}/versions/${PAPER_VERSION}/builds/${PAPER_BUILD}
    say "resolving ${api}"

    meta=$CACHE/paper-build.json
    curl -fsSL -H 'User-Agent: TradeShop-integration/1 (+https://github.com/DevOfPie/TradeShop)' \
        -o "$meta" "$api" \
        || die "could not reach the Paper API for ${PAPER_PROJECT} ${PAPER_VERSION} build ${PAPER_BUILD}.
       If this build was purged, the pin at the top of this script is stale -
       that is a purge, not your mistake. Pick a current build, update
       PAPER_BUILD and PAPER_SHA256 together, and say so in the commit."

    url=$(jq -r '.downloads["server:default"].url' "$meta")
    published=$(jq -r '.downloads["server:default"].checksums.sha256' "$meta")

    [ -n "$url" ] && [ "$url" != null ] || die "the Paper API returned no download URL for build ${PAPER_BUILD}"
    [ "$published" = "$PAPER_SHA256" ] || die "Paper build ${PAPER_BUILD} publishes sha256 ${published}, this script pins ${PAPER_SHA256}.
       The pin and the registry disagree; do not paper over it by trusting the registry."

    say "downloading ${url}"
    curl -fsSL -o "$cached.part" "$url" || die "download of Paper build ${PAPER_BUILD} failed"
    mv "$cached.part" "$cached"

    got=$(sum_of "$cached")
    [ "$got" = "$PAPER_SHA256" ] || die "downloaded Paper jar hashes ${got}, expected ${PAPER_SHA256}"
    say "Paper ${PAPER_VERSION} build ${PAPER_BUILD} verified"
}

# ---------------------------------------------------------------------------
# 2. BKCommonLib, pinned and checksummed.
# ---------------------------------------------------------------------------
fetch_bkcommonlib() {
    cached=$CACHE/$BKCL_JAR

    if [ "$(sum_of "$cached")" = "$BKCL_SHA256" ]; then
        say "BKCommonLib build ${BKCL_BUILD} already cached, checksum matches"
        return 0
    fi

    say "downloading ${BKCL_URL}"
    curl -fsSL -o "$cached.part" "$BKCL_URL" \
        || die "could not fetch BKCommonLib build ${BKCL_BUILD} (${BKCL_JAR}).
       CI keeps a limited number of builds; if this one has been rotated out,
       the pin is stale rather than wrong. Update BKCL_VERSION, BKCL_BUILD and
       BKCL_SHA256 together."
    mv "$cached.part" "$cached"

    got=$(sum_of "$cached")
    [ "$got" = "$BKCL_SHA256" ] || die "downloaded BKCommonLib jar hashes ${got}, expected ${BKCL_SHA256}.
       If BKCL_VERSION or BKCL_BUILD was overridden without also overriding
       BKCL_SHA256, that mismatch is why - a version, a build and the checksum
       it publishes belong together."
    say "BKCommonLib build ${BKCL_BUILD} verified"
}

# ---------------------------------------------------------------------------
# 3. The plugin under test, and the plugin that tests it.
# ---------------------------------------------------------------------------
find_tradeshop_jar() {
    # The shade plugin writes straight into target/server/plugins/.
    TRADESHOP_JAR=$(ls "$ROOT"/target/server/plugins/*.jar 2>/dev/null | head -n 1 || true)
    [ -n "$TRADESHOP_JAR" ] || die "no shaded TradeShop jar in target/server/plugins/. Run 'mvn -B package' first, or 'sh ci/build.sh'."
}

build_test_plugin() {
    tradeshop_jar=$1

    # it/ compiles against the plugin it is testing, but the plugin is not
    # published anywhere a build can resolve it from, and adding an install
    # step to the root pom would be a line in upstream's file. Installing the
    # freshly shaded jar under a fixed local coordinate keeps the version out
    # of it/pom.xml entirely: whatever was just built is what it/ compiles
    # against.
    say "installing $(basename "$tradeshop_jar") as org.shanerx:tradeshop:it-local"
    mvn -B -q org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file \
        -Dfile="$tradeshop_jar" \
        -DgroupId=org.shanerx -DartifactId=tradeshop -Dversion=it-local -Dpackaging=jar \
        || die "could not install the shaded jar into the local repository"

    say "building the in-server test plugin (mvn -f it/pom.xml package)"
    mvn -B -q -f "$ROOT/it/pom.xml" clean package \
        || die "the in-server test plugin did not build"

    IT_JAR=$(ls "$ROOT"/it/target/tradeshop-integration-*.jar 2>/dev/null | head -n 1 || true)
    [ -n "$IT_JAR" ] || die "it/ built but produced no jar"
}

# Every JavaScript dependency comes from the lockfile, never from a range: this
# tier is the flakiest one in the project and "it worked last week" must not be a
# possible explanation. `npm ci` refuses to run at all if package.json and
# package-lock.json disagree, which is the property being bought here.
install_client() {
    [ -f "$CLIENT_DIR/package-lock.json" ] \
        || die "it-client/package-lock.json is missing, so the client's dependencies are not pinned"

    say "installing the client's pinned dependencies (npm ci)"
    ( cd "$CLIENT_DIR" && npm ci --no-audit --no-fund >/dev/null ) \
        || die "npm ci failed in it-client/. If package.json and package-lock.json disagree, that is
       what npm ci is refusing to guess about - update the lockfile in the same
       commit as the dependency."
}

# ---------------------------------------------------------------------------
# 4. A throwaway server.
# ---------------------------------------------------------------------------

# Sets BIND_PORT to a port in the window that nothing is listening on.
#
# The probe binds the address the server is about to bind, using node - already
# required, because tier 3 is not optional - so that "free" is the kernel's
# answer rather than this script's guess. One node process walks the whole
# window; a process per candidate would be a hundred forks to learn the same
# thing.
#
# This runs as late as it can, immediately before the server is assembled and
# booted, because a port proved free and then taken by someone else is the one
# hole the approach cannot close: nothing can hold a port on behalf of a process
# that does not exist yet. Keeping that gap to seconds makes it a rare loss
# instead of a likely one, and check_console names it when it does happen.
pick_port() {
    if [ -n "${TS_IT_PORT:-}" ]; then
        BIND_PORT=$TS_IT_PORT
        say "TS_IT_PORT pins this run to ${BIND_ADDRESS}:${BIND_PORT}; the window is not searched"
        return 0
    fi

    # Four hex digits of the checkout's path: enough spread that neighbouring
    # worktrees do not start on the same offset, and the same answer every run.
    offset=$(printf '%s' "$ROOT" | sha256sum | cut -c1-4)
    offset=$(( 0x$offset % PORT_SPAN ))

    BIND_PORT=$(
        PORT_BASE=$PORT_BASE PORT_SPAN=$PORT_SPAN PORT_OFFSET=$offset PORT_HOST=$BIND_ADDRESS \
        node -e '
const net = require("net");
const base = Number(process.env.PORT_BASE);
const span = Number(process.env.PORT_SPAN);
const start = Number(process.env.PORT_OFFSET);
const host = process.env.PORT_HOST;
let tried = 0;
const next = () => {
    if (tried >= span) process.exit(1);
    const port = base + ((start + tried++) % span);
    const probe = net.createServer();
    probe.once("error", next);
    probe.listen(port, host, () => probe.close(() => {
        process.stdout.write(String(port));
        process.exit(0);
    }));
};
next();
'
    ) || BIND_PORT=

    [ -n "$BIND_PORT" ] || die "every port from ${BIND_ADDRESS}:${PORT_BASE} to \
${BIND_ADDRESS}:$(( PORT_BASE + PORT_SPAN - 1 )) is in use, so this run has nowhere
       to listen - all $PORT_SPAN were tried. Nothing is wrong with the plugin,
       the server or this script: the machine is holding the entire window. Look
       for harness servers that outlived their runs (java ... paper.jar --nogui,
       under some checkout's target/it-server) and stop them, or set TS_IT_PORT
       to a port outside the window."

    say "this run has ${BIND_ADDRESS}:${BIND_PORT} (window ${PORT_BASE}-$(( PORT_BASE + PORT_SPAN - 1 )), \
offset ${offset} from this checkout's path)"
}

assemble_server() {
    tradeshop_jar=$1
    it_jar=$2

    # Everything the server can regenerate is wiped so that no run inherits a
    # shop, a chest linkage or a data file from the last one. versions/,
    # libraries/ and cache/ are Paper's own unpacking of the jar and are kept:
    # they are reproducible from the checksummed jar and re-downloading them
    # every run is minutes of nothing.
    rm -rf "$SERVER/plugins" "$SERVER/world" "$SERVER/world_nether" "$SERVER/world_the_end" \
           "$SERVER/logs" "$CONSOLE" "$MARKER" "$CLIENT_LOG" "$SERVER/ops.json" "$SERVER/usercache.json"
    mkdir -p "$SERVER/plugins"

    cp "$CACHE/${PAPER_PROJECT}-${PAPER_VERSION}-${PAPER_BUILD}.jar" "$PAPER_JAR"
    cp "$CACHE/$BKCL_JAR" "$SERVER/plugins/"
    cp "$tradeshop_jar" "$SERVER/plugins/"
    cp "$it_jar" "$SERVER/plugins/"

    # The Mojang EULA is accepted for these automated runs by the owner of this
    # repository, in the record, once. The file is still written on every boot -
    # what changed is that writing it is no longer an assumption.
    printf 'eula=true\n' > "$SERVER/eula.txt"

    cat > "$SERVER/server.properties" <<PROPS
server-ip=$BIND_ADDRESS
server-port=$BIND_PORT
online-mode=false
enable-status=false
enable-query=false
enable-rcon=false
white-list=false
level-type=minecraft:flat
# A superflat with its layers spelled out. The default empty generator-settings
# makes vanilla log "No key layers in MapLike[{}]" at ERROR on every world it
# generates, and this harness treats an ERROR line as a failed run - correctly,
# so the world has to be described rather than left to a default that complains.
generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}
generate-structures=false
spawn-monsters=false
spawn-animals=false
spawn-npcs=false
view-distance=3
simulation-distance=3
max-players=4
spawn-protection=0
motd=TradeShop integration harness
PROPS
}

boot_and_wait() {
    say "booting Paper ${PAPER_VERSION} build ${PAPER_BUILD} on ${BIND_ADDRESS}:${BIND_PORT}"
    say "server JVM: $("$JAVA_BIN" -version 2>&1 | head -1)"
    started=$(date +%s)

    # stdin from /dev/null: a --nogui server whose stdin is a closed terminal
    # spins reading EOF. The test plugin shuts the server down itself once it
    # has written its result, so nothing needs to type "stop".
    (
        cd "$SERVER" &&
        exec "$JAVA_BIN" -Xms512M -Xmx1G -XX:+UseG1GC \
            ${TS_IT_INDUCE:+-Dtradeshop.it.induce=$TS_IT_INDUCE} \
            -Dtradeshop.it.marker="$MARKER" \
            -Dtradeshop.it.client=true \
            -Dtradeshop.it.clientdeadline="$CLIENT_DEADLINE" \
            -jar paper.jar --nogui
    ) < /dev/null > "$CONSOLE" 2>&1 &
    server_pid=$!

    client_started=no
    waited=0
    while [ "$waited" -lt "$RUN_TIMEOUT" ]; do
        if ! kill -0 "$server_pid" 2>/dev/null; then
            wait "$server_pid" 2>/dev/null || true
            elapsed=$(( $(date +%s) - started ))
            say "server exited after ${elapsed}s"
            return 0
        fi

        # The bot starts when the harness says it is armed, and not a moment
        # before: the tier-3 step machine is wired at enable but only accepts
        # steps once tier 2 has finished, so a bot that connected early would be
        # told so and fail. Waiting on the marker is waiting on a state the
        # server published, which is the difference between this and a sleep.
        if [ "$client_started" = no ] && grep -q 'tier 3 armed, waiting for a client' "$CONSOLE" 2>/dev/null; then
            client_started=yes
            launch_client
        fi

        sleep 1
        waited=$((waited + 1))
    done

    say "no result after ${RUN_TIMEOUT}s, killing the server"
    kill "$server_pid" 2>/dev/null || true
    sleep 5
    kill -9 "$server_pid" 2>/dev/null || true
    # Backstop for a JVM that outlived its shell. The bracket keeps the pattern
    # from matching the shell that is running it, and the path keeps it from
    # matching anybody else's server.
    pkill -f "[i]t-server/paper.jar" 2>/dev/null || true

    die "the server did not finish within ${RUN_TIMEOUT}s. A timeout is a failure, never a skip - see $CONSOLE"
}

# The bot runs alongside the server rather than blocking this loop, so that a bot
# which wedges is still caught by RUN_TIMEOUT above rather than by nothing.
launch_client() {
    if [ "${TS_IT_INDUCE:-}" = noclient ]; then
        say "INDUCED noclient: not launching the bot. Every tier-3 step must now fail by name."
        return 0
    fi
    if [ "${TS_IT_INDUCE:-}" = hang ]; then
        # In this mode the plugin deliberately never arms the client phase, so
        # this branch is unreachable; it is here so that reading the script does
        # not leave the question open.
        return 0
    fi

    say "the harness is armed; launching the client"
    (
        cd "$CLIENT_DIR" &&
        TS_IT_HOST=$BIND_ADDRESS TS_IT_PORT=$BIND_PORT TS_IT_VERSION=$PAPER_VERSION \
        exec node play.js
    ) < /dev/null > "$CLIENT_LOG" 2>&1 &
    client_pid=$!
}

# ---------------------------------------------------------------------------
# 5. Judgement. Every one of these is a way a run can look green and be wrong.
# ---------------------------------------------------------------------------
check_console() {
    [ -f "$CONSOLE" ] || die "the server produced no console output at all"

    # Asked before "Done (" below, and that order is the whole point. A server
    # that could not bind never prints "Done (" either, so the generic message
    # got there first and reported a busy machine as a server that would not
    # start - which is how one run was read as a broken build. pick_port makes
    # this rare; naming it makes it cheap on the occasions it still happens.
    if grep -q 'FAILED TO BIND TO PORT' "$CONSOLE"; then
        die "something else took ${BIND_ADDRESS}:${BIND_PORT} between this script proving it free and
       the server binding it, so the server stopped instead of starting. This is
       not a fault in the plugin, the server or the harness: it is two runs on
       one machine landing on the same port within the same few seconds. Run it
       again, or set TS_IT_PORT to pin a port nothing else will pick. Console:
$(tail -n 20 "$CONSOLE")"
    fi

    grep -q 'Done (' "$CONSOLE" \
        || die "the server never finished starting - it printed no 'Done (' line. Console:
$(tail -n 40 "$CONSOLE")"

    # A severe line fails the run even when every assertion passed. onEnable
    # throwing while the server carries on is exactly the shape of failure a
    # harness that only checks its own assertions calls green.
    #
    # The harness's own "FAIL <scenario>: <why>" lines are excluded, and only
    # those. A scenario is allowed to fail - several are written to - and its
    # message routinely names the exception it caught, which the pattern above
    # would otherwise read as the server being broken. Nothing is hidden by this:
    # every one of those lines is in the result file too, and check_results below
    # fails the run on it by name.
    severe=$(grep -nE '\[[0-9:]+ (ERROR|SEVERE|FATAL)\]|Exception|Error occurred while enabling|Could not load .plugin' "$CONSOLE" \
        | grep -v '\[TradeShopIT\] FAIL ' || true)
    [ -z "$severe" ] || die "the console carries severe lines:
$severe"

    # A client that never logged in is the vacuous pass this tier exists to
    # close, and it is asserted against the SERVER's console rather than against
    # anything the bot said about itself. The server is the only party that knows
    # whether a session was accepted.
    grep -q 'logged in with entity id' "$CONSOLE" \
        || die "no client ever logged in to this server, so nothing at tier 3 was exercised.
       A server that boots, plays nothing and stops cleanly is the failure this
       check exists for. Client log:
$(tail -n 30 "$CLIENT_LOG" 2>/dev/null || echo '(the client wrote no log at all)')"
}

# The bot's own verdict, read last and on purpose: it says whether the actions
# could be performed, while the result file says whether they had the right
# effect. Reporting the effect first means a red run names the behaviour rather
# than the plumbing.
check_client() {
    if [ "${TS_IT_INDUCE:-}" = noclient ]; then
        return 0
    fi
    [ -n "${client_pid:-}" ] || die "the harness armed tier 3 and no bot was ever launched"

    # Guarded, not bare: under `set -e` a non-zero `wait` exits this script on the
    # spot, which would fail the run with no message at all - the one outcome
    # worse than a wrong message.
    client_status=0
    wait "$client_pid" 2>/dev/null || client_status=$?
    [ "$client_status" = 0 ] || die "the client exited ${client_status}. It reports only what it did;
       the scenarios above report what happened. Client log:
$(tail -n 40 "$CLIENT_LOG")"

    say "client finished clean:"
    grep -E 'negotiated version|site:|placed |typing the sign|clicking |-->' "$CLIENT_LOG" \
        | sed 's/^/[integration]   /' || true
}

check_results() {
    [ -f "$MARKER" ] || die "the test plugin wrote no result file. The server booted and stopped without running anything, which is the failure this check exists for. Console:
$(tail -n 40 "$CONSOLE")"

    ran=$(grep -c '^SCENARIO ' "$MARKER" || true)
    declared=$(awk '/^SCENARIOS /{print $2}' "$MARKER")

    [ -n "$declared" ] || die "the result file has no SCENARIOS line, so the run cannot be shown to have finished:
$(cat "$MARKER")"
    [ "$ran" = "$declared" ] || die "the result file declares ${declared} scenarios but records ${ran}"
    [ "$ran" = "$EXPECTED_SCENARIOS" ] || die "expected ${EXPECTED_SCENARIOS} scenarios, ${ran} ran. A scenario that did not run is a failure, not a smaller suite:
$(cat "$MARKER")"

    failures=$(grep '^SCENARIO .* FAIL' "$MARKER" || true)
    # The stacks come out of the console rather than the result file: a result line
    # is counted and asserted against EXPECTED_SCENARIOS, so it has to stay one
    # line, and one line is not enough to find a fault by. This block is why - a
    # ClassCastException raised inside the plugin used to reach CI as its message
    # and nothing else, which named no file and no line and could not be worked
    # from at all. IntegrationPlugin.recordStack writes one console record per
    # frame, each carrying the "FAIL " prefix that check_console excludes.
    [ -z "$failures" ] || die "scenarios failed:
$failures

and the stacks they were thrown from:
$(grep '\[TradeShopIT\] FAIL ' "$CONSOLE" 2>/dev/null || echo '(the console recorded none)')"

    say "$ran/$EXPECTED_SCENARIOS scenarios passed:"
    sed 's/^/[integration]   /' "$MARKER"
}

# ---------------------------------------------------------------------------
main() {
    need curl
    need jq
    need sha256sum
    need java
    need mvn
    # Tier 3 is not optional, so neither is its toolchain. A missing Node is a
    # failed run: skipping the tier would turn a gate into a green light.
    need node
    need npm

    mkdir -p "$CACHE" "$SERVER"

    fetch_paper
    fetch_bkcommonlib

    find_tradeshop_jar
    say "plugin under test: $(basename "$TRADESHOP_JAR")"
    build_test_plugin "$TRADESHOP_JAR"
    install_client

    pick_port
    assemble_server "$TRADESHOP_JAR" "$IT_JAR"

    run_started=$(date +%s)
    boot_and_wait
    check_console
    check_results
    check_client

    say "integration run green in $(( $(date +%s) - run_started ))s on Paper ${PAPER_VERSION} build ${PAPER_BUILD}, client $(node --version)"
}

main "$@"

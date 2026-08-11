#!/bin/sh
# What CI does.
#
# The workflow that calls this holds only what CI *is* — triggers, runner, JDK,
# action versions — because changing that file needs the owner. Everything about
# what actually runs lives here, and here is writable by whoever is building
# this repository. Adding a check is an edit to this file that reaches the next
# push. See ci/README.md for why the split exists.
#
# Run it locally the same way CI does: sh ci/build.sh
#
# WHILE THE ITEM-METADATA SUITE IS RED - which is the state W4 phase 0 leaves the
# branch in, deliberately - this script stops at tier 1 and tiers 2 and 3 never
# run. That is `set -e` doing its job and is not worked around here: a build step
# that carries on past a failing one is how a red suite becomes a green build.
# To exercise tiers 2 and 3 while tier 1 is still red:
#
#     mvn -B clean package -DskipTests && sh ci/integration.sh
#
# and read the result file it prints. Once phase 1 lands, this script is the
# whole gate again.

set -eu

# -B is batch mode: no ANSI, no interactive prompts, and a log a human can read
# after the fact rather than a progress spinner.
mvn -B clean package

# Tier 2: boot the jar that was just shaded on a real Paper server and assert
# from inside it. The build above cannot tell whether the artifact starts - a
# green build, green CI and a green tier-1 suite have all been observed over a
# jar that died in onEnable. See ci/integration.sh.
sh ci/integration.sh

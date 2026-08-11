#!/bin/sh
# Reports which proposed workflows are still pending, with a diff against the
# live file.
#
# Deliberately NOT a gate. A pending proposal is a normal state, and failing a
# build on one would turn every proposal into a red build for a change nobody
# has agreed to yet. It always exits 0.
#
#   sh ci/workflow-proposals.sh

set -eu

# Compares ignoring trailing newlines. Applying a proposal is a `cp`, but a
# proposal copied through an editor loses or gains a final newline, and a report
# that cries "pending" over one byte is a report people stop reading. Note this
# ignores trailing newlines plural, which is slightly more lenient than the one
# byte it is there for — everything else counts, whitespace included.
body() {
    printf '%s' "$(cat "$1")"
}

found=0

for proposed in ci/proposed/*.yml ci/proposed/*.yaml; do
    [ -e "$proposed" ] || continue
    found=1
    name=$(basename "$proposed")
    live=".github/workflows/$name"

    if [ ! -f "$live" ]; then
        printf 'pending  %s -> %s (no live file yet)\n' "$proposed" "$live"
        printf '  the whole file is the change:\n'
        sed 's/^/    /' "$proposed"
        printf '\n'
    elif [ "$(body "$proposed")" = "$(body "$live")" ]; then
        printf 'applied  %s matches %s\n' "$proposed" "$live"
        printf '  delete the proposal: a matched proposal left in place is a\n'
        printf '  second copy of the workflow, free to drift from the one that runs.\n\n'
    else
        printf 'pending  %s differs from %s\n' "$proposed" "$live"
        diff -u "$live" "$proposed" | sed 's/^/    /' || true
        printf '\n'
    fi
done

if [ "$found" -eq 0 ]; then
    printf 'no proposals waiting\n'
fi

exit 0

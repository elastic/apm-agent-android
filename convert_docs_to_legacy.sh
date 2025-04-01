#!/usr/bin/env bash

ls docs/reference | egrep -o "[^.]+\.md" | sed -n "s/.md//p" | xargs -I '{}' pandoc -f gfm -t asciidoc docs/reference/{}.md -o legacy_docs/{}.asciidoc
ls legacy_docs | grep .asciidoc | xargs -I '{}' sed -i '' "s,.md,.asciidoc," legacy_docs/{}
_default:
    @just --list

validate:
    scripts/validate

validate-build:
    scripts/validate --build

doc-refs:
    scripts/check-doc-refs

source-map:
    scripts/generate-source-map docs/generated/source-map.md

build-debug:
    scripts/build-debug

review-diff:
    jj diff --tool difft

jj-status:
    jj status

jj-diff:
    jj diff

jj-ops:
    jj op log

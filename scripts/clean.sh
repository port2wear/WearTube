#!/usr/bin/env bash
set -euo pipefail

# WearTube project cleanup utility
# - Prunes build outputs, IDE caches, and transient artifacts in this repo
# - Optional: purge local Gradle cache (heavy) with --all
# Usage:
#   scripts/clean.sh            # Interactive
#   scripts/clean.sh --yes      # Non-interactive
#   scripts/clean.sh --all      # Also clears ~/.gradle/caches (asks unless --yes)
#   scripts/clean.sh --yes --all

confirm=yes
purge_gradle_cache=no
for arg in "$@"; do
  case "$arg" in
    --yes|-y) confirm=yes ;;
    --all|-a) purge_gradle_cache=yes ;;
    *) echo "Unknown option: $arg" >&2; exit 1 ;;
  esac
done

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
cd "$ROOT_DIR"

say() { printf "\033[36m[clean]\033[0m %s\n" "$*"; }
rm_rf() { [ -e "$1" ] && { rm -rf "$1" && say "removed $1"; } || true; }

say "Project root: $ROOT_DIR"

TARGETS=(
  "app/build"
  "build"
  ".gradle"
  ".kotlin"
  ".idea/caches"
  ".idea/libraries"
  ".idea/modules.xml"
  ".idea/workspace.xml"
  ".idea/navEditor.xml"
  ".idea/assetWizardSettings.xml"
  "**/.DS_Store"
  "**/captures"
  ".captures"
  ".externalNativeBuild"
  ".cxx"
)

if [[ "$confirm" != "yes" ]]; then
  read -r -p "This will remove build outputs and IDE caches in this repo. Proceed? [y/N] " ans
  [[ "$ans" =~ ^[Yy]$ ]] || { say "aborted"; exit 0; }
fi

say "Running Gradle clean (best-effort)"
if [[ -x "./gradlew" ]]; then
  ./gradlew --no-daemon clean | sed 's/^/[gradle] /' || true
fi

say "Pruning repo artifacts"
for t in "${TARGETS[@]}"; do
  # glob-aware delete
  if compgen -G "$t" > /dev/null; then
    for p in $t; do rm_rf "$p"; done
  else
    rm_rf "$t"
  fi
done

if [[ "$purge_gradle_cache" == "yes" ]]; then
  if [[ "$confirm" != "yes" ]]; then
    read -r -p "Also delete local Gradle cache at ~/.gradle/caches? This is large. [y/N] " ans
    [[ "$ans" =~ ^[Yy]$ ]] || purge_gradle_cache=no
  fi
  if [[ "$purge_gradle_cache" == "yes" ]]; then
    rm_rf "$HOME/.gradle/caches"
  fi
fi

say "Done. You can re-sync the project in Android Studio."


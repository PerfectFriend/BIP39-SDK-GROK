#!/usr/bin/env bash
# Push full BIP39-SDK-GROK tree to GitHub (requires authenticated gh + git).
# Usage:
#   ./scripts/push-to-github.sh
#   ./scripts/push-to-github.sh --private
#   GITHUB_OWNER=MyOrg ./scripts/push-to-github.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REPO_NAME="${REPO_NAME:-BIP39-SDK-GROK}"
VISIBILITY="--public"
BRANCH="${BRANCH:-main}"
COMMIT_MSG="${COMMIT_MSG:-BIP39-SDK-GROK: Android + iOS + Rust core}"

for arg in "$@"; do
  case "$arg" in
    --private) VISIBILITY="--private" ;;
    --public)  VISIBILITY="--public" ;;
    *) echo "Unknown arg: $arg" >&2; exit 1 ;;
  esac
done

if ! command -v gh >/dev/null; then
  echo "ERROR: GitHub CLI (gh) not found. Install: https://cli.github.com/" >&2
  exit 1
fi
if ! command -v git >/dev/null; then
  echo "ERROR: git not found" >&2
  exit 1
fi

# Auth check
if ! gh auth status >/dev/null 2>&1; then
  echo "ERROR: gh is not logged in. Run: gh auth login" >&2
  exit 1
fi

OWNER="${GITHUB_OWNER:-$(gh api user --jq .login)}"
FULL="${OWNER}/${REPO_NAME}"
echo "==> Target: https://github.com/${FULL}"
echo "    visibility: ${VISIBILITY#--}"

# Ensure .gitignore is sane
if [[ ! -f .gitignore ]]; then
  cat > .gitignore << 'GI'
.gradle/
build/
**/build/
local.properties
*.iml
.idea/
.DS_Store
rust/target/
library/src/main/jniLibs/**/*.so
ios/Frameworks/Bip39Engine.xcframework/
ios/Frameworks/build/
*.apk
*.aab
.cxx/
GI
fi

# Init repo if needed
if [[ ! -d .git ]]; then
  git init
  git branch -M "$BRANCH"
fi

# Create GitHub repo if missing
if gh repo view "$FULL" >/dev/null 2>&1; then
  echo "==> Repo exists: $FULL"
else
  echo "==> Creating repo $FULL ..."
  gh repo create "$FULL" $VISIBILITY --source=. --remote=origin --description "BIP39 SDK for Android & iOS — Rust core, Compose/SwiftUI, Tink, 20 themes" || {
    # create without --source if remote already weird
    gh repo create "$FULL" $VISIBILITY --description "BIP39 SDK for Android & iOS — Rust core, Compose/SwiftUI, Tink, 20 themes"
  }
fi

# Ensure origin points to the right place
REMOTE_URL="https://github.com/${FULL}.git"
if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REMOTE_URL"
else
  git remote add origin "$REMOTE_URL"
fi

# Prefer SSH if gh uses ssh
if gh config get git_protocol 2>/dev/null | grep -qi ssh; then
  git remote set-url origin "git@github.com:${FULL}.git"
fi

echo "==> Staging..."
git add -A
# Do not fail if nothing new
if git diff --cached --quiet; then
  echo "==> Nothing new to commit; pushing branch state..."
else
  git commit -m "$COMMIT_MSG" || true
fi

# First push / set upstream
echo "==> Pushing $BRANCH → origin..."
if git rev-parse --verify "$BRANCH" >/dev/null 2>&1; then
  git push -u origin "$BRANCH"
else
  git branch -M "$BRANCH"
  git push -u origin "$BRANCH"
fi

# Optional lightweight tag for JitPack-style consumers
TAG="${TAG:-}"
if [[ -n "$TAG" ]]; then
  git tag -f "$TAG"
  git push -f origin "$TAG"
  echo "==> Tag $TAG pushed"
fi

echo ""
echo "Done: https://github.com/${FULL}"
echo "Clone: gh repo clone ${FULL}"

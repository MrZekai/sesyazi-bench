#!/usr/bin/env bash
# Kullanım (Git Bash):  ./github-e-yukle.sh  [repo-url]
# Önce github.com/new adresinde BOŞ bir repo aç (README ekleme).
set -euo pipefail
REPO_URL="${1:-https://github.com/MrZekai/sesyazi-bench.git}"
cd "$(dirname "$0")"
git remote remove origin 2>/dev/null || true
git remote add origin "$REPO_URL"
git push -u origin main
echo
echo "Yüklendi. APK derlemesi: ${REPO_URL%.git}/actions"

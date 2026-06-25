#!/usr/bin/env bash
# ============================================================================
# clean.sh - 清理 .gitignore 中被忽略的未追踪文件与目录
#
# 用法（在项目根目录执行）：
#   bash scripts/clean.sh              — 预览将要删除的文件（安全）
#   bash scripts/clean.sh --run        — 确认后执行删除
#   bash scripts/clean.sh --run -y     — 跳过确认，直接删除
#
# 仅删除未追踪且匹配 .gitignore 的文件（如 build/、.gradle/、*.apk 等）。
# ============================================================================

set -euo pipefail
cd "$(dirname "$0")/.."

MODE="${1:-}"
SKIP_CONFIRM="${2:-}"

items=$(git ls-files --others --ignored --exclude-standard)

if [[ -z "$items" ]]; then
    echo "Nothing to clean."
    exit 0
fi

# --------------------------------------------------------------------
# 预览模式（默认）
# --------------------------------------------------------------------
if [[ "$MODE" != "--run" ]]; then
    count=$(echo "$items" | wc -l | tr -d ' ')
    echo "=== Preview ($count items would be removed) ==="
    echo "$items"
    echo ""
    echo "To execute: $0 --run"
    exit 0
fi

# --------------------------------------------------------------------
# 确认 + 执行
# --------------------------------------------------------------------
count=$(echo "$items" | wc -l | tr -d ' ')
echo "=== Clean ==="
echo "  Items: $count"
echo ""

if [[ "$SKIP_CONFIRM" != "-y" ]]; then
    read -r -p "Proceed? [y/N] " answer
    if [[ "$answer" != "y" && "$answer" != "Y" ]]; then
        echo "Cancelled."
        exit 0
    fi
fi

git ls-files --others --ignored --exclude-standard -z | xargs -0 rm -rf
echo "Done. Removed $count item(s)."

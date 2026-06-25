#!/usr/bin/env bash
# =============================================================================
# arch-graph.sh — Architecture Visualization (ROADMAP P1-2)
#
# Scans app/src/main/java/com/pai/app/**/*.kt for `import com.pai.app.*` lines,
# aggregates them by logical module, and emits a Mermaid dependency graph to
# docs/architecture-graph.md.
#
# Usage:
#   ./scripts/arch-graph.sh                  # regenerate docs/architecture-graph.md
#   ./scripts/arch-graph.sh --check          # CI mode: verify graph is up-to-date
#   ./scripts/arch-graph.sh --output <file>  # custom output path
#   ./scripts/arch-graph.sh --help
# =============================================================================
set -eo pipefail

# --- colors (degrade gracefully on non-TTY) ---------------------------------
if [[ -t 1 ]]; then
    GREEN=$'\033[32m'
    RED=$'\033[31m'
    YELLOW=$'\033[33m'
    BLUE=$'\033[34m'
    NC=$'\033[0m'
else
    GREEN=""
    RED=""
    YELLOW=""
    BLUE=""
    NC=""
fi

# --- defaults ----------------------------------------------------------------
OUTPUT="docs/architecture-graph.md"
CHECK=0
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# --- usage -------------------------------------------------------------------
usage() {
    cat <<EOF
${BLUE}Usage:${NC} $0 [OPTIONS]

Scan app/src/main/java/com/pai/app/**/*.kt for com.pai.app.* imports and
generate a Mermaid dependency graph at docs/architecture-graph.md.

${BLUE}Options:${NC}
  -h, --help            Show this help message and exit
  --check               Verify the graph file is up-to-date (CI mode).
                        Exits 0 if up-to-date, 1 if drifted.
  --output <file>       Output file path (default: docs/architecture-graph.md)

${BLUE}Examples:${NC}
  $0                          Regenerate the architecture graph
  $0 --check                  Verify graph is current (used in CI)
  $0 --output /tmp/graph.md   Write to a custom path

${BLUE}Logical module mapping:${NC}
  com.pai.app                       -> app
  com.pai.app.navigation.*          -> navigation
  com.pai.app.feature.<name>.*      -> feature/<name>
  com.pai.app.core.<name>.*         -> core/<name>           (base|network|database|
                                                              datastore|data|domain|
                                                              util|appstate)
  com.pai.app.core.designsystem.<s> -> core/designsystem/<s> (foundation|primitives|
                                                              patterns|shell|overlays)
  com.pai.app.<TopLevelClass>       -> app                   (e.g. BuildConfig)

Standard library imports (kotlin.*, kotlinx.*, java.*, android.*, androidx.*)
and self-imports (A -> A) are excluded from the graph.
EOF
}

# --- parse args --------------------------------------------------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --check)
            CHECK=1
            shift
            ;;
        --output)
            if [[ -z "${2:-}" ]]; then
                echo "${RED}Error: --output requires a file argument${NC}" >&2
                exit 1
            fi
            OUTPUT="$2"
            shift 2
            ;;
        *)
            echo "${RED}Error: unknown option: $1${NC}" >&2
            usage >&2
            exit 1
            ;;
    esac
done

# --- validate project root ---------------------------------------------------
# Resolve OUTPUT relative to CWD (not script dir) so users can run from anywhere.
# But require CWD to be the project root (contains app/build.gradle.kts).
if [[ ! -f "app/build.gradle.kts" ]]; then
    echo "${RED}Error: app/build.gradle.kts not found.${NC}" >&2
    echo "       Run this script from the project root." >&2
    exit 1
fi

# --- check ripgrep availability ---------------------------------------------
if ! command -v rg >/dev/null 2>&1; then
    echo "${RED}Error: ripgrep (rg) is required but not found in PATH.${NC}" >&2
    echo "       Install: https://github.com/BurntSushi/ripgrep#installation" >&2
    exit 1
fi

# --- map_to_module: package string -> logical module name --------------------
# Args: $1 = full package string (e.g. "com.pai.app.core.base")
# Echoes: logical module name (e.g. "core/base"), or empty on failure.
map_to_module() {
    local pkg="$1"
    # Exact top-level package (MainActivity, PaiApplication) -> "app"
    if [[ "$pkg" == "com.pai.app" || -z "$pkg" ]]; then
        echo "app"
        return
    fi
    # Strip "com.pai.app." prefix
    local rel="${pkg#com.pai.app.}"
    # If the prefix wasn't present (shouldn't happen — rg filters), bail out.
    if [[ "$rel" == "$pkg" ]]; then
        return
    fi
    # Split remaining path on '.'
    local -a seg
    IFS='.' read -ra seg <<< "$rel"
    local first="${seg[0]:-}"
    local second="${seg[1]:-}"
    local third="${seg[2]:-}"

    case "$first" in
        navigation)
            echo "navigation"
            ;;
        feature)
            if [[ -n "$second" ]]; then
                echo "feature/$second"
            else
                echo "feature"
            fi
            ;;
        core)
            if [[ "$second" == "designsystem" ]]; then
                if [[ -n "$third" ]]; then
                    echo "core/designsystem/$third"
                else
                    echo "core/designsystem"
                fi
            elif [[ -n "$second" ]]; then
                echo "core/$second"
            else
                echo "core"
            fi
            ;;
        *)
            # Top-level class in com.pai.app (e.g. BuildConfig) -> "app" module
            echo "app"
            ;;
    esac
}

# --- scan --------------------------------------------------------------------
echo "${BLUE}▶ Scanning app/src/main/java/com/pai/app/ ...${NC}" >&2

SRC_DIR="app/src/main/java/com/pai/app"
if [[ ! -d "$SRC_DIR" ]]; then
    echo "${RED}Error: source dir $SRC_DIR not found.${NC}" >&2
    exit 1
fi

tmp_nodes="$(mktemp)"
tmp_edges="$(mktemp)"
trap 'rm -f "$tmp_nodes" "$tmp_edges"' EXIT

# List kt files via ripgrep (fast).
mapfile -t KT_FILES < <(rg -l "^package com\.pai\.app" "$SRC_DIR" -g '*.kt' || true)

if [[ ${#KT_FILES[@]} -eq 0 ]]; then
    echo "${RED}Error: no .kt files with com.pai.app package declarations found.${NC}" >&2
    exit 1
fi

echo "${BLUE}▶ Found ${#KT_FILES[@]} Kotlin files; analyzing imports...${NC}" >&2

for file in "${KT_FILES[@]}"; do
    # Read the file's package declaration (first `package` line)
    pkg_line="$(rg "^package " "$file" --no-line-number --no-filename | head -n1 || true)"
    pkg="$(printf '%s' "$pkg_line" | sed 's/^package[[:space:]]*//' | awk '{print $1}' | tr -d '[:space:]')"
    src_module="$(map_to_module "$pkg")"
    if [[ -z "$src_module" ]]; then
        continue
    fi
    # Record the source module as a node (even if it has no out-edges)
    printf '%s\n' "$src_module" >> "$tmp_nodes"

    # Iterate over each `import com.pai.app.*` line in this file
    while IFS= read -r imp_line; do
        [[ -z "$imp_line" ]] && continue
        # Strip leading "import" + whitespace, take first whitespace-delimited token
        # (handles trailing `as Alias`)
        imp_pkg="$(printf '%s' "$imp_line" | sed 's/^import[[:space:]]*//' | awk '{print $1}')"
        tgt_module="$(map_to_module "$imp_pkg")"
        [[ -z "$tgt_module" ]] && continue
        # Record target as a node
        printf '%s\n' "$tgt_module" >> "$tmp_nodes"
        # Emit edge (skip self-edges)
        if [[ "$src_module" != "$tgt_module" ]]; then
            printf '%s|%s\n' "$src_module" "$tgt_module" >> "$tmp_edges"
        fi
    done < <(rg "^import com\.pai\.app\." "$file" --no-line-number --no-filename || true)
done

# Dedupe + sort
nodes_sorted="$(sort -u "$tmp_nodes")"
edges_sorted="$(sort -u "$tmp_edges")"

# Count (handle empty case)
if [[ -z "$nodes_sorted" ]]; then
    node_count=0
else
    node_count="$(printf '%s\n' "$nodes_sorted" | wc -l | tr -d '[:space:]')"
fi
if [[ -z "$edges_sorted" ]]; then
    edge_count=0
else
    edge_count="$(printf '%s\n' "$edges_sorted" | wc -l | tr -d '[:space:]')"
fi

echo "${BLUE}▶ Graph: $node_count nodes, $edge_count edges${NC}" >&2

# --- build Mermaid output ----------------------------------------------------
build_content() {
    local ts fence
    ts="$(date -u '+%Y-%m-%d %H:%M:%S UTC')"
    fence='```'
    cat <<'HEADER'
<!-- AUTO-GENERATED by scripts/arch-graph.sh — DO NOT EDIT -->
<!-- Run ./scripts/arch-graph.sh to regenerate -->

# Architecture Graph

> 本图由 `scripts/arch-graph.sh` 扫描 `app/src/main/java/com/pai/app/**/*.kt` 的 import 自动生成。
> 修改代码后运行 `./scripts/arch-graph.sh` 更新。CI 会验证图是否最新。

## 依赖图

```mermaid
graph TD
HEADER

    # Node declarations (sorted)
    if [[ -n "$nodes_sorted" ]]; then
        while IFS= read -r node; do
            [[ -z "$node" ]] && continue
            local id="${node//\//_}"
            printf '    %s[%s]\n' "$id" "$node"
        done <<< "$nodes_sorted"
    fi

    printf '\n'
    printf '    %s\n' '%% 边（A → B 表示 A import B 的符号）'

    # Edges (sorted)
    if [[ -n "$edges_sorted" ]]; then
        while IFS= read -r edge; do
            [[ -z "$edge" ]] && continue
            local src="${edge%%|*}"
            local tgt="${edge#*|}"
            local src_id="${src//\//_}"
            local tgt_id="${tgt//\//_}"
            printf '    %s --> %s\n' "$src_id" "$tgt_id"
        done <<< "$edges_sorted"
    fi

    cat <<EOF
${fence}

## 统计

- 节点数: ${node_count}
- 边数: ${edge_count}
- 生成时间: ${ts}
EOF
}

generated_content="$(build_content)"

# --- write or check ----------------------------------------------------------
# Normalize content by stripping the volatile timestamp line so --check is stable.
normalize() {
    sed -E 's/^- 生成时间: .*/- 生成时间: <normalized>/'
}

if [[ "$CHECK" -eq 1 ]]; then
    if [[ ! -f "$OUTPUT" ]]; then
        echo "${RED}✗ Architecture graph missing: $OUTPUT${NC}" >&2
        echo "   Run: ./scripts/arch-graph.sh" >&2
        exit 1
    fi
    expected="$(printf '%s\n' "$generated_content" | normalize)"
    actual="$(normalize < "$OUTPUT")"
    if [[ "$expected" == "$actual" ]]; then
        echo "${GREEN}✓ Architecture graph is up-to-date ($node_count nodes, $edge_count edges).${NC}"
        exit 0
    else
        echo "${RED}✗ Architecture graph has drifted: $OUTPUT${NC}" >&2
        echo "   Run ./scripts/arch-graph.sh and commit the result." >&2
        echo "" >&2
        echo "${YELLOW}--- diff (expected vs. actual) ---${NC}" >&2
        diff -u <(printf '%s\n' "$expected") <(printf '%s\n' "$actual") | head -80 >&2 || true
        echo "${YELLOW}--- end diff ---${NC}" >&2
        exit 1
    fi
else
    mkdir -p "$(dirname "$OUTPUT")"
    printf '%s\n' "$generated_content" > "$OUTPUT"
    echo "${GREEN}✓ Generated $OUTPUT ($node_count nodes, $edge_count edges)${NC}"
fi

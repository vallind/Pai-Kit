#!/usr/bin/env bash
# ============================================================================
# create-app.sh - 从 PaiScaffold 创建新应用（改包名 + 清洁化 + 构建验证）
#
# 用法（在 clone 出来的脚手架根目录执行）：
#   ./scripts/create-app.sh <新包名> [应用名称]
#
# 示例：
#   ./scripts/create-app.sh com.company.shop "购物商城"
#   ./scripts/create-app.sh com.example.blog
#
# 功能：
#   1. 全局替换包名 com.pai.app → 新包名
#   2. 重命名 Java/Kotlin 目录结构
#   3. 替换应用名称
#   4. 清理构建产物 + 旧包名空目录
#   5. 可选：重置 git 历史 / 移除 IDE 文件
#   6. 构建验证（compileDebugKotlin）
# ============================================================================

set -euo pipefail

SELF="$0"

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; }
prompt() { echo -e "${BLUE}[?]${NC} $*"; }

# ---- 帮助 ----
if [ $# -eq 0 ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    echo "用法: $SELF <新包名> [应用名称]"
    echo ""
    echo "示例:"
    echo "  $SELF com.company.shop \"购物商城\""
    echo "  $SELF com.example.blog"
    echo ""
    echo "选项:"
    echo "  -y   跳过可选提示（不重置 git 历史，不移除 IDE 文件）"
    exit 0
fi

SKIP_PROMPTS=false
if [ "$1" = "-y" ]; then
    SKIP_PROMPTS=true
    shift
fi

NEW_PACKAGE="$1"
APP_NAME="${2:-My App}"
PROJECT_DIR="$(pwd)"
OLD_PACKAGE="com.pai.app"
PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

# ---- 前置校验 ----
if [ ! -f "settings.gradle.kts" ]; then
    error "请在脚手架项目根目录执行此脚本（未找到 settings.gradle.kts）"
    exit 1
fi
if [ ! -d "app/src/main/java/com/pai/app" ]; then
    error "请在脚手架项目根目录执行此脚本（未找到 app/src/main/java/com/pai/app）"
    exit 1
fi
if ! echo "$NEW_PACKAGE" | grep -qE '^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$'; then
    error "包名格式不正确（应为 com.company.app 这种小写点分格式）"
    exit 1
fi

# ---- 平台检测 ----
sed_inplace() {
    if [[ "$(uname -s)" == "Darwin" ]]; then
        sed -i '' "$@"
    else
        sed -i "$@"
    fi
}

echo ""
echo "=========================================="
echo "  创建新应用"
echo "=========================================="
echo "  旧包名:     $OLD_PACKAGE"
echo "  新包名:     $NEW_PACKAGE"
echo "  应用名称:   $APP_NAME"
echo "  平台:       $(uname -s)"
echo "=========================================="
echo ""

# ---- 1. 替换所有源码/配置中的包名 ----
info "[1/7] 替换包名..."

find "$PROJECT_DIR" \
    -path "*/build/*" -prune -o \
    -path "*/.gradle/*" -prune -o \
    -type f \( -name "*.kt" -o -name "*.xml" -o -name "*.kts" \
               -o -name "*.java" -o -name "*.json" -o -name "*.md" \
               -o -name "*.toml" -o -name "*.pro" -o -name "*.properties" \) \
    -print | while IFS= read -r file; do
    if grep -q "$OLD_PACKAGE" "$file" 2>/dev/null; then
        sed_inplace "s/$OLD_PACKAGE/$NEW_PACKAGE/g" "$file"
    fi
done
info "  完成"

# ---- 2. 重命名目录 ----
info "[2/7] 重命名目录..."
for SRC_TYPE in main test androidTest; do
    OLD_DIR="app/src/$SRC_TYPE/java/com/pai/app"
    NEW_DIR="app/src/$SRC_TYPE/java/$PACKAGE_PATH"
    if [ -d "$OLD_DIR" ]; then
        mkdir -p "$(dirname "$NEW_DIR")"
        mv "$OLD_DIR" "$NEW_DIR"
        rm -rf "app/src/$SRC_TYPE/java/com/pai"
    fi
done
info "  完成"

# ---- 3. 替换应用名称 ----
info "[3/7] 替换应用名称..."
find "$PROJECT_DIR" -name "strings.xml" -not -path "*/build/*" -not -path "*/.gradle/*" \
    -exec grep -q "Pai App" {} \; \
    -exec sed_inplace "s/Pai App/$APP_NAME/g" {} \; 2>/dev/null || true
info "  完成"

# ---- 4. 清理构建产物 + 旧包名空目录 + Termux 激活配置 ----
info "[4/7] 清理构建产物与旧目录..."
rm -rf .gradle/ build/ app/build/ 2>/dev/null || true
find app/src -type d -path "*/com/pai" -exec rm -rf {} + 2>/dev/null || true
find app/src -type d -empty -delete 2>/dev/null || true

# 派生 App 不应继承脚手架中激活的 Termux 配置（决策 10）：
# 若 gradle.properties 含未注释的 Termux 行，自动注释化。
if [ -f "gradle.properties" ]; then
    sed_inplace \
        -e 's|^android\.builder\.sdkDownload=false|# android.builder.sdkDownload=false|' \
        -e 's|^android\.aapt2FromMavenOverride=.*|# &|' \
        gradle.properties
    info "  gradle.properties Termux 配置已注释（如存在）"
fi
info "  完成"

# ---- 5. 可选：重置 git 历史 + 移除 IDE 文件 ----
if [ "$SKIP_PROMPTS" = false ]; then
    # 重置 git 历史
    if [ -d ".git" ]; then
        echo ""
        prompt "是否重置 git 历史？将以当前文件作为初始 commit。 [y/N] "
        read -r RESET_GIT
        if [ "$RESET_GIT" = "y" ] || [ "$RESET_GIT" = "Y" ]; then
            info "[5/7] 重置 git 历史..."
            ROOT_COMMIT=$(git rev-list --max-parents=0 HEAD 2>/dev/null || echo "")
            if [ -n "$ROOT_COMMIT" ]; then
                git reset --soft "$ROOT_COMMIT"
            fi
            git add -A
            git commit --amend -m "initial: 从 PaiScaffold 派生 $(date +%Y-%m-%d)"
            info "  完成"
        else
            echo "  跳过"
        fi
    fi

    # 移除 IDE 文件
    echo ""
    prompt "是否移除 IDE 文件（.idea/ *.iml）？Android Studio 会自动重建。 [y/N] "
    read -r REMOVE_IDE
    if [ "$REMOVE_IDE" = "y" ] || [ "$REMOVE_IDE" = "Y" ]; then
        info "[5/7] 移除 IDE 文件..."
        rm -rf .idea/ 2>/dev/null || true
        find . -name "*.iml" -not -path "./.git/*" -delete 2>/dev/null || true
        info "  完成"
    else
        echo "  跳过"
    fi
fi

# ---- 6. Git 提交 ----
echo ""
info "[6/7] 提交 git..."
if git rev-parse --git-dir >/dev/null 2>&1; then
    git add -A
    git commit -q -m "rename: $OLD_PACKAGE → $NEW_PACKAGE ($APP_NAME)" 2>/dev/null || {
        warn "git commit 失败（可能无变更或未配置 user.name/email）"
    }
else
    warn "非 git 仓库，跳过提交"
fi
info "  完成"

# ---- 验证：检查旧包名残留 ----
echo ""
echo "=========================================="
echo "  验证"
echo "=========================================="
REMAINING=$(grep -rl "$OLD_PACKAGE" --include="*.kt" --include="*.java" --include="*.xml" --include="*.kts" --include="*.toml" "$PROJECT_DIR" 2>/dev/null \
    | grep -v "/build/" | grep -v "/.gradle/" | wc -l | tr -d ' ')
if [ "$REMAINING" -eq 0 ]; then
    info "所有文件已替换，无残留"
else
    warn "以下 $REMAINING 个文件可能仍有旧包名："
    grep -rl "$OLD_PACKAGE" --include="*.kt" --include="*.java" --include="*.xml" --include="*.kts" --include="*.toml" "$PROJECT_DIR" 2>/dev/null \
        | grep -v "/build/" | grep -v "/.gradle/"
fi

# ---- 7. 构建验证 ----
echo ""
info "[7/7] 构建验证..."
if command -v ./gradlew &>/dev/null || [ -f gradlew ]; then
    chmod +x gradlew 2>/dev/null || true
    if ./gradlew :app:compileDebugKotlin 2>&1; then
        echo ""
        echo "=========================================="
        echo "  ✅ 创建完成，编译通过"
        echo "=========================================="
    else
        echo ""
        echo "=========================================="
        echo "  ⚠️  编译失败，请检查后重试"
        echo "=========================================="
        exit 1
    fi
else
    warn "未找到 gradlew，跳过构建验证"
fi

# ---- 8. 自动安装 pre-commit hook ----
# 派生项目默认安装 git pre-commit 钩子，避免遗漏质量门禁
if [ -d .git ] && [ ! -f .git/hooks/pre-commit ]; then
    ln -sf ../../scripts/pre-commit .git/hooks/pre-commit
    chmod +x scripts/pre-commit 2>/dev/null || true
    echo "[ok] pre-commit hook installed"
fi

# ---- 完成 ----
echo ""
echo "=========================================="
echo "  完成！"
echo "=========================================="
echo ""
info "包名:     $OLD_PACKAGE → $NEW_PACKAGE"
info "应用名:   Pai App → $APP_NAME"
echo ""
info "下一步："
echo "  1. Android Studio → File → Open → 选择项目"
echo "  2. 等待 Gradle 同步完成"
echo "  3. 用 OpenCode 开始开发："
echo "     opencode"
echo "     > 添加一个商品列表页"
echo ""

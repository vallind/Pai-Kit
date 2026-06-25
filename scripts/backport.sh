#!/usr/bin/env bash
# ============================================================================
# backport.sh - 将当前 App 中新增的通用能力回传到脚手架仓库
#
# 用法（在 App 项目根目录执行）：
#   ./scripts/backport.sh <脚手架仓库路径> <文件1> [文件2] [文件3] ...
#
# 示例：
#   # 回传单个组件
#   ./scripts/backport.sh ~/projects/pai-scaffold \
#     app/src/main/java/com/company/shop/core/designsystem/primitives/DSRatingBar.kt
#
#   # 回传多个文件
#   ./scripts/backport.sh ~/projects/pai-scaffold \
#     app/src/.../DSRatingBar.kt \
#     app/src/.../DSPriceTag.kt \
#     docs/rules/07-ui-components.md
#
# 功能：
#   1. 把指定文件从当前 App 复制到脚手架仓库
#   2. 自动把包名从 App 包名替换回 com.pai.app
#   3. 在脚手架仓库 git add + commit
#   4. 打印脚手架仓库的 commit hash
# ============================================================================

set -eo pipefail

if [ $# -lt 2 ]; then
    echo "用法: $0 <脚手架仓库路径> <文件1> [文件2] ..."
    echo ""
    echo "示例:"
    echo "  $0 ~/projects/pai-scaffold app/src/.../DSRatingBar.kt"
    echo "  $0 ~/projects/pai-scaffold app/src/.../DSRatingBar.kt docs/rules/07-ui-components.md"
    exit 1
fi

SCAFFOLD_DIR="$1"
shift
FILES=("$@")

# 验证脚手架目录
if [ ! -d "$SCAFFOLD_DIR/.git" ]; then
    echo "错误: $SCAFFOLD_DIR 不是 git 仓库"
    exit 1
fi

# 获取当前 App 的包名（从 Kotlin 源码 package 指令推断，比目录路径推断更稳健）
CURRENT_PACKAGE=""
FIRST_KT=$(find app/src/main/java -type f -name "*.kt" 2>/dev/null | head -1)
if [ -n "$FIRST_KT" ]; then
    CURRENT_PACKAGE=$(grep -m1 -h '^package ' "$FIRST_KT" 2>/dev/null | head -1 | sed 's/^package //;s/[[:space:]]*$//;s/\r//')
fi

if [ -z "$CURRENT_PACKAGE" ] || [ "$CURRENT_PACKAGE" = "com.pai.app" ]; then
    echo "错误: 无法推断当前包名，或当前就是脚手架本身"
    exit 1
fi

# sed 中点号是元字符，转义后才能作为字面量匹配（如 com.company.shop → com\.company\.shop）
ESCAPED=$(printf '%s\n' "$CURRENT_PACKAGE" | sed 's/[.]/\\./g')

echo "=========================================="
echo "  Backport 到脚手架"
echo "=========================================="
echo "  脚手架目录: $SCAFFOLD_DIR"
echo "  当前包名:   $CURRENT_PACKAGE"
echo "  回传文件数: ${#FILES[@]}"
echo "=========================================="
echo ""

# 逐个文件回传
SUCCESS=0
FAIL=0

for FILE in "${FILES[@]}"; do
    if [ ! -f "$FILE" ]; then
        echo "  [跳过] 文件不存在: $FILE"
        FAIL=$((FAIL + 1))
        continue
    fi

    # 计算在脚手架中的目标路径
    # 把当前包名路径替换为 com/pai/app（用转义后的包名避免 sed 元字符误匹配）
    REL_PATH=$(echo "$FILE" | sed "s|$ESCAPED|com.pai.app|g")
    TARGET_PATH="$SCAFFOLD_DIR/$REL_PATH"

    # 创建目录
    mkdir -p "$(dirname "$TARGET_PATH")"

    # 复制文件（替换包名）
    sed "s|$ESCAPED|com.pai.app|g" "$FILE" > "$TARGET_PATH"

    echo "  [成功] $FILE → $REL_PATH"
    SUCCESS=$((SUCCESS + 1))
done

echo ""
echo "成功: $SUCCESS  跳过: $FAIL"

if [ $SUCCESS -gt 0 ]; then
    # 在脚手架仓库提交
    cd "$SCAFFOLD_DIR"
    git add -A

    COMMIT_MSG="backport: ${SUCCESS} files from app ($CURRENT_PACKAGE)"
    git commit -q -m "$COMMIT_MSG"

    HASH=$(git rev-parse --short HEAD)
    echo ""
    echo "=========================================="
    echo "  脚手架已更新"
    echo "=========================================="
    echo "  commit: $HASH"
    echo "  消息:   $COMMIT_MSG"
    echo ""
    echo "  其他 App 下次 clone 脚手架时自动获得。"
    echo "=========================================="
fi

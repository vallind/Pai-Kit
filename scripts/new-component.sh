#!/usr/bin/env bash
# ============================================================================
# new-component.sh - Scaffold a new DS (Design System) component
#
# 生成的 DS 组件遵守项目 convention：
#   - @Composable internal fun DSXxx(...)         (designsystem 包内 internal)
#   - 使用 DSTokens.Spacing.* / DSTokens.Radius.* / MSDuration.* (禁止硬编码 dp / tween)
#   - 内部可使用 M3 substrate（Button/Text/...），但对外暴露 DS API（不泄露 M3 类型）
#   - 3 个 @Preview：Light / Dark / AMOLED，均包裹在 DSDesignTheme 内
#   - KDoc 含使用示例 + 设计规范
#
# 用法:
#   ./scripts/new-component.sh <DSName> <domain> [--dry-run] [--force]
#
# 示例:
#   ./scripts/new-component.sh DSRatingBar primitives
#   ./scripts/new-component.sh DSBanner patterns --dry-run
# ============================================================================

set -eo pipefail

# ---------------------------------------------------------------------------
# TTY-aware color output
# ---------------------------------------------------------------------------
if [ -t 1 ]; then
    GREEN=$'\033[32m'
    YELLOW=$'\033[33m'
    RED=$'\033[31m'
    BLUE=$'\033[34m'
    BOLD=$'\033[1m'
    RESET=$'\033[0m'
else
    GREEN=""
    YELLOW=""
    RED=""
    BLUE=""
    BOLD=""
    RESET=""
fi

ok()   { printf '%s✓%s %s\n'  "$GREEN"  "$RESET" "$1"; }
warn() { printf '%s⚠%s %s\n'  "$YELLOW" "$RESET" "$1"; }
err()  { printf '%s✗%s %s\n'  "$RED"    "$RESET" "$1" >&2; }
info() { printf '%s→%s %s\n'  "$BLUE"   "$RESET" "$1"; }

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------
usage() {
    cat <<EOF
用法: $0 <DSName> <domain> [--dry-run] [--force]
  DSName                组件名（PascalCase，含 DS 前缀，如 "DSRatingBar"）
  domain                所属域：primitives | patterns | shell | overlays
  --dry-run, -n         预览
  --force               覆盖
  --help, -h            帮助

示例:
  $0 DSRatingBar primitives
  $0 DSBanner patterns --dry-run
EOF
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

validate_project_root() {
    local root="$1"
    if [ ! -f "$root/app/build.gradle.kts" ] || [ ! -f "$root/settings.gradle.kts" ]; then
        err "不在项目根目录：未找到 app/build.gradle.kts 或 settings.gradle.kts"
        err "请在项目根目录运行此脚本（当前: $root）"
        exit 1
    fi
}

# Validate DSName: must start with "DS" prefix and be PascalCase
validate_ds_name() {
    local name="$1"
    if ! printf '%s' "$name" | grep -Eq '^DS[A-Z][A-Za-z0-9]*$'; then
        err "组件名必须为 PascalCase 且以 DS 前缀开头，如 'DSRatingBar' / 'DSBanner'"
        err "（首字母必须大写，DS 后第一个字母也必须大写，仅含字母数字）"
        exit 1
    fi
}

# Validate domain: must be one of the 4 designsystem sub-domains
validate_domain() {
    local domain="$1"
    case "$domain" in
        primitives|patterns|shell|overlays) return 0 ;;
        *)
            err "无效域: '$domain'  必须为 primitives | patterns | shell | overlays"
            err "（详见 docs/rules/07-ui-components.md 第一节「5 层包结构」）"
            exit 1
            ;;
    esac
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
DS_NAME=""
DOMAIN=""
DRY_RUN=0
FORCE=0

while [ $# -gt 0 ]; do
    case "$1" in
        --help|-h) usage; exit 0 ;;
        --dry-run|-n) DRY_RUN=1; shift ;;
        --force) FORCE=1; shift ;;
        -*) err "未知参数: $1"; usage; exit 1 ;;
        *)
            if [ -z "$DS_NAME" ]; then
                DS_NAME="$1"
            elif [ -z "$DOMAIN" ]; then
                DOMAIN="$1"
            else
                err "多余参数: $1"; usage; exit 1
            fi
            shift
            ;;
    esac
done

if [ -z "$DS_NAME" ] || [ -z "$DOMAIN" ]; then
    usage
    exit 1
fi

validate_ds_name "$DS_NAME"
validate_domain "$DOMAIN"

# Determine project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
validate_project_root "$PROJECT_ROOT"

DS_PKG_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/designsystem/$DOMAIN"
DS_TEST_DIR="$PROJECT_ROOT/app/src/test/java/com/pai/app/core/designsystem/$DOMAIN"

DS_FILE="$DS_PKG_DIR/${DS_NAME}.kt"
DS_SCREENSHOT_TEST="$DS_TEST_DIR/${DS_NAME}ScreenshotTest.kt"

# ---------------------------------------------------------------------------
# Summary registry
# ---------------------------------------------------------------------------
CREATED_FILES=()
MODIFIED_FILES=()
SKIPPED_FILES=()
MANUAL_HINTS=()

record_create() { CREATED_FILES+=("$1"); }
record_modify() { MODIFIED_FILES+=("$1"); }
record_skip()   { SKIPPED_FILES+=("$1"); }
record_manual() { MANUAL_HINTS+=("$1"); }

# ---------------------------------------------------------------------------
# Content generators
# ---------------------------------------------------------------------------

gen_ds_component() {
    local ds_name="$1"
    local domain="$2"
    cat <<'KOTLIN' | sed "s/__DS_NAME__/$ds_name/g; s/__DOMAIN__/$domain/g"
// ============================================================================
// __DS_NAME__.kt
// __DS_NAME__ 组件 - 由 scripts/new-component.sh 生成
// 所属域：__DOMAIN__
// 业务方填充真实交互逻辑后删除本注释块
// ============================================================================
package com.pai.app.core.designsystem.__DOMAIN__

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * __DS_NAME__ - 由 new-component.sh 生成的基础骨架
 *
 * 业务方拉取脚手架后：
 * 1. 用 M3 原生组件作 substrate（designsystem 包内允许 import M3），但对外仅暴露 DS API
 *    （不把 M3 类型如 ButtonDefaults 暴露在 public/internal 签名中）
 * 2. 间距用 [DSTokens.Spacing.*]，圆角用 [DSTokens.Radius.*]
 * 3. 动画时长用 [com.pai.app.core.designsystem.foundation.tokens.MSDuration.*]
 * 4. 严禁硬编码 dp / sp / tween 数值
 *
 * 使用示例：
 * ```kotlin
 * __DS_NAME__(
 *     text = "Hello",
 *     onClick = { /* ... */ },
 *     modifier = Modifier.padding(DSTokens.Spacing.md),
 * )
 * ```
 *
 * @param modifier 修饰符
 * @param text 占位文本（业务方替换为真实参数）
 * @param onClick 占位点击回调（业务方替换为真实参数）
 */
@Composable
internal fun __DS_NAME__(
    modifier: Modifier = Modifier,
    text: String = "__DS_NAME__",
    onClick: () -> Unit = {},
) {
    // TODO 业务方实现：用 M3 substrate + DSTokens 构建组件
    // 示例：用 MaterialTheme.colorScheme.xxx + DSTokens.Spacing.xxx 包装一个 Row/Column/Box
    Column(
        modifier = modifier.padding(DSTokens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ============================================================================
// Previews: Light / Dark / AMOLED 三态（designsystem 包内统一三预览规范）
// ============================================================================

@Preview(showBackground = true, name = "__DS_NAME__ - Light")
@Composable
private fun __DS_NAME__LightPreview() {
    DSDesignTheme(darkTheme = false) {
        __DS_NAME__()
    }
}

@Preview(showBackground = true, name = "__DS_NAME__ - Dark")
@Composable
private fun __DS_NAME__DarkPreview() {
    DSDesignTheme(darkTheme = true) {
        __DS_NAME__()
    }
}

@Preview(showBackground = true, name = "__DS_NAME__ - AMOLED")
@Composable
private fun __DS_NAME__AmoledPreview() {
    DSDesignTheme(darkTheme = true, amoled = true) {
        __DS_NAME__()
    }
}
KOTLIN
}

gen_screenshot_test() {
    local ds_name="$1"
    local domain="$2"
    cat <<'KOTLIN' | sed "s/__DS_NAME__/$ds_name/g; s/__DOMAIN__/$domain/g"
// ============================================================================
// __DS_NAME__ScreenshotTest.kt
// __DS_NAME__ 截图测试骨架 - 由 scripts/new-component.sh 生成
//
// 注意：Paparazzi 当前可能尚未配置（ROADMAP P0-2）。
// 业务方在 build.gradle.kts 添加 Paparazzi 插件 + 依赖后，
// 取消下方 @Ignore 注解并启用 paparazzi.snapshot { ... } 调用即可。
// ============================================================================

package com.pai.app.core.designsystem.__DOMAIN__

import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/**
 * [__DS_NAME__] 截图测试骨架
 *
 * 由 new-component.sh 生成。Paparazzi 当前未配置时，本测试仅作占位，
 * 防止 ktlint「空文件」警告并提示业务方按 ROADMAP P0-2 启用截图测试。
 *
 * 启用步骤：
 * 1. app/build.gradle.kts 添加：
 *    ```
 *    plugins { id("app.cash.paparazzi") version "<latest>" }
 *    ```
 * 2. 取消下方 @Ignore 注解
 * 3. 将 placeholder() 替换为 paparazzi.snapshot { __DS_NAME__() }
 *    （import app.cash.paparazzi.Paparazzi + @get:Rule val paparazzi = Paparazzi(...)）
 */
class __DS_NAME__ScreenshotTest {

    @Test
    @Ignore("Paparazzi 未配置；按 ROADMAP P0-2 启用后取消本注解")
    fun `placeholder until paparazzi is configured`() {
        // 占位断言，避免空测试方法触发 ktlint 警告
        assertTrue(true)
    }

    // 启用 Paparazzi 后改为：
    //
    // @get:Rule
    // val paparazzi = Paparazzi(
    //     deviceConfig = DeviceConfig.PIXEL_6,
    //     theme = "android:Theme.Material.Light.NoActionBar",
    // )
    //
    // @Test
    // fun `__DS_NAME__ light`() {
    //     paparazzi.snapshot {
    //         DSDesignTheme(darkTheme = false) { __DS_NAME__() }
    //     }
    // }
    //
    // @Test
    // fun `__DS_NAME__ dark`() {
    //     paparazzi.snapshot {
    //         DSDesignTheme(darkTheme = true) { __DS_NAME__() }
    //     }
    // }
    //
    // @Test
    // fun `__DS_NAME__ amoled`() {
    //     paparazzi.snapshot {
    //         DSDesignTheme(darkTheme = true, amoled = true) { __DS_NAME__() }
    //     }
    // }
}
KOTLIN
}

# ---------------------------------------------------------------------------
# write_or_preview <path>
# ---------------------------------------------------------------------------
write_or_preview() {
    local path="$1"
    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would create: %s\n' "$BLUE" "$RESET" "$path"
        return 0
    fi
    if [ -f "$path" ]; then
        if [ $FORCE -eq 1 ]; then
            warn "覆盖已存在文件: $path"
        else
            warn "已存在（跳过）: $path  使用 --force 覆盖"
            record_skip "$path"
            return 1
        fi
    fi
    mkdir -p "$(dirname "$path")"
    return 0
}

# ---------------------------------------------------------------------------
# Main flow
# ---------------------------------------------------------------------------

info "DS Component: $DS_NAME  →  domain: $DOMAIN"
if [ $DRY_RUN -eq 1 ]; then info "DRY-RUN 模式：仅预览，不写入"; fi
if [ $FORCE -eq 1 ];  then info "FORCE 模式：覆盖已存在文件"; fi
echo ""

# 1. Generate component file
if write_or_preview "$DS_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_ds_component "$DS_NAME" "$DOMAIN" > "$DS_FILE"
        ok "已创建: $DS_FILE"
    fi
    record_create "$DS_FILE"
fi

# 2. Generate screenshot test file
if write_or_preview "$DS_SCREENSHOT_TEST"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_screenshot_test "$DS_NAME" "$DOMAIN" > "$DS_SCREENSHOT_TEST"
        ok "已创建: $DS_SCREENSHOT_TEST"
    fi
    record_create "$DS_SCREENSHOT_TEST"
fi

# 3. Reminder: docs/rules/07-ui-components.md mapping table row
echo ""
info "手动追加 docs/rules/07-ui-components.md 第三节「原生→DS 映射表」一行："
printf '  | `<M3 原生替代>` | `%s` | %s |\n' "$DS_NAME" "$DOMAIN"
record_manual "docs/rules/07-ui-components.md (append mapping table row for $DS_NAME)"

# Paparazzi reminder
info "Paparazzi 截图测试当前为占位骨架（@Ignore），按 ROADMAP P0-2 启用 Paparazzi 后取消 @Ignore"
record_manual "ROADMAP P0-2: configure Paparazzi plugin to enable $DS_NAME screenshot tests"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
printf '%s=== Summary ===%s\n' "$BOLD" "$RESET"
printf '%sCreated (%d):%s\n' "$GREEN" "${#CREATED_FILES[@]}" "$RESET"
for f in "${CREATED_FILES[@]}"; do printf '  + %s\n' "$f"; done
if [ ${#MODIFIED_FILES[@]} -gt 0 ]; then
    printf '%sModified (%d):%s\n' "$YELLOW" "${#MODIFIED_FILES[@]}" "$RESET"
    for f in "${MODIFIED_FILES[@]}"; do printf '  ~ %s\n' "$f"; done
fi
if [ ${#SKIPPED_FILES[@]} -gt 0 ]; then
    printf '%sSkipped (%d):%s\n' "$YELLOW" "${#SKIPPED_FILES[@]}" "$RESET"
    for f in "${SKIPPED_FILES[@]}"; do printf '  - %s\n' "$f"; done
fi
if [ ${#MANUAL_HINTS[@]} -gt 0 ]; then
    printf '%sManual follow-up (%d):%s\n' "$YELLOW" "${#MANUAL_HINTS[@]}" "$RESET"
    for f in "${MANUAL_HINTS[@]}"; do printf '  ! %s\n' "$f"; done
fi
echo ""
if [ $DRY_RUN -eq 1 ]; then
    info "DRY-RUN：未实际写入。去掉 --dry-run 实际生成代码。"
else
    ok "完成。下一步：填充组件真实交互逻辑 + 运行 ./gradlew :app:compileDebugKotlin 验证。"
fi

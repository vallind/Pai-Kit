#!/usr/bin/env bash
# ============================================================================
# new-feature.sh - Scaffold a new feature (ViewModel + Screen + Route + Test)
#
# 生成的代码严格遵守项目 convention：
#   - ViewModel: @HiltViewModel internal class, extends BaseViewModel(navigator, userState)
#   - UiState:   internal data class : UiState
#   - Screen:    @Composable internal fun, uses DSAppScaffold + DS components (no raw M3)
#   - Route:     @Serializable data object XxxRoute : AppRoute
#   - NavExt:    internal fun AppNavigator.gotoXxx() { navigate(XxxRoute) }
#
# 用法:
#   ./scripts/new-feature.sh <name> [--with-repository] [--dry-run] [--force]
#
# 示例:
#   ./scripts/new-feature.sh product --with-repository
#   ./scripts/new-feature.sh order-detail --dry-run
# ============================================================================

set -eo pipefail

# ---------------------------------------------------------------------------
# TTY-aware color output (graceful on non-TTY / CI)
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
用法: $0 <name> [--with-repository] [--dry-run] [--force]
  name                  feature 名称（小写 kebab-case，如 "product" / "order-detail"）
  --with-repository     同时生成 Repository + DTO + Entity + DAO（默认不生成）
  --dry-run, -n         预览将创建的文件，不实际写入
  --force               覆盖已存在的文件
  --help, -h            显示帮助

示例:
  $0 product --with-repository
  $0 order-detail --dry-run
EOF
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Validate project root: must contain app/build.gradle.kts + settings.gradle.kts
validate_project_root() {
    local root="$1"
    if [ ! -f "$root/app/build.gradle.kts" ] || [ ! -f "$root/settings.gradle.kts" ]; then
        err "不在项目根目录：未找到 app/build.gradle.kts 或 settings.gradle.kts"
        err "请在项目根目录运行此脚本（当前: $root）"
        exit 1
    fi
}

# Validate feature name: lowercase kebab-case
validate_name() {
    local name="$1"
    if ! printf '%s' "$name" | grep -Eq '^[a-z][a-z0-9-]*$'; then
        err "名称必须为小写 kebab-case（a-z0-9 + 连字符），如 'product' / 'order-detail'"
        exit 1
    fi
    if printf '%s' "$name" | grep -Eq -- '-$'; then
        err "名称不能以连字符结尾"
        exit 1
    fi
    if printf '%s' "$name" | grep -Eq -- '--'; then
        err "名称不能包含连续连字符"
        exit 1
    fi
}

# Convert kebab-case to PascalCase: product -> Product, order-detail -> OrderDetail
to_pascal() {
    local name="$1"
    printf '%s' "$name" \
        | tr -- '-_' '  ' \
        | awk '{ for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2); print }' \
        | tr -d ' '
}

# Convert kebab-case to package-safe lowercase: order-detail -> orderdetail
# Kotlin package names cannot contain hyphens; remove `-` and `_`.
to_pkg_name() {
    local name="$1"
    printf '%s' "$name" | tr -d -- '-_'
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
NAME=""
WITH_REPO=0
DRY_RUN=0
FORCE=0

while [ $# -gt 0 ]; do
    case "$1" in
        --help|-h) usage; exit 0 ;;
        --with-repository) WITH_REPO=1; shift ;;
        --dry-run|-n) DRY_RUN=1; shift ;;
        --force) FORCE=1; shift ;;
        -*) err "未知参数: $1"; usage; exit 1 ;;
        *)
            if [ -z "$NAME" ]; then
                NAME="$1"
            else
                err "多余参数: $1"; usage; exit 1
            fi
            shift
            ;;
    esac
done

if [ -z "$NAME" ]; then
    usage
    exit 1
fi

validate_name "$NAME"

# Determine project root: parent of scripts/ directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
validate_project_root "$PROJECT_ROOT"

PASCAL="$(to_pascal "$NAME")"
PKG_NAME="$(to_pkg_name "$NAME")"
ROUTE="${PASCAL}Route"

FEATURE_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/feature/$NAME"
TEST_DIR="$PROJECT_ROOT/app/src/test/java/com/pai/app/feature/$NAME"

APP_ROUTES="$PROJECT_ROOT/app/src/main/java/com/pai/app/navigation/routes/AppRoutes.kt"
NAV_EXTENSIONS="$PROJECT_ROOT/app/src/main/java/com/pai/app/navigation/extension/NavExtensions.kt"
MAIN_ACTIVITY="$PROJECT_ROOT/app/src/main/java/com/pai/app/MainActivity.kt"

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
# Content generators (write to stdout via heredoc + sed)
# ---------------------------------------------------------------------------

gen_view_model() {
    local name="$1"
    local pascal="$2"
    local pkg="$3"
    cat <<'KOTLIN' | sed "s/__NAME__/$name/g; s/__PASCAL__/$pascal/g; s/__PKG__/$pkg/g"
// ============================================================================
// __PASCAL__ViewModel.kt
// __PASCAL__ feature ViewModel — 由 scripts/new-feature.sh 生成
// 业务方填充真实业务逻辑后删除本注释块
// ============================================================================
package com.pai.app.feature.__PKG__

import com.pai.app.core.base.BaseViewModel
import com.pai.app.core.base.UiState
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * __PASCAL__ 页面 UI 状态
 *
 * 由 new-feature.sh 生成的基础骨架，业务方按需添加业务字段
 * （如 isLoading / error / items / form 字段等）。
 *
 * @param isLoading 是否加载中
 * @param error 错误信息（null 表示无错误）
 */
internal data class __PASCAL__UiState(
    val isLoading: Boolean = false,
    val error: String? = null,
) : UiState

/**
 * __PASCAL__ 页面 ViewModel
 *
 * 由 new-feature.sh 生成，继承 [BaseViewModel] 演示基础模板。
 *
 * 业务方拉取脚手架后：
 * 1. 在 [__PASCAL__UiState] 中添加业务字段
 * 2. 添加事件回调方法（如 onXxxClick / loadData 等）
 * 3. 如需网络请求，注入对应 Repository（用 [com.pai.app.core.base.ApiResult] 包装结果）
 *    并按 docs/rules/09-feature-templates.md 选择模板 A/B/C/D
 *
 * @param navigator 全局导航器（BaseViewModel 注入，类型安全跳转）
 * @param userState 全局登录状态（BaseViewModel 注入）
 */
@HiltViewModel
internal class __PASCAL__ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow(__PASCAL__UiState())

    /** UI 状态流 */
    val uiState: StateFlow<__PASCAL__UiState> = _uiState.asStateFlow()

    /** 业务方按需实现：加载数据、处理事件等 */
    fun onSomethingClick() {
        _uiState.update { it.copy(isLoading = true) }
        // TODO 业务方实现：调用 repository.loadXxx() 并处理 ApiResult
    }
}
KOTLIN
}

gen_screen() {
    local name="$1"
    local pascal="$2"
    local pkg="$3"
    cat <<'KOTLIN' | sed "s/__NAME__/$name/g; s/__PASCAL__/$pascal/g; s/__PKG__/$pkg/g"
// ============================================================================
// __PASCAL__Screen.kt
// __PASCAL__ 页面 — 由 scripts/new-feature.sh 生成
// 使用 DSAppScaffold + DS 组件（禁止 Material3 原生组件）
// ============================================================================
package com.pai.app.feature.__PKG__

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold

/**
 * __PASCAL__Screen - __PASCAL__ 页面
 *
 * 由 new-feature.sh 生成的基础骨架，使用 [DSAppScaffold] + DS 组件。
 *
 * 业务方拉取脚手架后：
 * 1. 在 [__PASCAL__Content] 内填充真实业务 UI
 * 2. 替换 DSText 占位内容
 * 3. 严禁使用 Material3 原生组件（如 Button / Text），必须用 DS* 组件
 *    （detekt ForbiddenImport 会拦截违规）
 * 4. 间距用 [DSTokens.Spacing.*]，禁止硬编码 dp
 *
 * @param onBackClick 返回回调（由 AppNavGraph 传入 appNavigator.goBack()）
 * @param viewModel 注入的 __PASCAL__ViewModel
 */
@Composable
internal fun __PASCAL__Screen(
    onBackClick: () -> Unit,
    viewModel: __PASCAL__ViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DSAppScaffold(
        title = "__PASCAL__",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        __PASCAL__Content(
            uiState = uiState,
            onSomethingClick = viewModel::onSomethingClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = DSTokens.Spacing.lg,
                    vertical = DSTokens.Spacing.md,
                ),
        )
    }
}

@Composable
private fun __PASCAL__Content(
    uiState: __PASCAL__UiState,
    onSomethingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
    ) {
        DSText(
            text = "__PASCAL__ 页面（脚手架）",
            variant = DSTextVariant.HeadlineMedium,
            color = DSTextColor.Primary,
        )
        DSText(
            text = "由 new-feature.sh 生成，业务方替换为真实业务 UI",
            variant = DSTextVariant.BodyMedium,
            color = DSTextColor.Secondary,
        )
        DSButton(
            text = "占位按钮",
            onClick = onSomethingClick,
            style = DSButtonStyle.Filled,
            loading = uiState.isLoading,
        )
    }
}
KOTLIN
}

gen_view_model_test() {
    local name="$1"
    local pascal="$2"
    local pkg="$3"
    cat <<'KOTLIN' | sed "s/__NAME__/$name/g; s/__PASCAL__/$pascal/g; s/__PKG__/$pkg/g"
// ============================================================================
// __PASCAL__ViewModelTest.kt
// __PASCAL__ViewModel 单元测试骨架 — 由 scripts/new-feature.sh 生成
// 业务方填充真实测试用例后删除本注释块
// ============================================================================

package com.pai.app.feature.__PKG__

import com.pai.app.core.datastore.UserPreferences
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import com.pai.app.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [__PASCAL__ViewModel] 单元测试骨架
 *
 * 由 new-feature.sh 生成。验证：
 * - 初始 uiState 为默认值
 * - onSomethingClick 设置 isLoading = true
 *
 * 业务方拉取脚手架后：
 * 1. 如注入了 Repository，mock repository 行为
 * 2. 添加业务测试用例（如加载成功 / 失败 / 字段校验）
 *
 * 参考 feature/auth/AuthViewModelTest.kt 的完整测试模式。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class __PASCAL__ViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    /** 应用级协程作用域，用于构造 UserState */
    private val appScope = TestScope(mainDispatcherRule.testDispatcher)

    private val userState: UserState = UserState(appScope, userPreferences)

    private lateinit var viewModel: __PASCAL__ViewModel

    @Before
    fun setUp() {
        // UserState.isLoggedIn 派生自 userPreferences.isLoggedIn，需 mock 为 MutableStateFlow
        every { userPreferences.isLoggedIn } returns MutableStateFlow(false)
        viewModel = __PASCAL__ViewModel(appNavigator, userState)
    }

    @Test
    fun `初始 uiState 字段均为默认值`() = runTest(mainDispatcherRule.testDispatcher) {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onSomethingClick 设置 isLoading 为 true`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onSomethingClick()

            assertTrue(viewModel.uiState.value.isLoading)
        }
}
KOTLIN
}

# ---------------------------------------------------------------------------
# write_or_preview <path>
# Returns 0 if file should be written (or dry-run printed), 1 if skipped.
# ---------------------------------------------------------------------------
write_or_preview() {
    local path="$1"
    local label="$2"
    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would create: %s\n' "$BLUE" "$RESET" "$path"
        # NOTE: 不在此处 record_create —— 主流程在 write_or_preview 返回后会统一记录，
        # 否则在 --dry-run 模式下 Summary 会重复列出每个文件两次。
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
# File-edit operations (auto-edit existing files via embedded Python)
# ---------------------------------------------------------------------------

# Edit AppRoutes.kt: insert @Serializable data object <Name>Route : AppRoute
# before the "// NavHost 起始目的地" section divider.
edit_app_routes() {
    local path="$1"
    local route_decl
    route_decl=$(printf '/** %s feature 路由（由 new-feature.sh 生成） */\n@Serializable\ndata object %s : AppRoute\n' "$PASCAL" "$ROUTE")

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (append route): %s\n' "$BLUE" "$RESET" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "data object ${ROUTE} " "$path" 2>/dev/null; then
        warn "AppRoutes.kt 已含 ${ROUTE}，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$route_decl" "$ROUTE" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
decl = sys.argv[2]
route = sys.argv[3]
force = sys.argv[4] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Idempotency: skip if route already present
pattern_existing = r"data object\s+" + re.escape(route) + r"\s*:\s*AppRoute"
if re.search(pattern_existing, content):
    if not force:
        print("SKIP: route already exists in AppRoutes.kt", file=sys.stderr)
        sys.exit(0)

# Find the section "// NavHost 起始目的地" and insert before its preceding divider
marker = "// NavHost 起始目的地"
idx = content.find(marker)
if idx == -1:
    # Fallback: insert before `val StartDestination`
    m = re.search(r"^val\s+StartDestination", content, re.MULTILINE)
    if m is None:
        print("ERROR: cannot find insertion point in AppRoutes.kt", file=sys.stderr)
        sys.exit(1)
    insert_idx = m.start()
else:
    # Search backward from idx for the line `// ===` divider
    line_start = content.rfind("\n// =", 0, idx)
    if line_start == -1:
        insert_idx = idx
    else:
        insert_idx = line_start + 1  # after the \n

# Insert decl + blank line
new_content = content[:insert_idx] + decl + "\n\n" + content[insert_idx:]
with open(path, "w", encoding="utf-8") as f:
    f.write(new_content)
print("OK")
PY
        ok "已编辑（追加路由 $ROUTE）: $path"
        record_modify "$path"
    else
        warn "AppRoutes.kt 自动编辑失败，请手动追加："
        printf '%s\n' "$route_decl"
        record_manual "$path (append: $ROUTE)"
    fi
}

# Edit NavExtensions.kt: append internal fun AppNavigator.goto<Name>()
edit_nav_extensions() {
    local path="$1"
    local func_block
    func_block=$(cat <<KOTLIN

/**
 * 跳转到 __PASCAL__ 页面（由 new-feature.sh 生成）
 *
 * 保留回退栈，允许返回上一页。
 */
internal fun AppNavigator.goto__PASCAL__() {
    navigate(__ROUTE__)
}
KOTLIN
    )
    func_block=$(printf '%s\n' "$func_block" | sed "s/__PASCAL__/$PASCAL/g; s/__ROUTE__/$ROUTE/g")

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (append goto%s): %s\n' "$BLUE" "$RESET" "$PASCAL" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "fun AppNavigator.goto${PASCAL}" "$path" 2>/dev/null; then
        warn "NavExtensions.kt 已含 goto${PASCAL}，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$func_block" "$ROUTE" "$PASCAL" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
block = sys.argv[2]
route = sys.argv[3]
pascal = sys.argv[4]
force = sys.argv[5] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Idempotency
if re.search(r"fun\s+AppNavigator\.goto" + re.escape(pascal) + r"\b", content):
    if not force:
        print("SKIP: goto already exists in NavExtensions.kt", file=sys.stderr)
        sys.exit(0)

# 1. Add route import after the last `import com.pai.app.navigation.routes.` line
route_import = f"import com.pai.app.navigation.routes.{route}"
if route_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.navigation\.routes\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + route_import + "\n" + content[last.end():]
    else:
        # Fallback: insert after package declaration
        pkg = re.search(r"^package\s+\S+\n", content, re.MULTILINE)
        if pkg:
            content = content[:pkg.end()] + "\n" + route_import + "\n" + content[pkg.end():]

# 2. Append extension function at end of file (ensure trailing newline first)
if not content.endswith("\n"):
    content += "\n"
content += block

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 goto${PASCAL}）: $path"
        record_modify "$path"
    else
        warn "NavExtensions.kt 自动编辑失败，请手动追加："
        printf '%s\n' "$func_block"
        record_manual "$path (append: goto$PASCAL)"
    fi
}

# Edit MainActivity.kt: add imports + composable<XxxRoute> block in AppNavGraph
edit_main_activity() {
    local path="$1"

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (add composable<%s> + imports): %s\n' "$BLUE" "$RESET" "$ROUTE" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "composable<${ROUTE}>" "$path" 2>/dev/null; then
        warn "MainActivity.kt 已含 composable<${ROUTE}>，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$NAME" "$PKG_NAME" "$PASCAL" "$ROUTE" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
name = sys.argv[2]
pkg_name = sys.argv[3]
pascal = sys.argv[4]
route = sys.argv[5]
force = sys.argv[6] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Idempotency
if re.search(r"composable<" + re.escape(route) + r">", content):
    if not force:
        print("SKIP: composable<{}> already in MainActivity".format(route), file=sys.stderr)
        sys.exit(0)

feature_import = f"import com.pai.app.feature.{pkg_name}.{pascal}Screen"
route_import = f"import com.pai.app.navigation.routes.{route}"

# 1. Add feature import after the last `import com.pai.app.feature.` line
if feature_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.feature\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + feature_import + "\n" + content[last.end():]

# 2. Add route import after the last `import com.pai.app.navigation.routes.` line
if route_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.navigation\.routes\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + route_import + "\n" + content[last.end():]

# 3. Insert composable block before the NavHost closing brace.
#    AppNavGraph's NavHost block ends with:
#        composable<...> { ... }
#        }       <- 8-space (last composable close)
#    }           <- 4-space (NavHost close)
# }               <- 0-space (function close)
composable_block = (
    f"\n        // {pascal} 路由（由 new-feature.sh 生成）\n"
    f"        composable<{route}> {{\n"
    f"            {pascal}Screen(onBackClick = {{ appNavigator.goBack() }})\n"
    f"        }}\n"
)
# Find the closing pattern: \n    }\n} at end of file (NavHost close + AppNavGraph function close)
# We insert the new composable block right before this closing pattern, so it
# lands INSIDE the NavHost lambda, after the last existing composable block.
pat_close = re.compile(r"\n    \}\n\}\s*$")
m = pat_close.search(content)
if m is None:
    # Looser fallback: any \n} at end of file
    pat_loose = re.compile(r"\n\}\s*$")
    m = pat_loose.search(content)
if m is None:
    print("ERROR: cannot find NavHost closing pattern in MainActivity.kt", file=sys.stderr)
    sys.exit(1)

insert_idx = m.start()
content = content[:insert_idx] + composable_block + content[insert_idx:]

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 composable<${ROUTE}> + imports）: $path"
        record_modify "$path"
    else
        warn "MainActivity.kt 自动编辑失败，请手动追加以下内容："
        printf '  - import: com.pai.app.feature.%s.%sScreen\n' "$PKG_NAME" "$PASCAL"
        printf '  - import: com.pai.app.navigation.routes.%s\n' "$ROUTE"
        printf '  - 在 AppNavGraph 的 NavHost { ... } 块内追加：\n'
        printf '      composable<%s> {\n' "$ROUTE"
        printf '          %sScreen(onBackClick = { appNavigator.goBack() })\n' "$PASCAL"
        printf '      }\n'
        record_manual "$path (add composable<$ROUTE>)"
    fi
}

# ---------------------------------------------------------------------------
# Main flow
# ---------------------------------------------------------------------------

info "Feature: $NAME  →  PascalCase: $PASCAL  Route: $ROUTE"
if [ $DRY_RUN -eq 1 ]; then info "DRY-RUN 模式：仅预览，不写入"; fi
if [ $FORCE -eq 1 ];  then info "FORCE 模式：覆盖已存在文件"; fi
echo ""

# 1. Generate new files
VIEW_MODEL_PATH="$FEATURE_DIR/${PASCAL}ViewModel.kt"
SCREEN_PATH="$FEATURE_DIR/${PASCAL}Screen.kt"
TEST_PATH="$TEST_DIR/${PASCAL}ViewModelTest.kt"

if write_or_preview "$VIEW_MODEL_PATH" "ViewModel"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_view_model "$NAME" "$PASCAL" "$PKG_NAME" > "$VIEW_MODEL_PATH"
        ok "已创建: $VIEW_MODEL_PATH"
    fi
    record_create "$VIEW_MODEL_PATH"
fi

if write_or_preview "$SCREEN_PATH" "Screen"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_screen "$NAME" "$PASCAL" "$PKG_NAME" > "$SCREEN_PATH"
        ok "已创建: $SCREEN_PATH"
    fi
    record_create "$SCREEN_PATH"
fi

if write_or_preview "$TEST_PATH" "Test"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_view_model_test "$NAME" "$PASCAL" "$PKG_NAME" > "$TEST_PATH"
        ok "已创建: $TEST_PATH"
    fi
    record_create "$TEST_PATH"
fi

echo ""

# 2. Edit existing files (routes / nav extensions / main activity)
edit_app_routes "$APP_ROUTES"
edit_nav_extensions "$NAV_EXTENSIONS"
edit_main_activity "$MAIN_ACTIVITY"

# 3. Routing docs reminder (always — markdown table row is hard to auto-edit safely)
echo ""
info "手动追加 docs/rules/04-routing.md 路由表行："
printf '  | `%s` | data object | %s feature（由 new-feature.sh 生成） |\n' "$ROUTE" "$PASCAL"
record_manual "docs/rules/04-routing.md (append route table row for $ROUTE)"

# 4. Optional: --with-repository
if [ $WITH_REPO -eq 1 ]; then
    echo ""
    info "--with-repository 已请求：调用 new-repository.sh 生成数据层..."
    REPO_SCRIPT="$SCRIPT_DIR/new-repository.sh"
    if [ -x "$REPO_SCRIPT" ]; then
        REPO_ARGS=("$NAME")
        if [ $DRY_RUN -eq 1 ]; then REPO_ARGS+=("--dry-run"); fi
        if [ $FORCE -eq 1 ];  then REPO_ARGS+=("--force");  fi
        "$REPO_SCRIPT" "${REPO_ARGS[@]}" || warn "new-repository.sh 执行失败（请检查输出）"
    else
        warn "未找到 $REPO_SCRIPT，请手动执行：./scripts/new-repository.sh $NAME"
        record_manual "scripts/new-repository.sh $NAME (not found)"
    fi
fi

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
printf '%s=== Summary ===%s\n' "$BOLD" "$RESET"
printf '%sCreated (%d):%s\n' "$GREEN" "${#CREATED_FILES[@]}" "$RESET"
for f in "${CREATED_FILES[@]}"; do printf '  + %s\n' "$f"; done
printf '%sModified (%d):%s\n' "$YELLOW" "${#MODIFIED_FILES[@]}" "$RESET"
for f in "${MODIFIED_FILES[@]}"; do printf '  ~ %s\n' "$f"; done
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
    ok "完成。下一步：填充业务逻辑 + 运行 ./gradlew :app:compileDebugKotlin 验证。"
fi

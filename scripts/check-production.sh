#!/usr/bin/env bash
# ============================================================================
# check-production.sh - Pai Scaffold Production Readiness Auto-Checker
# ============================================================================
# 用途：自动验证 PRODUCTION_CHECKLIST.md 中可程序化检查的项目。
# 不依赖 Android SDK / Gradle — 纯 grep/test 文件内容验证。
#
# 用法：
#   ./scripts/check-production.sh           # 彩色终端输出
#   ./scripts/check-production.sh --json    # JSON 输出（CI 集成用）
#   ./scripts/check-production.sh --help    # 帮助
#
# 退出码：
#   0 = 全部通过
#   1 = 有 warn 项（业务方需接入）
#   2 = 有 fail 项（脚手架基线不达标）
# ============================================================================

set -eo pipefail

# ----------------------------------------------------------------------------
# 全局变量
# ----------------------------------------------------------------------------
SCRIPT_NAME="$(basename "$0")"
PROJECT_ROOT=""
OUTPUT_FORMAT="text"  # text | json
USE_COLOR=false

# 检查结果收集（并行数组：section / name / status）
declare -a CHECK_SECTIONS=()
declare -a CHECK_NAMES=()
declare -a CHECK_STATUSES=()   # pass | warn | fail
declare -a CHECK_MESSAGES=()

PASS_COUNT=0
WARN_COUNT=0
FAIL_COUNT=0

# ----------------------------------------------------------------------------
# 颜色（仅 TTY 启用）
# ----------------------------------------------------------------------------
if [[ -t 1 ]]; then
    USE_COLOR=true
fi
COLOR_RED=""
COLOR_YELLOW=""
COLOR_GREEN=""
COLOR_BLUE=""
COLOR_BOLD=""
COLOR_RESET=""
if [[ "$USE_COLOR" == "true" ]]; then
    COLOR_RED=$'\033[31m'
    COLOR_YELLOW=$'\033[33m'
    COLOR_GREEN=$'\033[32m'
    COLOR_BLUE=$'\033[34m'
    COLOR_BOLD=$'\033[1m'
    COLOR_RESET=$'\033[0m'
fi

# ----------------------------------------------------------------------------
# 帮助
# ----------------------------------------------------------------------------
show_help() {
    cat <<EOF
$SCRIPT_NAME - Pai Scaffold Production Readiness Auto-Checker

自动验证 PRODUCTION_CHECKLIST.md 中可程序化检查的项目（不依赖 Android SDK）。

用法:
  $SCRIPT_NAME              彩色终端输出
  $SCRIPT_NAME --json       JSON 输出（CI 集成用）
  $SCRIPT_NAME -h|--help    显示本帮助

检查范围（15 项）:
  [1/6] 构建配置  (4 项 ✅)
  [2/6] 安全      (4 项 ✅)
  [3/6] 崩溃监控  (1 项 ⚠)
  [4/6] 发布      (1 项 ⚠)
  [5/6] 质量      (3 项 ✅)
  [6/6] 可观测    (1 ✅ + 1 ⚠)

退出码:
  0  全部通过
  1  有 warn 项（业务方需接入 — 见 PRODUCTION_CHECKLIST.md）
  2  有 fail 项（脚手架基线不达标 — 需修复）

详见: PRODUCTION_CHECKLIST.md
EOF
}

# ----------------------------------------------------------------------------
# 参数解析
# ----------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        --json)
            OUTPUT_FORMAT="json"
            shift
            ;;
        *)
            echo "未知参数: $1" >&2
            echo "运行 '$SCRIPT_NAME --help' 查看用法。" >&2
            exit 2
            ;;
    esac
done

# ----------------------------------------------------------------------------
# 定位项目根目录（含 app/build.gradle.kts）
# ----------------------------------------------------------------------------
find_project_root() {
    local dir="$PWD"
    while [[ "$dir" != "/" ]]; do
        if [[ -f "$dir/app/build.gradle.kts" ]]; then
            PROJECT_ROOT="$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    return 1
}

if ! find_project_root; then
    echo "错误: 未找到项目根目录（需含 app/build.gradle.kts）。" >&2
    echo "请在 Pai Scaffold 项目内运行本脚本。" >&2
    exit 2
fi

# ----------------------------------------------------------------------------
# 辅助：在项目内 grep（兼容 P0-1 convention plugin 重构）
# 同时检查 app/build.gradle.kts 与 build-logic/ （若存在）
# 用法: grep_in_build_configs <pattern>
# 返回: 0 命中 / 1 未命中
# ----------------------------------------------------------------------------
grep_in_build_configs() {
    local pattern="$1"
    local hit=1
    # 1) app/build.gradle.kts
    if [[ -f "$PROJECT_ROOT/app/build.gradle.kts" ]] \
       && grep -Eq "$pattern" "$PROJECT_ROOT/app/build.gradle.kts"; then
        hit=0
    fi
    # 2) build-logic/convention/src/main/kotlin/*.kt (P0-1 convention plugin)
    if [[ -d "$PROJECT_ROOT/build-logic" ]]; then
        if grep -rEq "$pattern" "$PROJECT_ROOT/build-logic" 2>/dev/null; then
            hit=0
        fi
    fi
    return $hit
}

# ----------------------------------------------------------------------------
# 记录检查结果
# 用法: record <section> <name> <status> <message>
#   status: pass | warn | fail
# ----------------------------------------------------------------------------
record() {
    local section="$1"
    local name="$2"
    local status="$3"
    local message="${4:-}"
    CHECK_SECTIONS+=("$section")
    CHECK_NAMES+=("$name")
    CHECK_STATUSES+=("$status")
    CHECK_MESSAGES+=("$message")
    case "$status" in
        pass) PASS_COUNT=$((PASS_COUNT + 1)) ;;
        warn) WARN_COUNT=$((WARN_COUNT + 1)) ;;
        fail) FAIL_COUNT=$((FAIL_COUNT + 1)) ;;
    esac
}

# ============================================================================
# 检查函数（每个返回 0=pass / 1=warn / 2=fail，并通过 record 记录）
# ============================================================================

# --- [1/6] 构建配置 ---

check_minify_enabled() {
    if grep_in_build_configs 'isMinifyEnabled\s*=\s*true'; then
        record "构建配置" "release isMinifyEnabled = true" "pass"
    else
        record "构建配置" "release isMinifyEnabled = true" "fail" \
            "app/build.gradle.kts release 块未启用 isMinifyEnabled = true"
    fi
}

check_shrink_resources() {
    if grep_in_build_configs 'isShrinkResources\s*=\s*true'; then
        record "构建配置" "release isShrinkResources = true" "pass"
    else
        record "构建配置" "release isShrinkResources = true" "fail" \
            "app/build.gradle.kts release 块未启用 isShrinkResources = true"
    fi
}

check_base_url_by_buildtype() {
    # 统计 "BASE_URL" 出现次数（应 ≥ 2：debug + release）
    # 兼容 P0-1：同时检查 app/build.gradle.kts 与 build-logic/ convention plugin
    # （convention plugin 中 buildConfigField 可能跨多行，故用 "BASE_URL" 而非整行匹配）
    local count=0
    local build_gradle="$PROJECT_ROOT/app/build.gradle.kts"
    if [[ -f "$build_gradle" ]]; then
        local n
        n=$(grep -Ec '"BASE_URL"' "$build_gradle" || true)
        count=$((count + n))
    fi
    if [[ -d "$PROJECT_ROOT/build-logic" ]]; then
        local m
        m=$(grep -rEc '"BASE_URL"' "$PROJECT_ROOT/build-logic" 2>/dev/null | \
            awk -F: '{s+=$NF} END{print s+0}')
        count=$((count + m))
    fi
    if [[ "$count" -ge 2 ]]; then
        record "构建配置" "BASE_URL 按 buildType 区分" "pass" \
            "检测到 $count 处 BASE_URL 定义"
    else
        record "构建配置" "BASE_URL 按 buildType 区分" "fail" \
            "BASE_URL 出现 $count 次（需 ≥ 2：debug + release）"
    fi
}

check_test_instrumentation_runner() {
    local build_gradle="$PROJECT_ROOT/app/build.gradle.kts"
    local runner_in_config=false
    local runner_file_exists=false

    if [[ -f "$build_gradle" ]] \
       && grep -Eq 'testInstrumentationRunner\s*=\s*"com\.pai\.app\.HiltTestRunner"' "$build_gradle"; then
        runner_in_config=true
    fi
    # 兼容 P0-1：也检查 build-logic/
    if [[ "$runner_in_config" == "false" ]] && [[ -d "$PROJECT_ROOT/build-logic" ]]; then
        if grep -rEq 'testInstrumentationRunner\s*=\s*"com\.pai\.app\.HiltTestRunner"' \
           "$PROJECT_ROOT/build-logic" 2>/dev/null; then
            runner_in_config=true
        fi
    fi

    if [[ -f "$PROJECT_ROOT/app/src/androidTest/java/com/pai/app/HiltTestRunner.kt" ]]; then
        runner_file_exists=true
    fi

    if [[ "$runner_in_config" == "true" ]] && [[ "$runner_file_exists" == "true" ]]; then
        record "构建配置" "testInstrumentationRunner = HiltTestRunner" "pass"
    else
        record "构建配置" "testInstrumentationRunner = HiltTestRunner" "fail" \
            "config=$runner_in_config file=$runner_file_exists"
    fi
}

# --- [2/6] 安全 ---

check_allow_backup_false() {
    local manifest="$PROJECT_ROOT/app/src/main/AndroidManifest.xml"
    if [[ ! -f "$manifest" ]]; then
        record "安全" "allowBackup + fullBackupContent = false" "fail" \
            "AndroidManifest.xml 不存在"
        return
    fi
    local allow_backup_ok=false
    local full_backup_ok=false
    if grep -Eq 'android:allowBackup\s*=\s*"false"' "$manifest"; then
        allow_backup_ok=true
    fi
    # fullBackupContent="false" 或 dataExtractionRules 引用均算
    if grep -Eq 'android:fullBackupContent\s*=\s*"false"' "$manifest" \
       || grep -Eq 'android:dataExtractionRules\s*=' "$manifest"; then
        full_backup_ok=true
    fi
    if [[ "$allow_backup_ok" == "true" ]] && [[ "$full_backup_ok" == "true" ]]; then
        record "安全" "allowBackup + fullBackupContent = false" "pass"
    else
        record "安全" "allowBackup + fullBackupContent = false" "fail" \
            "allowBackup=$allow_backup_ok fullBackupContent/dataExtractionRules=$full_backup_ok"
    fi
}

check_network_security_config() {
    local manifest="$PROJECT_ROOT/app/src/main/AndroidManifest.xml"
    local nsc_file="$PROJECT_ROOT/app/src/main/res/xml/network_security_config.xml"
    local manifest_ref=false
    local file_exists=false

    if [[ -f "$manifest" ]] \
       && grep -Eq 'android:networkSecurityConfig\s*=\s*"@xml/network_security_config"' "$manifest"; then
        manifest_ref=true
    fi
    if [[ -f "$nsc_file" ]]; then
        file_exists=true
    fi

    if [[ "$manifest_ref" == "true" ]] && [[ "$file_exists" == "true" ]]; then
        record "安全" "networkSecurityConfig 存在" "pass"
    else
        record "安全" "networkSecurityConfig 存在" "fail" \
            "manifest_ref=$manifest_ref file=$file_exists"
    fi
}

check_encrypted_prefs() {
    local file="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/datastore/EncryptedPrefs.kt"
    if [[ ! -f "$file" ]]; then
        record "安全" "EncryptedPrefs 存在" "fail" \
            "$file 不存在"
        return
    fi
    if grep -Eq 'AES256_GCM' "$file" && grep -Eq 'EncryptedSharedPreferences' "$file"; then
        record "安全" "EncryptedPrefs 存在" "pass"
    else
        record "安全" "EncryptedPrefs 存在" "fail" \
            "EncryptedPrefs.kt 未使用 AES256-GCM + EncryptedSharedPreferences"
    fi
}

check_redact_header() {
    local file="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/network/NetworkModule.kt"
    if [[ ! -f "$file" ]]; then
        record "安全" "HttpLoggingInterceptor redactHeader(Authorization)" "fail" \
            "NetworkModule.kt 不存在"
        return
    fi
    # 检查 redactHeader("Authorization") 或 redactHeader(HEADER_AUTHORIZATION) + 常量定义
    if grep -Eq 'redactHeader\(\s*"Authorization"\s*\)' "$file" \
       || { grep -Eq 'redactHeader\(\s*HEADER_AUTHORIZATION\s*\)' "$file" \
            && grep -Eq 'HEADER_AUTHORIZATION\s*=\s*"Authorization"' "$file"; }; then
        record "安全" "HttpLoggingInterceptor redactHeader(Authorization)" "pass"
    else
        record "安全" "HttpLoggingInterceptor redactHeader(Authorization)" "fail" \
            "NetworkModule.kt 未对 Authorization 头脱敏"
    fi
}

# --- [3/6] 崩溃监控 ---

check_crash_reporter() {
    local crash_dir="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/util/crash"
    # 脚手架基线：无 CrashReporter 接入（P1-4 待实现）→ warn
    if [[ -d "$crash_dir" ]]; then
        # 目录存在 — 检查是否有非 NoOp 实现
        if grep -rEq 'class\s+\w*(Crashlytics|Bugly|Sentry)\w*' "$crash_dir" 2>/dev/null; then
            record "崩溃监控" "CrashReporter 已接入" "pass"
        else
            record "崩溃监控" "CrashReporter 仅 NoOp（业务方需接入）" "warn" \
                "core/util/crash/ 存在但仅 NoOp 实现（P1-4 待实现）"
        fi
    else
        record "崩溃监控" "CrashReporter 仅 NoOp（业务方需接入）" "warn" \
            "core/util/crash/ 不存在（P1-4 待实现）"
    fi
}

# --- [4/6] 发布 ---

check_adaptive_icon() {
    local icon_xml="$PROJECT_ROOT/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml"
    # 脚手架基线：有 mipmap-anydpi-v26/ic_launcher.xml（占位 adaptive icon）
    # 但业务方需按品牌定制 → warn
    if [[ -f "$icon_xml" ]] && grep -q '<adaptive-icon' "$icon_xml"; then
        record "发布" "adaptive icon 需品牌定制（业务方）" "warn" \
            "脚手架默认 adaptive icon 存在，业务方需按品牌替换"
    else
        record "发布" "adaptive icon 未配置（业务方）" "warn" \
            "mipmap-anydpi-v26/ic_launcher.xml 不存在或非 adaptive-icon"
    fi
}

# --- [5/6] 质量 ---

check_ci_jobs() {
    local ci_yml="$PROJECT_ROOT/.github/workflows/ci.yml"
    if [[ ! -f "$ci_yml" ]]; then
        record "质量" "CI workflow 存在 8 Job" "fail" \
            ".github/workflows/ci.yml 不存在"
        return
    fi
    # 统计 job 数：仅匹配 jobs: 块下的 "  <job-name>:" 行
    # （2 空格缩进 + 小写字母开头 + 冒号结尾，且在 ^jobs: 之后）
    local job_count
    job_count=$(awk '/^jobs:/{found=1; next} found && /^  [a-z][a-zA-Z0-9_-]*[[:space:]]*:[[:space:]]*$/{count++} END{print count+0}' "$ci_yml")
    if [[ "$job_count" -ge 8 ]]; then
        record "质量" "CI workflow 存在 8 Job" "pass" \
            "检测到 $job_count 个 Job"
    else
        record "质量" "CI workflow 存在 8 Job" "fail" \
            "仅检测到 $job_count 个 Job（需 ≥ 8）"
    fi
}

check_detekt_ignore_failures() {
    # 检查 detekt ignoreFailures = false
    # 位置：app/build.gradle.kts（detekt {} 块）或 build-logic/ convention
    local found=false
    if [[ -f "$PROJECT_ROOT/app/build.gradle.kts" ]] \
       && grep -Eq 'ignoreFailures\s*(=|\.set\()\s*false' "$PROJECT_ROOT/app/build.gradle.kts"; then
        found=true
    fi
    if [[ "$found" == "false" ]] && [[ -d "$PROJECT_ROOT/build-logic" ]]; then
        if grep -rEq 'ignoreFailures\s*(=|\.set\()\s*false' "$PROJECT_ROOT/build-logic" 2>/dev/null; then
            found=true
        fi
    fi
    if [[ "$found" == "true" ]]; then
        record "质量" "detekt ignoreFailures = false" "pass"
    else
        record "质量" "detekt ignoreFailures = false" "fail" \
            "未找到 detekt ignoreFailures = false"
    fi
}

check_konsist_tests() {
    local arch_dir="$PROJECT_ROOT/app/src/test/java/com/pai/app/architecture"
    local design_system_ok=false
    local feature_ok=false
    local route_ok=false
    if [[ -f "$arch_dir/DesignSystemArchitectureTest.kt" ]]; then
        design_system_ok=true
    fi
    if [[ -f "$arch_dir/FeatureArchitectureTest.kt" ]]; then
        feature_ok=true
    fi
    if [[ -f "$arch_dir/RouteArchitectureTest.kt" ]]; then
        route_ok=true
    fi
    if [[ "$design_system_ok" == "true" ]] \
       && [[ "$feature_ok" == "true" ]] \
       && [[ "$route_ok" == "true" ]]; then
        record "质量" "Konsist 架构测试存在" "pass" \
            "DesignSystem + Feature + Route 3 个测试文件齐全"
    else
        record "质量" "Konsist 架构测试存在" "fail" \
            "DS=$design_system_ok Feature=$feature_ok Route=$route_ok"
    fi
}

# --- [6/6] 可观测 ---

check_timber_debug_only() {
    local file="$PROJECT_ROOT/app/src/main/java/com/pai/app/PaiApplication.kt"
    if [[ ! -f "$file" ]]; then
        record "可观测" "Timber DebugTree 仅 DEBUG" "fail" \
            "PaiApplication.kt 不存在"
        return
    fi
    # 检查 Timber.plant(Timber.DebugTree()) 被 BuildConfig.DEBUG 守卫
    if grep -Eq 'BuildConfig\.DEBUG' "$file" \
       && grep -Eq 'Timber\.plant\(\s*Timber\.DebugTree\(\)\s*\)' "$file"; then
        record "可观测" "Timber DebugTree 仅 DEBUG" "pass"
    else
        record "可观测" "Timber DebugTree 仅 DEBUG" "fail" \
            "Timber.DebugTree() 未被 BuildConfig.DEBUG 守卫"
    fi
}

check_business_analytics() {
    # 业务埋点无法自动验证是否接入 → 始终 warn（业务方自检）
    record "可观测" "业务埋点未接入（业务方）" "warn" \
        "业务埋点需业务方按需接入，无法自动验证"
}

# ============================================================================
# 运行所有检查
# ============================================================================
run_all_checks() {
    # [1/6] 构建配置
    check_minify_enabled
    check_shrink_resources
    check_base_url_by_buildtype
    check_test_instrumentation_runner

    # [2/6] 安全
    check_allow_backup_false
    check_network_security_config
    check_encrypted_prefs
    check_redact_header

    # [3/6] 崩溃监控
    check_crash_reporter

    # [4/6] 发布
    check_adaptive_icon

    # [5/6] 质量
    check_ci_jobs
    check_detekt_ignore_failures
    check_konsist_tests

    # [6/6] 可观测
    check_timber_debug_only
    check_business_analytics
}

# ============================================================================
# 文本输出
# ============================================================================
render_text() {
    local total=$((PASS_COUNT + WARN_COUNT + FAIL_COUNT))
    local pass_rate=0
    if [[ "$total" -gt 0 ]]; then
        pass_rate=$((PASS_COUNT * 100 / total))
    fi

    echo "================================================================"
    echo "  Pai Scaffold Production Readiness Check"
    echo "================================================================"

    local current_section=""
    local section_index=0
    local section_order=("构建配置" "安全" "崩溃监控" "发布" "质量" "可观测")
    declare -A section_numbers=(
        [构建配置]="1/6"
        [安全]="2/6"
        [崩溃监控]="3/6"
        [发布]="4/6"
        [质量]="5/6"
        [可观测]="6/6"
    )

    local i=0
    for section in "${section_order[@]}"; do
        echo "[$(printf '%s' "${section_numbers[$section]}")] $section"
        for ((i = 0; i < ${#CHECK_SECTIONS[@]}; i++)); do
            if [[ "${CHECK_SECTIONS[$i]}" == "$section" ]]; then
                local status="${CHECK_STATUSES[$i]}"
                local name="${CHECK_NAMES[$i]}"
                local msg="${CHECK_MESSAGES[$i]}"
                local symbol
                local color
                case "$status" in
                    pass)
                        symbol="✅"
                        color="$COLOR_GREEN"
                        ;;
                    warn)
                        symbol="⚠ "
                        color="$COLOR_YELLOW"
                        ;;
                    fail)
                        symbol="❌"
                        color="$COLOR_RED"
                        ;;
                esac
                if [[ -n "$msg" ]]; then
                    echo "  ${color}${symbol}${COLOR_RESET} ${name} ${COLOR_BLUE}[${msg}]${COLOR_RESET}"
                else
                    echo "  ${color}${symbol}${COLOR_RESET} ${name}"
                fi
            fi
        done
        echo ""
    done

    echo "================================================================"
    echo "  结果: ${PASS_COUNT}/${total} 通过 (${pass_rate}%)"
    if [[ "$WARN_COUNT" -gt 0 ]]; then
        echo "  ${COLOR_YELLOW}⚠ ${WARN_COUNT} 项需业务方接入（见 PRODUCTION_CHECKLIST.md）${COLOR_RESET}"
    fi
    if [[ "$FAIL_COUNT" -gt 0 ]]; then
        echo "  ${COLOR_RED}❌ ${FAIL_COUNT} 项失败（脚手架基线不达标）${COLOR_RESET}"
    fi
    if [[ "$WARN_COUNT" -eq 0 ]] && [[ "$FAIL_COUNT" -eq 0 ]]; then
        echo "  ${COLOR_GREEN}✅ 全部通过${COLOR_RESET}"
    fi
    echo "================================================================"
}

# ============================================================================
# JSON 输出
# ============================================================================
render_json() {
    local total=$((PASS_COUNT + WARN_COUNT + FAIL_COUNT))
    local pass_rate=0
    if [[ "$total" -gt 0 ]]; then
        pass_rate=$((PASS_COUNT * 100 / total))
    fi

    local exit_code=0
    if [[ "$FAIL_COUNT" -gt 0 ]]; then
        exit_code=2
    elif [[ "$WARN_COUNT" -gt 0 ]]; then
        exit_code=1
    fi

    echo "{"
    echo "  \"passed\": ${PASS_COUNT},"
    echo "  \"warned\": ${WARN_COUNT},"
    echo "  \"failed\": ${FAIL_COUNT},"
    echo "  \"total\": ${total},"
    echo "  \"pass_rate\": ${pass_rate},"
    echo "  \"exit_code\": ${exit_code},"
    echo "  \"checks\": ["

    local first=true
    for ((i = 0; i < ${#CHECK_SECTIONS[@]}; i++)); do
        local section="${CHECK_SECTIONS[$i]}"
        local name="${CHECK_NAMES[$i]}"
        local status="${CHECK_STATUSES[$i]}"
        local msg="${CHECK_MESSAGES[$i]}"
        # 转义 JSON 字符串中的特殊字符
        name="${name//\"/\\\"}"
        msg="${msg//\"/\\\"}"
        msg="${msg//\\/\\\\}"
        if [[ "$first" == "true" ]]; then
            first=false
        else
            echo ","
        fi
        echo -n "    {\"section\": \"${section}\", \"name\": \"${name}\", \"status\": \"${status}\""
        if [[ -n "$msg" ]]; then
            echo -n ", \"message\": \"${msg}\""
        fi
        echo -n "}"
    done

    echo ""
    echo "  ]"
    echo "}"
}

# ============================================================================
# 主流程
# ============================================================================
run_all_checks

if [[ "$OUTPUT_FORMAT" == "json" ]]; then
    render_json
else
    render_text
fi

# 退出码：0 全通过 / 1 有 warn / 2 有 fail
if [[ "$FAIL_COUNT" -gt 0 ]]; then
    exit 2
elif [[ "$WARN_COUNT" -gt 0 ]]; then
    exit 1
else
    exit 0
fi

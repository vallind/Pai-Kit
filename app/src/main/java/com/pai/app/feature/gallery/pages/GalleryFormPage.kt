// ============================================================================
// GalleryFormPage.kt
// 表单组件展示页：TextField / TextArea / Segmented / Stepper / Dropdown /
//                 Checkbox / Switch / RadioButton / Slider
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如表单组件间距/高度示例）—— 仅用于表单组件演示，
// 业务代码请用 DSTokens.Spacing / DSTokens.ComponentHeight.*。
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pai.app.core.designsystem.primitives.DSCheckbox
import com.pai.app.core.designsystem.primitives.DSDropdown
import com.pai.app.core.designsystem.primitives.DSRadioButton
import com.pai.app.core.designsystem.primitives.DSSegmentedControl
import com.pai.app.core.designsystem.primitives.DSSlider
import com.pai.app.core.designsystem.primitives.DSSwitch
import com.pai.app.core.designsystem.primitives.DSTextField
import com.pai.app.core.designsystem.primitives.DSTextFieldStyle
import com.pai.app.core.designsystem.primitives.DSTextArea
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.primitives.DSStepper
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryFormPage - 表单组件展示页
 *
 * 内容分八节：
 * 1. DSTextField Outlined 4 状态：label / placeholder / error / password
 * 2. DSTextField Filled 填充式输入框
 * 3. DSTextArea 多行输入 + 字数统计
 * 4. DSSegmentedControl 3 选项分段控制
 * 5. DSStepper 数字步进器
 * 6. DSDropdown 下拉选择器
 * 7. DSCheckbox + DSSwitch + DSRadioButton 选择控件
 * 8. DSSlider 滑块
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryFormPage(onBackClick: () -> Unit) {
    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "表单 Form",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg)
        ) {
            // 1. DSTextField Outlined 4 状态
            SectionCard(title = "DSTextField Outlined 4 种状态") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    // label + placeholder
                    var name by remember { mutableStateOf("") }
                    DSTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "用户名",
                        placeholder = "请输入用户名",
                        leadingIcon = Icons.Default.Person
                    )

                    // error
                    var email by remember { mutableStateOf("invalid") }
                    DSTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "邮箱",
                        isError = true,
                        errorMessage = "邮箱格式不正确",
                        leadingIcon = Icons.Default.Email
                    )

                    // password
                    var pwd by remember { mutableStateOf("password123") }
                    DSTextField(
                        value = pwd,
                        onValueChange = { pwd = it },
                        label = "密码",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock
                    )

                    // 禁用态
                    DSTextField(
                        value = "不可编辑",
                        onValueChange = {},
                        label = "禁用字段",
                        enabled = false
                    )
                }
            }

            // 2. DSTextField Filled
            SectionCard(title = "DSTextField Filled 填充式") {
                var text by remember { mutableStateOf("Hello World") }
                DSTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "姓名",
                    placeholder = "请输入姓名",
                    style = DSTextFieldStyle.Filled
                )
            }

            // 3. DSTextArea 多行输入
            SectionCard(title = "DSTextArea 多行输入 + 字数统计") {
                var comment by remember { mutableStateOf("产品需求：用户希望可以快速输入多行文本，并实时看到字数统计。") }
                DSTextArea(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "评论",
                    placeholder = "说点什么吧...",
                    maxLength = 200,
                    minHeight = DSTokens.Spacing.xxxl
                )
            }

            // 4. DSSegmentedControl 3 选项
            SectionCard(title = "DSSegmentedControl 分段控制") {
                val options = listOf("日", "周", "月")
                var selected by remember { mutableIntStateOf(0) }
                DSSegmentedControl(
                    options = options,
                    selectedIndex = selected,
                    onSelectedChange = { selected = it }
                )
            }

            // 5. DSStepper 数字步进器
            SectionCard(title = "DSStepper 数字步进器") {
                var count by remember { mutableIntStateOf(1) }
                DSStepper(
                    value = count,
                    onValueChange = { count = it },
                    range = 1..99
                )
            }

            // 6. DSDropdown 下拉选择器
            SectionCard(title = "DSDropdown 下拉选择器") {
                val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "成都")
                var selectedIndex by remember { mutableIntStateOf(0) }
                DSDropdown(
                    label = "所在城市",
                    options = cities,
                    selectedIndex = selectedIndex,
                    onSelectedChange = { selectedIndex = it },
                    leadingIcon = Icons.Default.LocationOn
                )
            }

            // 7. 选择控件：Checkbox + Switch + RadioButton
            SectionCard(title = "DSCheckbox / DSSwitch / DSRadioButton") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    var checkboxChecked by remember { mutableStateOf(true) }
                    DSCheckbox(
                        checked = checkboxChecked,
                        onCheckedChange = { checkboxChecked = it },
                        label = "我已阅读并同意用户协议"
                    )

                    var switchChecked by remember { mutableStateOf(false) }
                    DSSwitch(
                        checked = switchChecked,
                        onCheckedChange = { switchChecked = it },
                        label = "接收推送通知"
                    )

                    // RadioButton 单选组
                    val radioOptions = listOf("男", "女", "其他")
                    var radioSelected by remember { mutableStateOf("男") }
                    radioOptions.forEach { option ->
                        DSRadioButton(
                            selected = radioSelected == option,
                            onClick = { radioSelected = option },
                            label = option
                        )
                    }
                }
            }

            // 8. DSSlider 滑块
            SectionCard(title = "DSSlider 滑块") {
                var sliderValue by remember { mutableFloatStateOf(0.5f) }
                DSSlider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..1f
                )
            }
        }
    }
}

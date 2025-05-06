package wx.demo.hook.chat

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DescriptorData
import me.hd.wauxv.databinding.ModuleDialogMsgFormatBinding
import me.hd.wauxv.factory.showDialog
import me.hd.wauxv.factory.toDateStr
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.api.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexConstructor
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.lang.StringBuilder // 引入 StringBuilder

@Obfuscate
@HookAnno
@ViewAnno
object MsgFormatHook : SwitchHook("MsgFormatHook"), IDexFind {
    private object MethodSendTextComponent : DescriptorData("MsgFormatHook.MethodSendTextComponent")
    private object ValMsgFormatTextFormat : DefaultData("MsgFormatHook.ValMsgFormatTextFormat", stringDefVal = TEXT_FORMAT_DEF_VAL)
    private object ValMsgFormatTimeFormat : DefaultData("MsgFormatHook.ValMsgFormatTimeFormat", stringDefVal = TIME_FORMAT_DEF_VAL)

    private val digitMapCircled = mapOf(
        '0' to "₀", '1' to "₁", '2' to "₂", '3' to "₃", '4' to "₄",
        '5' to "₅", '6' to "₆", '7' to "₇", '8' to "₈", '9' to "₉"
    )

    private const val TEXT_FORMAT_DEF_VAL = "\${sendText}\${line}\${sendTime}"
    private const val TIME_FORMAT_DEF_VAL = "yyyy₋MM₋dd HH܄mm܄ss"
    private val availablePlaceholders = listOf(
        "\${sendText}", "\${line}", "\${sendTime}"
    )

    override val location = "聊天"
    override val funcName = "发送文本格式"
    override val funcDesc = "将聊天发送的文本进行自定义格式处理"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogMsgFormatBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogEdtMsgFormatTextFormat.setText(ValMsgFormatTextFormat.stringVal)
        binding.moduleDialogEdtMsgFormatTimeFormat.setText(ValMsgFormatTimeFormat.stringVal)
        binding.moduleDialogEdtMsgFormatTextPlaceholders.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder("文本格式 可用占位符(点击添加)\n").apply {
                availablePlaceholders.forEach { placeholder ->
                    val startOffset = length
                    append("$placeholder ")
                    val endOffset = length - 1
                    setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                val selectionStart = binding.moduleDialogEdtMsgFormatTextFormat.selectionStart
                                val selectionEnd = binding.moduleDialogEdtMsgFormatTextFormat.selectionEnd
                                binding.moduleDialogEdtMsgFormatTextFormat.text?.replace(selectionStart, selectionEnd, placeholder)
                            }
                        }, startOffset, endOffset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        layoutView.context.showDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValMsgFormatTextFormat.stringVal = binding.moduleDialogEdtMsgFormatTextFormat.text.toString()
                ValMsgFormatTimeFormat.stringVal = binding.moduleDialogEdtMsgFormatTimeFormat.text.toString()
            }
            neutralButton("重置") {
                ValMsgFormatTextFormat.stringVal = TEXT_FORMAT_DEF_VAL
                ValMsgFormatTimeFormat.stringVal = TIME_FORMAT_DEF_VAL
            }
            negativeButton()
        }
    }
    
    // --- 新增：数字替换辅助函数 ---
    /**
     * 将输入字符串中的数字根据提供的映射表进行替换
     * @param input 原始字符串 (例如格式化后的时间)
     * @param digitMap 数字字符到替换字符串的映射
     * @return 替换数字后的字符串
     */
    private fun replaceDigits(input: String, digitMap: Map<Char, String>): String {
        val result = StringBuilder(input.length) // 预估长度，提高效率
        for (char in input) {
            // 尝试从映射表中获取当前字符的替换项
            // 如果 char 是数字且在 map 的 key 中，则 replacement 不为 null
            val replacement = digitMap[char]
            if (replacement != null) {
                // 如果找到了替换项，则追加替换后的字符串
                result.append(replacement)
            } else {
                // 如果不是需要替换的数字，或者映射表中没有定义，则追加原始字符
                result.append(char)
            }
        }
        return result.toString()
    }

    private fun formatMsg(msg: String): String {
        // 1. 获取当前时间戳
        val currentTimeMillis = System.currentTimeMillis()
        // 2. 使用用户配置的格式，格式化时间 (得到标准时间字符串)
        val standardFormattedTime = currentTimeMillis.toDateStr(ValMsgFormatTimeFormat.stringVal)

        // --- 新增：应用自定义数字替换 ---
        // 在这里选择你想要使用的替换规则 (digitMap)
        // 你也可以根据需要添加配置项来让用户选择使用哪个 map
        val activeDigitMap = digitMapCircled // 或者使用 digitMapChinese
        val customFormattedTime = replaceDigits(standardFormattedTime, activeDigitMap)
        // --- 新增结束 ---

        // 3. 使用 *替换数字后* 的时间字符串，替换占位符
        return ValMsgFormatTextFormat.stringVal
            .replace("\${sendText}", msg)
            .replace("\${line}", "\n")
            .replace("\${sendTime}", customFormattedTime) // 使用 customFormattedTime
    }
    
    override fun initOnce() {
        MethodSendTextComponent.toDexConstructor {
            hook {
                beforeIfEnabled {
                    val originalText = args(8).string()
                    args(8).set(formatMsg(originalText))
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodSendTextComponent.findDexClassMethod(dexKit) {
            onClass {
                matcher {
                    usingEqStrings("MicroMsg.ChattingUI.SendTextComponent", "doSendMessage begin send txt msg")
                }
            }
            onMethod {
                matcher {
                    paramCount = 13
                }
            }
        }
    }
}

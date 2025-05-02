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

@Obfuscate
@HookAnno
@ViewAnno
object MsgFormatHook : SwitchHook("MsgFormatHook"), IDexFind {
    private object MethodSendTextComponent : DescriptorData("MsgFormatHook.MethodSendTextComponent")
    private object ValMsgFormatTextFormat : DefaultData("MsgFormatHook.ValMsgFormatTextFormat", stringDefVal = TEXT_FORMAT_DEF_VAL)
    private object ValMsgFormatTimeFormat : DefaultData("MsgFormatHook.ValMsgFormatTimeFormat", stringDefVal = TIME_FORMAT_DEF_VAL)

    private const val TEXT_FORMAT_DEF_VAL = "\${sendText}\${line}\${sendTime}"
    private const val TIME_FORMAT_DEF_VAL = "yyyy-MM-dd HH:mm:ss"
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

    private fun formatMsg(msg: String): String {
        return ValMsgFormatTextFormat.stringVal
            .replace("\${sendText}", msg)
            .replace("\${line}", "\n")
            .replace("\${sendTime}", System.currentTimeMillis().toDateStr(ValMsgFormatTimeFormat.stringVal))
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

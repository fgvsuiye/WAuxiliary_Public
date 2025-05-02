package wx.demo.hook.helper

import android.view.LayoutInflater
import android.view.View
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.type.java.IntType
import me.hd.wauxv.data.config.DefaultData
import me.hd.wauxv.data.config.DescriptorData
import me.hd.wauxv.data.factory.HostInfo
import me.hd.wauxv.data.factory.WxVersion
import me.hd.wauxv.databinding.ModuleDialogVoiceLengthBinding
import me.hd.wauxv.factory.showDialog
import me.hd.wauxv.hook.anno.HookAnno
import me.hd.wauxv.hook.anno.ViewAnno
import me.hd.wauxv.hook.base.SwitchHook
import me.hd.wauxv.hook.core.api.IDexFind
import me.hd.wauxv.hook.factory.findDexClassMethod
import me.hd.wauxv.hook.factory.toDexMethod
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
@HookAnno
@ViewAnno
object VoiceLengthHook : SwitchHook("VoiceLengthHook"), IDexFind {
    private object MethodSetVoice : DescriptorData("VoiceLengthHook.MethodSetVoice")
    private object ValVoiceLength : DefaultData("VoiceLengthHook.ValVoiceLength", intDefVal = 1)

    override val location = "辅助"
    override val funcName = "语音时长"
    override val funcDesc = "可自定义修改发送的语音消息显示时长"
    override var onClick: ((View) -> Unit)? = { layoutView ->
        val binding = ModuleDialogVoiceLengthBinding.inflate(LayoutInflater.from(layoutView.context))
        binding.moduleDialogSliderVoiceLength.value = ValVoiceLength.intVal.toFloat()
        layoutView.context.showDialog {
            title = funcName
            view = binding.root
            positiveButton("保存") {
                ValVoiceLength.intVal = binding.moduleDialogSliderVoiceLength.value.toInt()
            }
            negativeButton()
        }
    }
    override val isAvailable = HostInfo.verCode > WxVersion.V8_0_30.code

    override fun initOnce() {
        MethodSetVoice.toDexMethod {
            hook {
                beforeIfEnabled {
                    val objIndex = when {
                        args.size == 1 -> 0
                        args.size == 2 && args[0] is String -> 1
                        else -> return@beforeIfEnabled
                    }
                    val obj = args(objIndex).any()!!
                    val voiceLengthFieldName = "l"
                    obj::class.java.field {
                        name = voiceLengthFieldName
                        type = IntType
                    }.get(obj).set(ValVoiceLength.intVal * 1000)
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodSetVoice.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    usingEqStrings("MicroMsg.VoiceStorage", "update failed, no values set")
                }
            }
        }
    }
}

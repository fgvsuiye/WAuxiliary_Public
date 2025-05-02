package wx.demo.hook.limit

import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import me.hd.wauxv.data.config.DescriptorData
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
object RemoveVoipLimitHook : SwitchHook("RemoveVoipLimitHook"), IDexFind {
    private object MethodIsVoiceUsing : DescriptorData("RemoveVoipLimitHook.MethodIsVoiceUsing")
    private object MethodIsMultiTalking : DescriptorData("RemoveVoipLimitHook.MethodIsMultiTalking")
    private object MethodMarkCheckAppBrand : DescriptorData("RemoveVoipLimitHook.MethodMarkCheckAppBrand")
    private object MethodIsCameraUsing : DescriptorData("RemoveVoipLimitHook.MethodIsCameraUsing")

    override val location = "限制"
    override val funcName = "移除通话限制"
    override val funcDesc = "将通话中无法播放及拍摄视频限制移除"

    override fun initOnce() {
        listOf(MethodIsVoiceUsing, MethodIsMultiTalking, MethodMarkCheckAppBrand, MethodIsCameraUsing).forEach { descData ->
            descData.toDexMethod {
                hook {
                    beforeIfEnabled {
                        resultFalse()
                    }
                }
            }
        }
    }

    override fun dexFind(dexKit: DexKitBridge) {
        MethodIsVoiceUsing.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    paramTypes(ContextClass)
                    usingEqStrings("MicroMsg.DeviceOccupy", "isVoiceUsing")
                }
            }
        }
        MethodIsMultiTalking.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    paramTypes(ContextClass)
                    usingEqStrings("MicroMsg.DeviceOccupy", "isMultiTalking")
                }
            }
        }
        MethodMarkCheckAppBrand.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    paramTypes(ContextClass)
                    usingEqStrings("MicroMsg.DeviceOccupy", "checkAppBrandVoiceUsingAndShowToast isVoiceUsing:%b, isCameraUsing:%b")
                }
            }
        }
        MethodIsCameraUsing.findDexClassMethod(dexKit) {
            onMethod {
                matcher {
                    paramTypes(ContextClass, BooleanType, BundleClass)
                    usingEqStrings("MicroMsg.DeviceOccupy", "isCameraUsing")
                }
            }
        }
    }
}

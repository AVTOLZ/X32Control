package dev.tiebe.avt.x32.api.internal

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.channelIndex
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.fader.channel.Channel
import dev.tiebe.avt.x32.api.fader.dca.DCA
import dev.tiebe.avt.x32.api.getFaderFromIndex

class Status private constructor(private val osc: OSCController) {

    fun setLock(state: Boolean) {
        osc.sendMessage(OSCMessage("/-stat/lock", listOf(if (state) 1 else 0)))
    }

    suspend fun getLock(): Boolean {
        val value = osc.getValue(OSCMessage("/-stat/lock")) ?: throw IllegalStateException("Could not get lock")

        return value.arguments[0] as Int == 1
    }

    fun setScreen(screen: Screen) {
        if (screen == Screen.LOCK)
            throw IllegalArgumentException("Screen cannot be set to LOCK")

        osc.sendMessage(OSCMessage("/-stat/screen", listOf(screen.id)))
    }

    suspend fun getScreen(): Screen {
        val value = osc.getValue(OSCMessage("/-stat/screen")) ?: throw IllegalStateException("Could not get screen")

        return Screen.entries[value.arguments[0] as Int]
    }

    fun setSelection(fader: Fader) {
        if (fader is DCA)
            throw IllegalArgumentException("DCA cannot be selected")

        osc.sendMessage(OSCMessage("/-stat/selidx", listOf(fader.channelIndex)))
    }

    suspend fun getSelection(): Fader {
        val value = osc.getValue(OSCMessage("/-stat/selidx")) ?: throw IllegalStateException("Could not get selection")

        return osc.getFaderFromIndex(value.arguments[0] as String)
    }

    fun setChannelFaderBank(bank: ChannelFaderBank) {
        osc.sendMessage(OSCMessage("/-stat/chfaderbank", listOf(bank.value)))
    }

    suspend fun getChannelFaderBank(): ChannelFaderBank {
        val value = osc.getValue(OSCMessage("/-stat/chfaderbank")) ?: throw IllegalStateException("Could not get channel fader bank")

        return ChannelFaderBank.entries[value.arguments[0] as Int]
    }

    fun setGroupFaderBank(bank: GroupFaderBank) {
        osc.sendMessage(OSCMessage("/-stat/grpfaderbank", listOf(bank.value)))
    }

    suspend fun getGroupFaderBank(): GroupFaderBank {
        val value = osc.getValue(OSCMessage("/-stat/grpfaderbank")) ?: throw IllegalStateException("Could not get group fader bank")

        return GroupFaderBank.entries[value.arguments[0] as Int]
    }

    fun setSendsOnFader(state: Boolean) {
        osc.sendMessage(OSCMessage("/-stat/sendsonfader", listOf(if (state) 1 else 0)))
    }

    suspend fun getSendsOnFader(): Boolean {
        val value = osc.getValue(OSCMessage("/-stat/sendsonfader")) ?: throw IllegalStateException("Could not get sends on fader")

        return value.arguments[0] as Int == 1
    }

    suspend fun getUSBMounted(): Boolean {
        val value = osc.getValue(OSCMessage("/-stat/usbmounted")) ?: throw IllegalStateException("Could not get USB mounted")

        return value.arguments[0] as Int == 1
    }


    companion object {
        @Volatile private var INSTANCES: MutableMap<OSCController, Status> = mutableMapOf()

        fun getInstance(osc: OSCController): Status {
            if (!INSTANCES.containsKey(osc)) {
                INSTANCES[osc] = Status(osc)
            }

            return INSTANCES[osc]!!
        }

        enum class ChannelFaderBank(val value: Int) {
            CH1_16(0),
            CH17_32(1),
            AUX_USB_FX(2),
            BUS_MASTER(3)
        }

        enum class GroupFaderBank(val value: Int) {
            DCA(0),
            BUS1_8(1),
            BUS9_16(2),
            MATRIX(3)
        }
    }

}
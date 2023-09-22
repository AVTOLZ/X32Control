package dev.tiebe.avt.osc.x32.api.fader

import dev.tiebe.avt.osc.x32.api.fader.channel.Channel
import kotlinx.coroutines.runBlocking

data class FaderSettings(
    val gain: Float,
    val muteState: Boolean,
    val soloState: Boolean,
    val stereoState: Boolean,
    val monoState: Boolean,
    val color: Pair<Color, Boolean>,
    val name: String,
    val source: Int?,
    val icon: Icon,
) {
    companion object {
        fun fromFader(fader: Fader): FaderSettings {
            return runBlocking {
                return@runBlocking FaderSettings(
                    fader.mix.getLevel(),
                    fader.mix.getMute(),
                    fader.config.getSolo(),
                    fader.mix.getStereo(),
                    fader.mix.getMono(),
                    fader.config.getColor(),
                    fader.config.getName(),
                    if (fader is Channel) fader.config.getSource() else null,
                    fader.config.getIcon()
                )
            }
        }
    }

    fun applyTo(fader: Fader) {
        fader.mix.setLevel(gain)
        fader.mix.setMute(muteState)
        fader.config.setSolo(soloState)
        fader.mix.setStereo(stereoState)
        fader.mix.setMono(monoState)
        fader.config.setColor(color.first, color.second)
        fader.config.setName(name)
        if (fader is Channel && source != null) fader.config.setSource(source)
        fader.config.setIcon(icon)
    }

    fun forceApplyTo(fader: Fader) {
        fader.mix.setLevel(gain, true)
        fader.mix.setMute(muteState, true)
        fader.config.setSolo(soloState, true)
        fader.mix.setStereo(stereoState, true)
        fader.mix.setMono(monoState, true)
        fader.config.setColor(color.first, color.second, true)
        fader.config.setName(name, true)
        if (fader is Channel && source != null) fader.config.setSource(source, true)
        fader.config.setIcon(icon, true)
    }
}
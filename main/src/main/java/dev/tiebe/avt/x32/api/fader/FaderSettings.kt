package dev.tiebe.avt.x32.api.fader

import kotlinx.coroutines.runBlocking

data class FaderSettings(
    val gain: Float,
    val muteState: Boolean,
    val soloState: Boolean,
    val stereoState: Boolean,
    val monoState: Boolean,
    val color: Pair<Color, Boolean>,
    val name: String,
    val source: Int,
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
                    fader.config.getSource(),
                    fader.config.getIcon()
                )
            }
        }
    }

    fun applyTo(fader: Fader) {
        runBlocking {
            fader.mix.setLevel(gain)
            fader.mix.setMute(muteState)
            fader.config.setSolo(soloState)
            fader.mix.setStereo(stereoState)
            fader.mix.setMono(monoState)
            fader.config.setColor(color.first, color.second)
            fader.config.setName(name)
            fader.config.setSource(source)
            fader.config.setIcon(icon)
        }
    }
}
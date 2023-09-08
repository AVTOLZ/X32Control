@file:Suppress("unused")

package dev.tiebe.avt.x32.api.fader

/*
Regex: (\d+?(?= )) (.+)
Replace: \U$2\E($1),
*/

enum class Icon(val value: Int) {
    BLANK(1),
    KICK_BACK(2),
    KICK_FRONT(3),
    SNARE_TOP(4),
    SNARE_BOTTOM(5),
    TOM_HIGH(6),
    TOM_MEDIUM(7),
    TOM(8),
    CHARLEY(9),
    CRASH(10),
    DRUMS(11),
    BELL(12),
    CONGAS_1(13),
    CONGAS_2(14),
    TAMBOURINE(15),
    XYLOPHONE(16),
    ELEC_BASS(17),
    ACOU_BASS_1(18),
    ACOU_BASS_2(19),
    ELEC_GUIT_1(20),
    ELEC_GUIT_2(21),
    ELEC_GUIT_3(22),
    ACOU_GUIT(23),
    AMP_1(24),
    AMP_2(25),
    AMP_3(26),
    ACOU_PIANO(27),
    ORGAN(28),
    ELEC_KEY_1(29),
    ELEC_KEY_2(30),
    SYNTH_1(31),
    SYNT_2(32),
    SYNTH_3(33),
    SYNTH_4(34),
    TRUMPET(35),
    TROMBONE(36),
    SAX(37),
    CLARINETTE(38),
    VIOLIN(39),
    CELLO(40),
    MALE_SINGER(41),
    FEMALE_SINGER(42),
    CHOIR(43),
    HAND_SIGN(44),
    TALK_A(45),
    TALK_B(46),
    MIC_1(47),
    C_MIC_LEFT(48),
    C_MIC_RIGHT(49),
    MIC_2(50),
    WIRELESS_MIC(51),
    TABLE_MIC(52),
    IN_EAR(53),
    XLR(54),
    TRS(55),
    TRS_LEFT(56),
    TRS_RIGHT(57),
    CINCH_LEFT(58),
    CINCH_RIGHT(59),
    TAPE_RECORDER(60),
    FX(61),
    PC(62),
    WEDGE(63),
    SPEAKER_RIGHT(64),
    SPEAKER_LEFT(65),
    SPEAKER_ARRAY(66),
    SPEAKER_STAND(67),
    RACK(68),
    CONTROLS(69),
    FADERS(70),
    ROUTING_MAIN(71),
    ROUTING_BUS(72),
    ROUTING_DISPATCH(73),
    SMILEY(74)
}


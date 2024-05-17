package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import ua.syt0r.kanji.core.app_data.data.JapaneseWord

data class VocabPracticeSet(
    val title: String,
    val expressionIds: List<Long>
)

sealed interface VocabPracticePreviewState {
    object Loading : VocabPracticePreviewState
    data class Loaded(val words: List<JapaneseWord>) : VocabPracticePreviewState
}

val vocabSets = listOf(
    VocabPracticeSet(
        title = "Grammar Terms",
        expressionIds = listOf(
            1531570,
            1451380,
            1250430
        )
    ),
    VocabPracticeSet(
        title = "Animals",
        expressionIds = listOf(
            1451470,
            1467640,
            1258330,
            1430250,
            1471560,
            1231490,
            1578010,
            1351830,
            1246850,
            1252990,
            1430250,
            1443970,
            1267340,
            1560940,
            1397840,
            1267610,
            1319030
        )
    ),
    VocabPracticeSet(
        title = "Stores & Establishments",
        expressionIds = listOf(
            1066710,
            1053280,
            1145310,
            1038050,
            1538200,
            1522240,
            1243490,
            1542430,
            1486680,
            1490220,
            1173750,
            1040260,
            1083590,
            1103480,
            1194560
        )
    ),
    VocabPracticeSet(
        title = "Cities",
        expressionIds = listOf(
            1447690,
            1652350,
            2078800,
            2164660,
            2164610,
            2827768,
            2164670,
            1182580,
            2164640,
            2164620,
            2845104,
            2827769,
            2770680
        )
    ),
    VocabPracticeSet(
        title = "Transport",
        expressionIds = listOf(
            1443530,
            1098390,
            1076190,
            1318290,
            1485470,
            1361590,
            1420900,
            1602800,
            1135680,
            1068190,
            1097810,
            1175140,
            1435080,
            1245570
        )
    ),
    VocabPracticeSet(
        title = "Apartments",
        expressionIds = listOf(
            1130040,
            1017760,
            1432030,
            1192270,
            1557500,
            1497040,
            1215490,
            1263400,
            1412640,
            1142410,
            1562110,
            1545970,
            1360140,
            1547460,
            1084810,
            1119940,
            1099900,
            1027180,
            1040060,
            1371920,
            1443000,
            1022880,
            1032260
        )
    )
)

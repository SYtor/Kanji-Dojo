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
        title = "Time",
        expressionIds = listOf(
            1315920,
            1315840,
            2020680,
            1502840,
            1502860,
            1490430,
            1579110,
            1584660,
            1579260,
            1576050,
            1584640,
            1594050,
            1333450,
            1255430,
            1468060,
            2084840,
            1316220,
            1288850,
            1528060,
            1196030,
            1269060,
            1268990,
            1428280,
            1426250,
            1542790,
            1536350,
            1362810,
            1333520,
            1295310,
            1507720,
            1289220,
            1548010
        )
    ),
    VocabPracticeSet(
        title = "Week Days",
        expressionIds = listOf(
            1545770, 1255890, 1194290, 1372190, 1534890, 1243320, 1445590, 1464900
        )
    ),
    VocabPracticeSet(
        title = "Common Verbs",
        expressionIds = listOf(
            1578850,
            1547720,
            1259290,
            1591110,
            1562350,
            1456360,
            1343950,
            1358280,
            1169870,
            1360010,
            1291800,
            1597040,
            1514320,
            1174340,
            1202450,
            1202460,
            1508590,
            1597890,
            1473740,
            1473950,
            1305990
        )
    ),
    VocabPracticeSet(
        title = "Colors",
        expressionIds = listOf(
            2013900,
            1381380,
            1555300,
            1576760,
            1474900,
            1287410,
            1107140,
            1311640,
            1422720,
            1201970,
            1035860,
            1371670,
            1578120,
            1243560
        )
    ),
    VocabPracticeSet(
        title = "Regular Food",
        expressionIds = listOf(
            1270590,
            1103090,
            1463520,
            1578010,
            1537370,
            1193060,
            1549140,
            1231590,
            1077330,
            1098620,
            1450070,
            1595020,
            1291600,
            1576630,
            1538590,
            1533610,
            1508750,
            1253020,
            1457440,
            1231580,
            1005930,
            1367800,
            1240670,
            1370860
        )
    ),
    VocabPracticeSet(
        title = "Japanese Food",
        expressionIds = listOf(
            1595650,
            1440590,
            1238460,
            1574470,
            1527050,
            1001620,
            1137720,
            1605410,
            1596920,
            1172510,
            1005660,
            1474440,
            1306570,
            1610810,
            1001890,
            1629600,
            1422890,
            1470070,
            1001390,
            1037740,
            1590640
        )
    ),
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
        "Body",
        listOf(
            1582310,
            1217730,
            1604890,
            1317170,
            1486720,
            1275640,
            1313000,
            1592270,
            1258950,
            1237820,
            1562850,
            1327190,
            1309650,
            1472800,
            1002610,
            1404630,
            1487320,
            1404840,
            1477950,
            1207560,
            1584160,
            1584690,
            1207510,
            1387010
        )
    ),
    VocabPracticeSet(
        title = "Common Places",
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
            1194560,
            1246720,
            1600440,
            1231800,
            1273270,
            1451490,
            1486460,
            1474720,
            1308300,
            1488090,
            1175140,
            1245570,
            1279990,
            1454290,
            1237410,
            1302680,
            1201190,
            1390020,
            1267280,
            1183450
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
            1323080,
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
    )
)

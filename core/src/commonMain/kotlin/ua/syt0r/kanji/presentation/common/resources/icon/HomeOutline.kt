package ua.syt0r.kanji.presentation.common.resources.icon

import androidx.compose.material.icons.materialIcon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.path

val ExtraIcons.HomeOutline by lazy {
    materialIcon("HomeOutline") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 3f)
            lineTo(4f, 9f)
            verticalLineTo(21f)
            horizontalLineTo(10f)
            verticalLineTo(14f)
            horizontalLineTo(14f)
            verticalLineTo(21f)
            horizontalLineTo(20f)
            verticalLineTo(9f)
            close()
        }
    }
}
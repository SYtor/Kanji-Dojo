package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.MutableState
import kotlinx.datetime.LocalDateTime
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncDataDiffType

sealed interface SyncDialogState {

    object Hidden : SyncDialogState

    object Uploading : SyncDialogState
    object Downloading : SyncDialogState

    data class Conflict(
        val diffType: SyncDataDiffType,
        val remoteDataTime: LocalDateTime?,
        val lastSyncTime: LocalDateTime?
    ) : SyncDialogState

    sealed interface Error : SyncDialogState {

        val showDialog: MutableState<Boolean>

        data class Unsupported(
            override val showDialog: MutableState<Boolean>
        ) : Error

        data class Api(
            override val showDialog: MutableState<Boolean>,
            val issue: ApiRequestIssue
        ) : Error

    }

}
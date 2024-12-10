package ua.syt0r.kanji.presentation.screen.main

import kotlinx.datetime.LocalDateTime
import ua.syt0r.kanji.core.ApiRequestIssue

sealed interface SyncDialogState {

    object Hidden : SyncDialogState

    object Uploading : SyncDialogState
    object Downloading : SyncDialogState

    data class Conflict(
        val remoteDataTime: LocalDateTime?,
        val lastSyncTime: LocalDateTime?
    ) : SyncDialogState

    object Unsupported : SyncDialogState

    sealed interface Error : SyncDialogState {
        data class Api(val issue: ApiRequestIssue) : Error
    }

}
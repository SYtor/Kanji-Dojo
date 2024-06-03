package ua.syt0r.kanji.core

sealed interface RefreshableData<T> {
    class Loading<T> : RefreshableData<T>
    data class Loaded<T>(val value: T) : RefreshableData<T>
}
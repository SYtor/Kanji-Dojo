package ua.syt0r.kanji.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.Module
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface AccountManager {
    val state: StateFlow<AccountState>
    fun signIn(refreshToken: String, idToken: String)
    fun signOut()
    fun refreshUserData()
}

sealed interface SubscriptionInfo {
    object Inactive : SubscriptionInfo
    data class Active(val due: LocalDateTime) : SubscriptionInfo
    data class Expired(val due: LocalDateTime) : SubscriptionInfo
}

sealed interface AccountState {

    object Loading : AccountState

    object LoggedOut : AccountState

    data class LoggedIn(
        val email: String,
        val subscriptionInfo: SubscriptionInfo
    ) : AccountState

    data class Error(
        val throwable: Throwable
    ) : AccountState

}

fun Module.addAccountDefinitions() {

    single<AccountManager> {
        DefaultAccountManager(
            coroutineScope = CoroutineScope(Dispatchers.IO),
            appPreferences = get(),
            networkApi = get(),
            timeUtils = get()
        )
    }

}

class DefaultAccountManager(
    private val coroutineScope: CoroutineScope,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkApi: NetworkApi,
    private val timeUtils: TimeUtils
) : AccountManager {

    private val _state = MutableStateFlow<AccountState>(AccountState.Loading)
    override val state: StateFlow<AccountState> = _state

    init {
        _state.launchWhenHasSubscribers(coroutineScope, ::refreshFromLocal)
    }

    override fun signIn(refreshToken: String, idToken: String) {
        _state.value = AccountState.Loading
        coroutineScope.launch {

            appPreferences.refreshToken.set(refreshToken)
            appPreferences.idToken.set(idToken)

            refreshUserData()

        }

    }

    override fun signOut() {
        _state.value = AccountState.LoggedOut
        coroutineScope.launch {
            appPreferences.apply {
                refreshToken.set(null)
                idToken.set(null)
                userEmail.set(null)
                subscriptionDue.set(null)
            }
        }
    }

    override fun refreshUserData() {
        coroutineScope.launch {
            val userInfo = networkApi.getUserInfo().getOrElse {
                _state.value = AccountState.Error(it)
                return@launch
            }

            val subscriptionDue = userInfo.subscriptionDue
                ?.let { Instant.fromEpochMilliseconds(it) }

            appPreferences.userEmail.set(userInfo.email)
            appPreferences.subscriptionDue.set(subscriptionDue)

            _state.value = AccountState.LoggedIn(
                email = userInfo.email,
                subscriptionInfo = getSubInfo(userInfo.subscription, subscriptionDue)
            )
        }
    }

    private fun refreshFromLocal() {
        coroutineScope.launch {
            _state.value = appPreferences.run {
                val email = appPreferences.userEmail.get()
                val subscriptionDue = appPreferences.subscriptionDue.get()

                if (email == null)
                    return@run AccountState.LoggedOut

                AccountState.LoggedIn(
                    email = email,
                    subscriptionInfo = getSubInfo(true, subscriptionDue) // TODO isActive -> status
                )
            }
        }
    }

    private fun getSubInfo(isActive: Boolean, due: Instant?): SubscriptionInfo {
        return when {
            !isActive -> SubscriptionInfo.Inactive
            due!! >= timeUtils.now() -> SubscriptionInfo.Active(
                due = due.toLocalDateTime(TimeZone.currentSystemDefault())
            )

            else -> SubscriptionInfo.Expired(
                due = due.toLocalDateTime(TimeZone.currentSystemDefault())
            )
        }
    }

}
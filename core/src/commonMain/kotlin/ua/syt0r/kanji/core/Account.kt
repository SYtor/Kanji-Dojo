package ua.syt0r.kanji.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.koin.core.module.Module
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesUserInfo

interface AccountManager {
    val state: StateFlow<AccountState>
    fun signIn(refreshToken: String, idToken: String)
    fun signOut()
    fun refreshUserData()
    fun notifyNoSubscription()
    fun notifyAuthExpired()
}

sealed interface AccountState {

    object Loading : AccountState

    object LoggedOut : AccountState

    data class LoggedIn(
        val email: String,
        val subscriptionInfo: SubscriptionInfo,
        val issue: ApiRequestIssue?
    ) : AccountState

    data class Error(
        val issue: ApiRequestIssue
    ) : AccountState

}

sealed interface SubscriptionInfo {
    object Inactive : SubscriptionInfo
    data class Active(val due: LocalDateTime) : SubscriptionInfo
    data class Expired(val due: LocalDateTime) : SubscriptionInfo
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
        _state.launchWhenHasSubscribers(coroutineScope) { updateStateFromLocal() }
    }

    override fun signIn(refreshToken: String, idToken: String) {
        _state.value = AccountState.Loading
        coroutineScope.launch {
            appPreferences.refreshToken.set(refreshToken)
            appPreferences.idToken.set(idToken)
            updateStateFromRemote()
        }
    }

    override fun signOut() {
        _state.value = AccountState.LoggedOut
        coroutineScope.launch { clearUserData() }
    }

    override fun refreshUserData() {
        _state.value = AccountState.Loading
        coroutineScope.launch { updateStateFromRemote() }
    }

    override fun notifyNoSubscription() {
        coroutineScope.launch { updateStateFromLocal(ApiRequestIssue.NoSubscription) }
    }

    override fun notifyAuthExpired() {
        coroutineScope.launch { updateStateFromLocal(ApiRequestIssue.NotAuthenticated) }
    }

    private suspend fun updateStateFromRemote() {
        val apiUserInfo = networkApi.getUserInfo().getOrElse {
            updateStateFromLocal(ApiRequestIssue.classify(it))
            return
        }

        val userInfo = apiUserInfo.toPreferencesType()
        appPreferences.userInfo.set(userInfo)

        updateStateFromLocal()
    }

    private suspend fun updateStateFromLocal(issue: ApiRequestIssue? = null) {
        val userInfo = appPreferences.userInfo.get()

        _state.value = when {
            userInfo != null -> AccountState.LoggedIn(
                email = userInfo.email,
                subscriptionInfo = getSubscriptionInfo(userInfo.subscriptionDue),
                issue = issue
            )

            issue != null -> AccountState.Error(issue)

            else -> AccountState.LoggedOut
        }
    }

    private fun getSubscriptionInfo(dueMillis: Long?): SubscriptionInfo {
        val due = dueMillis?.let { Instant.fromEpochMilliseconds(it) }
        return when {
            due == null -> SubscriptionInfo.Inactive
            due >= timeUtils.now() -> SubscriptionInfo.Active(due.toLocalDateTime())
            else -> SubscriptionInfo.Expired(due.toLocalDateTime())
        }
    }

    private fun ApiUserInfo.toPreferencesType(): PreferencesUserInfo = PreferencesUserInfo(
        email = email,
        subscriptionDue = subscriptionDue,
        updateTimestamp = timeUtils.now().toEpochMilliseconds()
    )

    private suspend fun clearUserData() {
        appPreferences.apply {
            refreshToken.set(null)
            idToken.set(null)
            userInfo.set(null)
        }
    }

}
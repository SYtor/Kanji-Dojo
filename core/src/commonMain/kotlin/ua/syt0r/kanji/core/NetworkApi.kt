package ua.syt0r.kanji.core

object NetworkApi {

    object Url {

        private const val BASE = "http://localhost:8080"

        const val GET_SUBSCRIPTION_INFO = "$BASE/getSubscriptionInfo"
        const val GET_BACKUP_INFO = "$BASE/getBackupInfo"
        const val GET_BACKUP = "$BASE/getBackup"
        const val UPDATE_BACKUP = "$BASE/updateBackup"


        private const val FIREBASE_KEY = "AIzaSyCP9IzlOBkf9C6VHXBsD7xJr88R-ZOUKsA"
        const val REFRESH_AUTH_TOKEN =
            "https://securetoken.googleapis.com/v1/token?key=$FIREBASE_KEY"

    }

}
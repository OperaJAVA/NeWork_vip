package ru.netology.nework.error

import android.database.SQLException
import ru.netology.nework.R
import java.io.IOException

@Suppress("UNUSED_PARAMETER")
sealed class AppError(var code: String, errorNetwork: Int) : RuntimeException() {

    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code, R.string.error_network)
class ApiError400(code: String) : AppError(code, R.string.error_network)
class ApiError403(code: String) : AppError(code, R.string.error_network)
class ApiError404(code: String) : AppError(code, R.string.error_network)
class ApiError415(code: String) : AppError(code, R.string.error_network)

// Изменила эти строки, чтобы использовать идентификаторы ресурсов
data object NetworkError : AppError("error_network", R.string.error_network)
data object DbError : AppError("error_db", R.string.error_db)
data object UnknownError : AppError("error_unknown", R.string.error_unknown)
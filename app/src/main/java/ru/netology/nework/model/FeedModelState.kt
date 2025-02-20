package ru.netology.nework.model

sealed class FeedModelState {
    object Loading : FeedModelState() // Состояние загрузки
    object Error : FeedModelState() // Общее состояние ошибки
    object NotFound : FeedModelState() // Запись не найдена
    object Unauthorized : FeedModelState() // Ошибка авторизации
    object BadRequest : FeedModelState() // Ошибка 400
    object UnsupportedMediaType : FeedModelState() // Ошибка 415
    object NetworkError : FeedModelState() // Ошибка сети
    object Refreshing : FeedModelState() // Состояние обновления
    object AuthStatus : FeedModelState() // Статус аутентификации
    data class Success<T>(val data: T) : FeedModelState() // Успешный ответ с данными
    data class State(
        val loading: Boolean,
        val error400: Boolean = false,
        val error404: Boolean = false,
        val error: Boolean = false
    ) : FeedModelState() // Состояние с флагом загрузки и ошибками
}
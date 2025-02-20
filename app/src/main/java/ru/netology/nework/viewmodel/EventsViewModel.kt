package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.media.Media
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.repository.PostsRepository
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class EventsViewModel @Inject constructor(
    private val repositoryEvents: EventsRepository,
    private val repositoryPosts: PostsRepository
) : ViewModel() {

    val events = repositoryEvents.eventsDb
        .map { event ->
            event.map {
                it.copy(eventOwner = it.authorId == myID)
            }
        }
        .cachedIn(viewModelScope)
        .flowOn(Dispatchers.Default)

    val receivedEvents: LiveData<List<Event>>
        get() = repositoryEvents.eventsFlow.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    init {
        getEvents()
    }

    private fun getEvents() {
        viewModelScope.launch {
            repositoryEvents.getEventsDB()
        }
    }

    fun likeEvent(event: Event, like: Boolean) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repositoryEvents.likeEvent(event.id!!, like)
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }
                    is ru.netology.nework.error.ApiError404 -> {
                        _dataState.value = FeedModelState.NotFound // Не найдено
                    }
                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun saveEvent(event: Event, media: MultipartBody.Part?, typeAttach: AttachmentType?) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                if (typeAttach != null && media != null) {
                    val _media: Media = repositoryPosts.upload(media)
                    val eventWithAttachment =
                        event.copy(attachment = Attachment(_media.url, typeAttach))
                    repositoryEvents.saveEvent(eventWithAttachment)
                } else {
                    repositoryEvents.saveEvent(event)
                }
                _dataState.value = FeedModelState.Success(false) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }
                    is ru.netology.nework.error.ApiError415 -> {
                        _dataState.value = FeedModelState.UnsupportedMediaType // Ошибка 415
                    }
                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun removeEvent(event: Event) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repositoryEvents.deleteEvent(event)
                _dataState.value = FeedModelState.Success(false) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }
                    is ru.netology.nework.error.ApiError404 -> {
                        _dataState.value = FeedModelState.NotFound // Не найдено
                    }
                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun participateEvent(event: Event, status: Boolean) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repositoryEvents.participateEvent(event.id!!, status)
                _dataState.value = FeedModelState.Success(false) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }
                    is ru.netology.nework.error.ApiError404 -> {
                        _dataState.value = FeedModelState.NotFound // Не найдено
                    }
                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }
}
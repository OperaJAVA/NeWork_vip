package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Event
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.ApiError403
import ru.netology.nework.error.ApiError404
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import java.io.IOException
import javax.inject.Inject
import android.util.Log
import retrofit2.Response

class EventsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val daoEvents: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : EventsRepository {

    private val _eventsFlow = MutableStateFlow(emptyList<Event>())
    override val eventsFlow: Flow<List<Event>>
        get() = _eventsFlow.asStateFlow()

    @OptIn(ExperimentalPagingApi::class)
    override val eventsDb: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false,
            initialLoadSize = 10,
        ),
        pagingSourceFactory = { daoEvents.getPagingSource() },
        remoteMediator = EventRemoteMediator(
            service = apiService,
            eventDao = daoEvents,
            eventRemoteKeyDao = eventRemoteKeyDao,
            db = appDb,
        )
    ).flow
        .map {
            it.map(EventEntity::toDto)
        }

    override suspend fun getEvents() {
        try {
            val response = apiService.getEvents()

            if (!response.isSuccessful) {
                handleApiError(response)
            }

            val events = response.body() ?: throw ApiError(response.code(), response.message())
            val _events = events.map {
                if (AuthViewModel.myID == it.authorId) {
                    it.copy(eventOwner = true)
                } else it
            }
            daoEvents.insertAllEvents(
                _events.toEntity()
            )

        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while fetching events", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while fetching events", e)
            throw UnknownError
        }
    }

    override suspend fun likeEvent(id: Long, like: Boolean) {
        try {
            if (like) {
                val response = apiService.likeEventId(id)

                if (!response.isSuccessful) {
                    handleApiError(response)
                }

                val event = response.body() ?: throw ApiError(response.code(), response.message())
                daoEvents.insertEvent(EventEntity.fromDto(event))
            } else {
                dislikeEvent(id)
            }
        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while liking event", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while liking event", e)
            throw UnknownError
        }
    }

    private suspend fun dislikeEvent(id: Long) {
        try {
            val response = apiService.dislikeEventId(id)

            if (!response.isSuccessful) {
                handleApiError(response)
            }

            val event = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(EventEntity.fromDto(event))

        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while disliking event", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while disliking event", e)
            throw UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = apiService.sendEvent(event)

            if (!response.isSuccessful) {
                handleApiError(response)
            }

            val getEvent = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(EventEntity.fromDto(getEvent))

        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while saving event", e)
            throw UnknownError
        }
    }

    override suspend fun deleteEvent(event: Event) {
        try {
            daoEvents.removeEventById(event.id!!)
            val response = apiService.removeEvent(event.id)
            if (!response.isSuccessful) {
                daoEvents.insertEvent(EventEntity.fromDto(event))
                handleApiError(response)
            }

        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while deleting event", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while deleting event", e)
            throw UnknownError
        }
    }

    override suspend fun participateEvent(id: Long, status: Boolean) {
        try {
            if (status) {
                val response = apiService.participantsId(id)
                if (!response.isSuccessful) {
                    handleApiError(response)
                }
                val event = response.body() ?: throw ApiError(response.code(), response.message())
                daoEvents.insertEvent(EventEntity.fromDto(event))
            } else {
                delParticipateEvent(id)
            }

        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while participating in event", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while participating in event", e)
            throw UnknownError
        }
    }

    override suspend fun getEventsDB() {
        daoEvents.getEvents()
            .flowOn(Dispatchers.IO)
            .collect { events ->
                _eventsFlow.update { events.toDto() }
            }
    }

    private suspend fun delParticipateEvent(id: Long) {
        try {
            val response = apiService.delParticipantsId(id)

            if (!response.isSuccessful) {
                handleApiError(response)
            }

            val event = response.body() ?: throw ApiError(response.code(), response.message())
            daoEvents.insertEvent(EventEntity.fromDto(event))

        } catch (e: IOException) {
            Log.e("EventsRepositoryImpl", "Network error while deleting participation", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("EventsRepositoryImpl", "Error while deleting participation", e)
            throw UnknownError
        }
    }

    private fun handleApiError(response: Response<*>) {
        when (response.code()) {
            403 -> throw ApiError403(response.code().toString())
            404 -> throw ApiError404(response.code().toString())
            else -> throw ApiError(response.code(), response.message())
        }
    }
}
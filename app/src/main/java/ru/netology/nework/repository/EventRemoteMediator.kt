package ru.netology.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import java.lang.Exception
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator @Inject constructor(
    private val service: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val id = eventRemoteKeyDao.max()
                    if (id == null) {
                        service.getEventsLatest(state.config.initialLoadSize)
                    } else service.getEventsAfter(id, state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getEventsBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (eventRemoteKeyDao.isEmpty()) {
                            eventRemoteKeyDao.clear()
                            eventRemoteKeyDao.insert(
                                listOf(
                                    EventRemoteKeyEntity(
                                        type = EventRemoteKeyEntity.KeyType.AFTER,
                                        id = body.first().id,
                                    ),
                                    EventRemoteKeyEntity(
                                        type = EventRemoteKeyEntity.KeyType.BEFORE,
                                        id = body.last().id,
                                    ),
                                )
                            )
                        } else {
                            if (body.isNotEmpty()) {
                                eventRemoteKeyDao.insert(
                                    listOf(
                                        EventRemoteKeyEntity(
                                            type = EventRemoteKeyEntity.KeyType.AFTER,
                                            id = body.first().id,
                                        )
                                    )
                                )
                            }
                        }
                    }

                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                type = EventRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }

                    else -> {}
                }

                eventDao.insertAllEvents(body.toEntity())

            }

            return MediatorResult.Success(endOfPaginationReached = false)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }

    }

}
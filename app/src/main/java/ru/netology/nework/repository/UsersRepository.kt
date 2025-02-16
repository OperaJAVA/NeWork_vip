package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.UserResponse

interface UsersRepository {
    val allUsers: Flow<List<UserResponse>>
    val allUsersJob: Flow<List<Job>>
    suspend fun getUsers()
    suspend fun getUser(id: Long): UserResponse
    suspend fun getJobs(id: Long)
    suspend fun saveJob(job: Job)
    suspend fun deleteJob(job: Job)
}
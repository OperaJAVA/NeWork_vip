package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.ListMarkedUsers
import ru.netology.nework.dto.MarkedUser
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.StatusModelShowUserAcc
import ru.netology.nework.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class UsersViewModel @Inject constructor(
    private val usersRepo: UsersRepository
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val listUsers: LiveData<List<UserResponse>> = usersRepo.allUsers.asLiveData(Dispatchers.Default)

    private val _statusShowListJobs = MutableLiveData(StatusModelShowUserAcc())
    val statusShowListJobs: LiveData<StatusModelShowUserAcc>
        get() = _statusShowListJobs

    private val _userAccount: MutableLiveData<UserResponse> = MutableLiveData()
    val userAccount: LiveData<UserResponse>
        get() = _userAccount

    val userJobs: LiveData<List<Job>> = usersRepo.allUsersJob.asLiveData(Dispatchers.Default)

    init {
        loadUsers()
    }

    fun loadUsers() {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                usersRepo.getUsers()
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }

                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun getUserJobs(id: Long) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                usersRepo.getJobs(id)
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }

                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun getUser(id: Long) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                usersRepo.getUser(id)
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
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

    fun saveJob(job: Job) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                usersRepo.saveJob(job)
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }

                    is ru.netology.nework.error.ApiError404 -> {
                        _dataState.value = FeedModelState.NotFound // Не найдено
                    }

                    is ru.netology.nework.error.ApiError400 -> {
                        _dataState.value = FeedModelState.BadRequest // Ошибка 400
                    }

                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun removeJob(id: Long) {
        val job = userJobs.value?.find { it.id == id }
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                job?.let { usersRepo.deleteJob(it) }
                _dataState.value = FeedModelState.Success(true) // Успешное состояние
            } catch (e: Exception) {
                when (e) {
                    is ru.netology.nework.error.ApiError403 -> {
                        _dataState.value = FeedModelState.Unauthorized // Ошибка авторизации
                    }

                    is ru.netology.nework.error.ApiError404 -> {
                        _dataState.value = FeedModelState.NotFound // Не найдено
                    }

                    is ru.netology.nework.error.NetworkError -> {
                        // Обработка сетевой ошибки
                    }

                    else -> {
                        _dataState.value = FeedModelState.Error // Общая ошибка
                    }
                }
            }
        }
    }

    fun takeUser(user: UserResponse?) {
        user?.let {
            _userAccount.value = it
        }
    }

    fun selectUsers(list: List<Long>): List<UserResponse> {
        val users = mutableListOf<UserResponse>()
        listUsers.value?.let {
            for (i in listUsers.value!!) {
                if (list.contains(i.id)) users.add(i)
            }
        }
        return users
    }

    fun updateCheckableUsers(users: List<Long>) {
        ListMarkedUsers.cleanUsers()
        users.forEach { user ->
            ListMarkedUsers.addUser(MarkedUser(id = user))
        }
    }

    fun setStatusShowListJobs(status: Boolean) {
        _statusShowListJobs.value = _statusShowListJobs.value?.copy(statusShowListJobs = status)
    }
}
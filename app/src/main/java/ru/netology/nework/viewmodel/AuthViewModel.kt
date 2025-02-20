package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.media.MediaUpload
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.PostsRepository
import ru.netology.nework.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: PostsRepository,
    private val repositoryUser: UsersRepository
) : ViewModel() {

    companion object {
        @Volatile
        var userAuth: Boolean = false
        var myID: Long? = null
        const val DIALOG_OUT = 1
        const val DIALOG_IN = 2
        const val DIALOG_REG = 3
    }

    val authState: LiveData<AppAuth.AuthState>
        get() = appAuth.authState.asLiveData()

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getRegFromServer(login: String, pass: String, name: String, upload: MediaUpload) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
                val user = repository.userReg(login, pass, name, upload) // Отправляем на сервер

                user?.let {
                    if (user.id != 0L && user.token != null) {
                        appAuth.setAuth(user.id, login, pass, user.token)
                    }
                }
                _dataState.value = FeedModelState.Success(user) // Успешное состояние
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

    fun getAuthFromServer(login: String, pass: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
                val user = repository.userAuth(login, pass)

                if (user?.id != 0L && user?.token != null) {
                    val myAcc = repositoryUser.getUser(user.id)
                    appAuth.setAuth(user.id, login, pass, user.token)
                    appAuth.saveMyAcc(myAcc)
                }

                myID = user?.id
                userAuth = true
                _dataState.value = FeedModelState.AuthStatus // Статус аутентификации
            } catch (e: Exception) {
                userAuth = false
                when (e) {
                    is ru.netology.nework.error.ApiError400 -> {
                        _dataState.value = FeedModelState.BadRequest // Ошибка 400
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

    fun getMyAcc(): UserResponse = appAuth.getMyAcc()

    fun deleteAuth() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
                repository.signOut()
                appAuth.removeAuth()
                myID = null
                userAuth = false
                _dataState.value = FeedModelState.AuthStatus // Обновляем статус аутентификации
            } catch (e: Exception) {
                println(e.printStackTrace())
            }
        }
    }
}
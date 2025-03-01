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
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.media.Media
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.PostsRepository
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class PostsViewModel @Inject constructor(
    private val repository: PostsRepository,
) : ViewModel() {

    val data = repository.postsDb.map { post ->
        post.map { it.copy(postOwner = it.authorId == myID) }
    }
        .cachedIn(viewModelScope)
        .flowOn(Dispatchers.Default)

    private val _userWall = MutableLiveData<List<Post>>()
    val userWall: LiveData<List<Post>>
        get() = _userWall

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val receivedPosts: LiveData<List<Post>>
        get() = repository.postsFlow.asLiveData(Dispatchers.Default)

    init {
        getPosts()
    }

    private fun getPosts() {
        viewModelScope.launch {
            repository.getPostsDB()
        }
    }

    fun getUserPosts(id: Long) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repository.getUserPosts(id)
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

    fun savePost(post: Post, media: MultipartBody.Part?, typeAttach: AttachmentType?) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                if (typeAttach != null && media != null) {
                    val _media: Media = repository.upload(media)
                    val postWithAttachment =
                        post.copy(attachment = Attachment(_media.url, typeAttach))
                    repository.savePost(postWithAttachment)
                } else {
                    repository.savePost(post)
                }
                _dataState.value = FeedModelState.Success(true) // Успешное состояние

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

    fun like(post: Post, like: Boolean) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repository.likePost(post.id, like)
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

    fun removePost(post: Post) {
        _dataState.value = FeedModelState.Loading // Устанавливаем состояние загрузки
        viewModelScope.launch {
            try {
                repository.deletePost(post)
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

    fun takePosts(list: List<Post>?) {
        list?.let { _userWall.value = it }
    }
}
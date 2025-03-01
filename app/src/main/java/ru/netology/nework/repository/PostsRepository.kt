package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.media.Media
import ru.netology.nework.media.MediaUpload


interface PostsRepository {
    val postsDb: Flow<PagingData<Post>>
    val postsFlow: Flow<List<Post>>
    suspend fun getPosts()
    suspend fun getPostsDB()
    suspend fun getUserPosts(id: Long)
    suspend fun likePost(id: Long, like: Boolean): Post

    suspend fun userAuth(login: String, pass: String): User?
    suspend fun userReg(login: String, pass: String, name: String, upload: MediaUpload): User?

    suspend fun upload(media: MultipartBody.Part): Media
    suspend fun signOut()
    suspend fun savePost(post: Post)
    suspend fun deletePost(post: Post)
}
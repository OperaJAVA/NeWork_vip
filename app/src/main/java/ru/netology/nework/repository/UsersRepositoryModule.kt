package ru.netology.nework.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface UsersRepositoryModule {
    @Singleton
    @Binds
    fun bindsUsersRepositoryImpl(impl: UsersRepositoryImpl): UsersRepository
}
package com.techbros.myproject

sealed class UIState<out T> {
    data class Success<out T>(val data: T) : UIState<T>()
    data class Failure(val message: String) : UIState<Nothing>()
    data class Error(val exception: Throwable) : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    object Idle : UIState<Nothing>()
}

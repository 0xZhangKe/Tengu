package com.tengu.app.framework.load

sealed interface DataLoadingState {

    data object Idle : DataLoadingState

    data object Loading : DataLoadingState

    data object Success : DataLoadingState

    data class Failure(val throwable: Throwable) : DataLoadingState

    companion object {

        fun success(): DataLoadingState = Success

        fun failure(throwable: Throwable): DataLoadingState = Failure(throwable)

        fun idle(): DataLoadingState = Idle

        fun loading(): DataLoadingState = Loading
    }
}

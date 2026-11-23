package com.galindo.murocampus.ui

sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val datos: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}

fun mensajeDe(e: Throwable): String = e.localizedMessage ?: "Ocurrió un error inesperado"

package com.frommetoyou.soundforme.presentation.ui.util

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
sealed class UiText {
    data class DynamicString(val text: String) : UiText()
    data class StringResource(val resId: Int, val arguments: Array<Any>? = null) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringResource

            if (resId != other.resId) return false
            if (arguments != null) {
                if (other.arguments == null) return false
                if (!arguments.contentEquals(other.arguments)) return false
            } else if (other.arguments != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + (arguments?.contentHashCode() ?: 0)
            return result
        }
    }

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> text
            is StringResource -> if(arguments != null) {
                context.getString(resId, *arguments)
            } else {
                context.getString(resId)
            }
        }
    }
}
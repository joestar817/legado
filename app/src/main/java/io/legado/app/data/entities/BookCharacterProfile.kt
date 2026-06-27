package io.legado.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookCharacterProfiles")
data class BookCharacterProfile(
    @PrimaryKey
    val workKey: String,
    @ColumnInfo(defaultValue = "")
    var bookName: String = "",
    @ColumnInfo(defaultValue = "")
    var bookAuthor: String = "",
    var latestBookUrl: String? = null,
    @ColumnInfo(defaultValue = "0")
    var characterCount: Int = 0,
    @ColumnInfo(defaultValue = "1")
    var enabled: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    var updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun workKey(bookName: String, bookAuthor: String): String {
            return "${normalize(bookName)}\n${normalizeAuthor(bookAuthor)}"
        }

        private fun normalize(value: String): String {
            return value.trim().replace(Regex("\\s+"), " ")
        }

        private fun normalizeAuthor(value: String): String {
            return normalize(value)
                .removePrefix("作者:")
                .removePrefix("作者：")
                .trim()
        }
    }
}

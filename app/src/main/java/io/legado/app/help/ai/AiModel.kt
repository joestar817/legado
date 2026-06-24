package io.legado.app.help.ai

import com.google.gson.annotations.SerializedName

data class AiModel(
    @SerializedName(value = "id", alternate = ["a"])
    val id: String,
    @SerializedName(value = "name", alternate = ["b"])
    val name: String = id,
    @SerializedName(value = "ownedBy", alternate = ["owned_by", "c"])
    val ownedBy: String = ""
)

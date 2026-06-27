package io.legado.app.ui.book.character

import android.content.Context
import io.legado.app.R
import io.legado.app.data.entities.BookCharacter

object BookCharacterLabels {

    val genderValues = listOf(
        BookCharacter.Gender.UNKNOWN,
        BookCharacter.Gender.MALE,
        BookCharacter.Gender.FEMALE
    )

    val roleValues = listOf(
        BookCharacter.RoleTag.UNKNOWN,
        BookCharacter.RoleTag.MALE_LEAD,
        BookCharacter.RoleTag.FEMALE_LEAD,
        BookCharacter.RoleTag.MALE_SUPPORT,
        BookCharacter.RoleTag.FEMALE_SUPPORT,
        BookCharacter.RoleTag.PASSERBY,
        BookCharacter.RoleTag.OTHER
    )

    fun genderLabel(context: Context, value: String?): String {
        return when (value) {
            BookCharacter.Gender.MALE -> context.getString(R.string.character_gender_male)
            BookCharacter.Gender.FEMALE -> context.getString(R.string.character_gender_female)
            else -> context.getString(R.string.character_gender_unknown)
        }
    }

    fun roleLabel(context: Context, value: String?): String {
        return when (value) {
            BookCharacter.RoleTag.MALE_LEAD -> context.getString(R.string.character_role_male_lead)
            BookCharacter.RoleTag.FEMALE_LEAD -> context.getString(R.string.character_role_female_lead)
            BookCharacter.RoleTag.MALE_SUPPORT -> context.getString(R.string.character_role_male_support)
            BookCharacter.RoleTag.FEMALE_SUPPORT -> context.getString(R.string.character_role_female_support)
            BookCharacter.RoleTag.PASSERBY -> context.getString(R.string.character_role_passerby)
            BookCharacter.RoleTag.OTHER -> context.getString(R.string.character_role_other)
            else -> context.getString(R.string.character_role_unknown)
        }
    }
}

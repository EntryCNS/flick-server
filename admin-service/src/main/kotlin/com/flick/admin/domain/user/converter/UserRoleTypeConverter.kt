package com.flick.admin.domain.user.converter

import com.flick.common.error.CustomException
import com.flick.domain.user.enums.UserRoleType
import com.flick.domain.user.error.UserError
import org.springframework.core.convert.converter.Converter

class UserRoleTypeConverter : Converter<String, UserRoleType> {
    override fun convert(source: String): UserRoleType {
        val roleType = UserRoleType.entries.find { it.name.equals(source, ignoreCase = true) }

        return roleType ?: throw CustomException(UserError.INVALID_ROLE_TYPE)
    }
}
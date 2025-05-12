package com.flick.admin.domain.user.converter

import com.flick.domain.user.enums.UserRoleType
import org.springframework.core.convert.converter.Converter

class UserRoleTypeConverter : Converter<String, UserRoleType> {
    override fun convert(source: String): UserRoleType {
        return UserRoleType.valueOf(source.uppercase())
    }
}
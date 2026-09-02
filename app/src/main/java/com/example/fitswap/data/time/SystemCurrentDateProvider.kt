package com.example.fitswap.data.time

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemCurrentDateProvider @Inject constructor() : CurrentDateProvider {
    override fun today(): LocalDate = LocalDate.now()
}

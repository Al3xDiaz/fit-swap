package com.example.fitswap.data.time

import java.time.LocalDate

interface CurrentDateProvider {
    fun today(): LocalDate
}

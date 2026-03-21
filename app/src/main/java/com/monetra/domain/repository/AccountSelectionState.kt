package com.monetra.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountSelectionState @Inject constructor() {
    private val _selectedAccount = MutableStateFlow<String?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    fun selectAccount(account: String) {
        _selectedAccount.value = account
    }

    fun clear() {
        _selectedAccount.value = null
    }
}

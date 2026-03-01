package com.monetra.data.repository

import com.monetra.data.local.dao.RefundableDao
import com.monetra.data.local.entity.RefundableEntity
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableType
import com.monetra.domain.repository.RefundableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.monetra.domain.repository.CloudBackupRepository
import javax.inject.Inject

class RefundableRepositoryImpl @Inject constructor(
    private val dao: RefundableDao,
    private val cloudBackupRepository: CloudBackupRepository
) : RefundableRepository {

    override fun getAllRefundables(): Flow<List<Refundable>> {
        return dao.getAllRefundables().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeRefundableById(id: Long): Flow<Refundable?> {
        return dao.getRefundableFlowById(id).map { it?.toDomain() }
    }

    override suspend fun getRefundableById(id: Long): Refundable? {
        return dao.getRefundableById(id)?.toDomain()
    }

    override suspend fun upsertRefundable(refundable: Refundable): Long {
        val id = dao.upsertRefundable(refundable.toEntity())
        cloudBackupRepository.scheduleBackup()
        return id
    }

    override suspend fun deleteRefundable(refundable: Refundable) {
        dao.deleteRefundable(refundable.toEntity())
        cloudBackupRepository.scheduleBackup()
    }

    override suspend fun updatePaidStatus(id: Long, isPaid: Boolean) {
        dao.updatePaidStatus(id, isPaid)
        cloudBackupRepository.scheduleBackup()
    }

    private fun RefundableEntity.toDomain() = Refundable(
        id = id,
        amount = amount,
        personName = personName,
        phoneNumber = phoneNumber,
        givenDate = givenDate,
        dueDate = dueDate,
        note = note,
        isPaid = isPaid,
        remindMe = remindMe,
        entryType = runCatching { RefundableType.valueOf(entryType) }.getOrDefault(RefundableType.LENT)
    )

    private fun Refundable.toEntity() = RefundableEntity(
        id = id,
        amount = amount,
        personName = personName,
        phoneNumber = phoneNumber,
        givenDate = givenDate,
        dueDate = dueDate,
        note = note,
        isPaid = isPaid,
        remindMe = remindMe,
        entryType = entryType.name
    )
}

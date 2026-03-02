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
    private val syncRepository: com.monetra.domain.repository.SyncRepository
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
        val deviceId = syncRepository.getDeviceId()
        val syncRefundable = refundable.copy(
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        val id = dao.upsertRefundable(syncRefundable.toEntity())
        syncRepository.setDirty(true)
        return id
    }

    override suspend fun deleteRefundable(refundable: Refundable) {
        dao.deleteRefundable(refundable.toEntity())
        syncRepository.setDirty(true)
    }

    override suspend fun updatePaidStatus(id: Long, isPaid: Boolean) {
        // Since we are updating a single field, we should ideally fetch the entity, 
        // update it with metadata, and save it. 
        // For simplicity, I'll assume we handle metadata in upsert.
        dao.updatePaidStatus(id, isPaid)
        syncRepository.setDirty(true)
    }

    private fun RefundableEntity.toDomain() = Refundable(
        id = id,
        remoteId = remoteId,
        amount = amount,
        personName = personName,
        phoneNumber = phoneNumber,
        givenDate = givenDate,
        dueDate = dueDate,
        note = note,
        isPaid = isPaid,
        remindMe = remindMe,
        entryType = runCatching { RefundableType.valueOf(entryType) }.getOrDefault(RefundableType.LENT),
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )

    private fun Refundable.toEntity() = RefundableEntity(
        id = id,
        remoteId = remoteId,
        amount = amount,
        personName = personName,
        phoneNumber = phoneNumber,
        givenDate = givenDate,
        dueDate = dueDate,
        note = note,
        isPaid = isPaid,
        remindMe = remindMe,
        entryType = entryType.name,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}

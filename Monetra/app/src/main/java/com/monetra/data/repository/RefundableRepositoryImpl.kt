package com.monetra.data.repository

import com.monetra.data.local.dao.RefundableDao
import com.monetra.data.local.entity.RefundableEntity
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableType
import com.monetra.domain.repository.RefundableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        
        val existing = if (refundable.id != 0L) {
            dao.getRefundableById(refundable.id)
        } else {
            dao.getRefundableByRemoteId(refundable.remoteId)
        }

        val syncRefundable = refundable.copy(
            id = existing?.id ?: refundable.id,
            remoteId = existing?.remoteId ?: refundable.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        val id = dao.upsertRefundable(syncRefundable.toEntity())
        syncRepository.clearTombstone(syncRefundable.remoteId)
        syncRepository.setDirty(true)
        return id
    }

    override suspend fun deleteRefundable(refundable: Refundable) {
        syncRepository.markDeleted(refundable.remoteId, "REFUNDABLE")
        dao.deleteRefundable(refundable.toEntity())
    }

    override suspend fun updatePaidStatus(id: Long, isPaid: Boolean) {
        dao.getRefundableById(id)?.let { entity ->
            val updated = entity.copy(
                isPaid = isPaid,
                version = entity.version + 1L,
                isSynced = false,
                updatedAt = System.currentTimeMillis(),
                deviceId = syncRepository.getDeviceId()
            )
            dao.upsertRefundable(updated)
            syncRepository.setDirty(true)
        }
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

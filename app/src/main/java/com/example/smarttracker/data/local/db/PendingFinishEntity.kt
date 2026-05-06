package com.example.smarttracker.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Локальная очередь незавершённых запросов завершения тренировки.
 *
 * Запись создаётся когда [WorkoutStartViewModel] не смог выполнить
 * POST /training/{id}/save_training из-за отсутствия сети.
 * [SaveTrainingWorker] читает таблицу при появлении сети и доставляет запросы на сервер,
 * после чего удаляет успешно отправленные строки.
 *
 * @PrimaryKey trainingId — один UUID тренировки не может быть завершён дважды.
 * OnConflictStrategy.IGNORE в DAO гарантирует идемпотентность при повторных вставках.
 */
@Entity(tableName = "pending_finishes")
data class PendingFinishEntity(
    @PrimaryKey val trainingId: String,
    /** Время завершения в формате ISO 8601 UTC — фиксируется в момент нажатия «Завершить» */
    val timeEnd: String,
    val totalDistanceMeters: Double?,
    val totalKilocalories: Double?,
    /**
     * ID типа активности. Non-null только для тренировок, начатых офлайн (localUUID).
     * [SyncGpsPointsWorker] при наличии этого поля сначала регистрирует тренировку
     * на сервере, затем переключает GPS-точки на serverUUID и загружает их.
     * null = тренировка уже зарегистрирована на сервере, регистрация не нужна.
     */
    val typeActivId: Int? = null,
    /**
     * Реальное время начала тренировки (ISO 8601 UTC). Заполняется вместе с [typeActivId]
     * для офлайн-старта — передаётся в POST /training/start чтобы бэкенд записал
     * правильный time_start вместо времени получения запроса (которое всегда позже
     * реального старта для офлайн-тренировок → time_end < time_start без этого поля).
     * null = онлайн-тренировка, бэкенд сам устанавливает time_start = now().
     */
    val timeStart: String? = null,
)

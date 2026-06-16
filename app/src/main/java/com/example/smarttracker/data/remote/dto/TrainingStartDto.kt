package com.example.smarttracker.data.remote.dto

import com.example.smarttracker.domain.model.ActiveTrainingResult
import com.google.gson.annotations.SerializedName

/**
 * DTO запроса POST /training/start.
 *
 * @param typeActivId идентификатор типа активности (из GET /training/types_activity)
 * @param timeStart фактическое время начала тренировки (ISO 8601 UTC). Передаётся только
 *   для офлайн-тренировок, у которых есть реальный timestamp старта. Если null — бэкенд
 *   использует время получения запроса (поведение по умолчанию).
 *   Поле опциональное: старые версии клиента его не передают — бэкенд ведёт себя как раньше.
 */
data class TrainingStartRequestDto(
    @SerializedName("type_activ_id")
    val typeActivId: Int,
    @SerializedName("time_start")
    val timeStart: String? = null,
)

/**
 * DTO ответа POST /training/start.
 *
 * Бэкенд создаёт запись активной тренировки и возвращает её UUID.
 * Этот UUID используется для всех последующих операций:
 * загрузка GPS-точек, завершение тренировки.
 *
 * @param activeTrainingId серверный UUID тренировки
 * @param typeActivId тип активности
 * @param timeStart время начала (ISO 8601)
 * @param message человекочитаемое сообщение ("Тренировка начата")
 * @param kilocalories начальный расход калорий (обычно 0.0 на старте)
 */
data class TrainingStartResponseDto(
    @SerializedName("active_training_id")
    val activeTrainingId: String,
    @SerializedName("type_activ_id")
    val typeActivId: Int,
    @SerializedName("time_start")
    val timeStart: String,
    val message: String,
    val kilocalories: Double = 0.0,
)

/** Маппинг DTO → domain-модель */
fun TrainingStartResponseDto.toDomain(): ActiveTrainingResult = ActiveTrainingResult(
    activeTrainingId = activeTrainingId,
    typeActivId      = typeActivId,
    timeStart        = timeStart,
    message          = message,
)

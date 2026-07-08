package com.example.smarttracker.data.location

/**
 * Детектор автопаузы: по потоку скоростей GPS-точек решает, когда пользователь
 * остановился (→ [Event.PAUSE]) и когда снова начал движение (→ [Event.RESUME]).
 *
 * Чистый Kotlin без Android-зависимостей — покрыт юнит-тестами [AutopauseDetectorTest].
 * Решения о применении событий (включена ли автопауза, не ручная ли это пауза,
 * warmup после старта) принимает [LocationTrackingService] — детектор только
 * распознаёт паттерн движения.
 *
 * Гистерезис против дребезга на границе:
 *  - пауза: скорость < [pauseSpeedMps] непрерывно ≥ [pauseAfterMs] (по timestamp
 *    точек, не по их числу — интервал GPS зависит от типа активности);
 *  - резюм: скорость ≥ [resumeSpeedMps] на [resumePointsRequired] точках подряд —
 *    одиночный GPS-скачок на месте не снимает паузу.
 *
 * Точки с неизвестной скоростью (null) игнорируются: они не подтверждают ни
 * остановку, ни движение, и не сбрасывают накопленные счётчики.
 *
 * Потокобезопасность не требуется: и GPS-колбэк, и команды сервису приходят
 * на main looper (см. комментарии в LocationTrackingService.onLocationReceived).
 */
class AutopauseDetector(
    private val pauseSpeedMps: Float = LocationConfig.AUTOPAUSE_PAUSE_SPEED_MPS,
    private val resumeSpeedMps: Float = LocationConfig.AUTOPAUSE_RESUME_SPEED_MPS,
    private val pauseAfterMs: Long = LocationConfig.AUTOPAUSE_MIN_STILL_MS,
    private val resumePointsRequired: Int = LocationConfig.AUTOPAUSE_RESUME_POINTS,
) {

    enum class Event { NONE, PAUSE, RESUME }

    /** Момент первой «стоячей» точки текущей серии. null = серии нет. */
    private var stillSinceMs: Long? = null

    /** Число последовательных «движущихся» точек во время паузы. */
    private var movingPoints = 0

    /**
     * Обрабатывает очередную GPS-точку.
     *
     * @param speedMps    скорость точки, м/с; null = неизвестна (точка игнорируется)
     * @param timestampMs время фиксации точки (epoch ms)
     * @param isRecording текущее состояние записи сервиса: true → ищем остановку,
     *                    false → ищем возобновление движения
     */
    fun onPoint(speedMps: Float?, timestampMs: Long, isRecording: Boolean): Event {
        if (speedMps == null) return Event.NONE
        return if (isRecording) {
            movingPoints = 0
            if (speedMps < pauseSpeedMps) {
                val since = stillSinceMs ?: timestampMs.also { stillSinceMs = it }
                if (timestampMs - since >= pauseAfterMs) {
                    stillSinceMs = null
                    Event.PAUSE
                } else {
                    Event.NONE
                }
            } else {
                stillSinceMs = null
                Event.NONE
            }
        } else {
            stillSinceMs = null
            if (speedMps >= resumeSpeedMps) {
                movingPoints++
                if (movingPoints >= resumePointsRequired) {
                    movingPoints = 0
                    Event.RESUME
                } else {
                    Event.NONE
                }
            } else {
                movingPoints = 0
                Event.NONE
            }
        }
    }

    /** Сброс накопленного состояния — при ручной паузе/резюме и старте сессии. */
    fun reset() {
        stillSinceMs = null
        movingPoints = 0
    }
}

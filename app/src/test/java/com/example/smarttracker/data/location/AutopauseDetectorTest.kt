package com.example.smarttracker.data.location

import com.example.smarttracker.data.location.AutopauseDetector.Event
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тесты [AutopauseDetector]: гистерезис порогов, накопление «стоячего» времени
 * по timestamp (не по числу точек), устойчивость к null-скоростям и одиночным
 * GPS-скачкам.
 *
 * Дефолтные пороги из LocationConfig: пауза < 0.5 м/с непрерывно ≥ 5 сек,
 * резюм ≥ 1.0 м/с на 2 точках подряд.
 */
class AutopauseDetectorTest {

    private val detector = AutopauseDetector()

    /** Хелпер: скармливает серию (speed, t) при фиксированном isRecording. */
    private fun feed(vararg points: Pair<Float?, Long>, isRecording: Boolean): List<Event> =
        points.map { (speed, t) -> detector.onPoint(speed, t, isRecording) }

    @Test
    fun `остановка на 5 секунд даёт PAUSE`() {
        val events = feed(
            0.2f to 0L,
            0.1f to 2_000L,
            0.3f to 4_000L,
            0.2f to 5_000L,   // 5 сек от первой стоячей точки
            isRecording = true,
        )
        assertEquals(listOf(Event.NONE, Event.NONE, Event.NONE, Event.PAUSE), events)
    }

    @Test
    fun `короткая остановка на светофоре меньше 5 сек не даёт PAUSE`() {
        val events = feed(
            0.2f to 0L,
            0.1f to 3_000L,
            2.5f to 6_000L,   // поехал до истечения 5 сек
            0.2f to 9_000L,   // новая серия — счёт заново
            0.1f to 12_000L,
            isRecording = true,
        )
        assertEquals(List(5) { Event.NONE }, events)
    }

    @Test
    fun `движение на границе порога паузы не копит остановку`() {
        // 0.7 м/с — выше порога паузы 0.5, серия не начинается
        val events = feed(
            0.7f to 0L, 0.6f to 3_000L, 0.8f to 6_000L, 0.7f to 9_000L,
            isRecording = true,
        )
        assertEquals(List(4) { Event.NONE }, events)
    }

    @Test
    fun `резюм требует двух последовательных движущихся точек`() {
        val events = feed(
            1.5f to 0L,       // 1-я движущаяся — ещё рано
            1.8f to 3_000L,   // 2-я подряд → RESUME
            isRecording = false,
        )
        assertEquals(listOf(Event.NONE, Event.RESUME), events)
    }

    @Test
    fun `одиночный GPS-скачок на паузе не даёт RESUME (гистерезис)`() {
        val events = feed(
            1.5f to 0L,       // скачок
            0.2f to 3_000L,   // снова стоим — счётчик сброшен
            1.2f to 6_000L,   // опять одна
            0.1f to 9_000L,
            isRecording = false,
        )
        assertEquals(List(4) { Event.NONE }, events)
    }

    @Test
    fun `скорость между порогами (0,5-1,0) на паузе не резюмит`() {
        // GPS-дрейф 0.7 м/с: выше порога паузы, но ниже порога резюма
        val events = feed(
            0.7f to 0L, 0.8f to 3_000L, 0.9f to 6_000L,
            isRecording = false,
        )
        assertEquals(List(3) { Event.NONE }, events)
    }

    @Test
    fun `null-скорость игнорируется и не сбрасывает серию остановки`() {
        val events = feed(
            0.2f to 0L,
            null to 2_000L,   // нет данных — пропуск
            0.1f to 5_000L,   // 5 сек от первой стоячей → PAUSE
            isRecording = true,
        )
        assertEquals(listOf(Event.NONE, Event.NONE, Event.PAUSE), events)
    }

    @Test
    fun `reset очищает накопленную серию`() {
        detector.onPoint(0.2f, 0L, true)
        detector.onPoint(0.1f, 3_000L, true)
        detector.reset()
        // После reset серия начинается заново — 5 сек ещё не накоплено
        assertEquals(Event.NONE, detector.onPoint(0.1f, 5_000L, true))
        assertEquals(Event.PAUSE, detector.onPoint(0.2f, 10_000L, true))
    }

    @Test
    fun `после PAUSE детектор сразу готов распознавать резюм`() {
        feed(0.2f to 0L, 0.1f to 5_000L, isRecording = true).also {
            assertEquals(Event.PAUSE, it.last())
        }
        // Состояние сервиса сменилось на паузу — теперь ждём движение
        val resume = feed(1.4f to 8_000L, 1.6f to 11_000L, isRecording = false)
        assertEquals(listOf(Event.NONE, Event.RESUME), resume)
    }
}

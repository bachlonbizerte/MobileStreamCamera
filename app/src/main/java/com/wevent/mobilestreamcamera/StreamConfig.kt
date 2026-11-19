package com.wevent.mobilestreamcamera

data class StreamDestination(
    val name: String,
    val protocol: Protocol,
    val url: String,
    val enabled: Boolean = true
)

enum class Protocol { RTMP, SRT }
enum class VideoCodec { H264, H265 }

data class StreamConfig(
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 30,
    val bitrateKbps: Int = 6000,
    val codec: VideoCodec = VideoCodec.H264,
    val audioEnabled: Boolean = true,
    val recordLocally: Boolean = false,
    val destinations: List<StreamDestination> = listOf(
        StreamDestination("Mobile Stream Server V5", Protocol.RTMP,
            "rtmp://192.168.0.7:1935/live/camera1")
    )
) {
    fun validate(): String? {
        if (width <= 0 || height <= 0) return "Résolution invalide"
        if (fps !in 1..120) return "FPS invalide"
        if (bitrateKbps !in 250..50000) return "Bitrate invalide"
        if (destinations.none { it.enabled }) return "Active au moins une destination"
        if (destinations.any { it.url.isBlank() }) return "Une URL de destination est vide"
        return null
    }
}

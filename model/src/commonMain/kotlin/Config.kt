package ru.altmanea.edu.ktor.model

class Config {
    companion object {
        const val serverDomain = "localhost"
        const val serverPort = 8000
        const val serverApi = "1_0"
        const val serverUrl = "http://$serverDomain:$serverPort/"
        const val serverDataUrl = "${serverUrl}api_$serverApi/"

        const val studentsPath = "students/"
        const val studentsURL = "$serverDataUrl$studentsPath"
        const val lessonsPath = "lessons/"
        const val lessonsURL = "$serverDataUrl$lessonsPath"
    }
}
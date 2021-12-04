package ru.altmanea.edu.ktor.server.face

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import kotlinx.html.head
import kotlinx.html.title

fun Route.index(){
    get("/"){
        call.respondHtml(HttpStatusCode.OK){
            head {
                title {
                    +"Ktor App Example"
                }
            }
        }
    }
}
package org.gallon.rss

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Spider1Application

fun main(args: Array<String>) {
	runApplication<Spider1Application>(*args)
}
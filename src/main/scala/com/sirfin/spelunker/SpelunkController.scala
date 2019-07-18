package com.sirfin.spelunker

import java.net.URL

import org.springframework.web.bind.annotation._

@RestController
class SpelunkController {

    var jar: Option[JarSearcher] = None

    @PostMapping(
        path=Array("/loadJar"),
        consumes = Array("application/json"),
        produces = Array("application/json"))
    def loadJar(@RequestBody jarUrl: String): Unit = {
        jar = Option(JarSearcher(new URL(jarUrl)))
    }

    @GetMapping(
        path=Array("/$input/$output"),
        produces=Array("application/json"))
    def findPath(@RequestParam input: List[String], @RequestParam output: String): List[List[String]] = {
        jar.map(_.findMethodChain(input, output).map(solutionChain => solutionChain.map(_.toString)))
            .getOrElse(List(List("No path found")))
    }
}

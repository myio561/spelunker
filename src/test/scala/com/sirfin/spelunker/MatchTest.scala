package com.sirfin.spelunker

import java.net.URL

import org.scalatest.{FeatureSpec, Matchers}

class MatchTest extends FeatureSpec with Matchers {

    feature("find chain of methods that satisfy initial input/output"){

        val targetInput: List[String] = List("java.util.Date")
        val targetOutput: String = "org.joda.time.LocalDate"

        scenario("exact match"){
            val expectedResult: List[List[MethodSignature]] = List(
                List(
                    MethodSignature("org.joda.time.LocalDate", "fromDateFields", List("java.util.Date"), "org.joda.time.LocalDate")
                )
            )

            val jar = JarSearcher(new URL("https://repo1.maven.org/maven2/joda-time/joda-time/2.10.3/joda-time-2.10.3.jar"))
            jar.findPath(targetInput, targetOutput) shouldBe expectedResult
        }

        scenario("no exact match, chain match") {

            val targetInput: List[String] = List("org.joda.time.YearMonth")
            val targetOutput: String = "java.util.Date"

            val expectedResult: List[List[MethodSignature]] = List(List(
                MethodSignature("org.joda.time.LocalDate", "toLocalDateTime", List("org.joda.time.LocalTime"), "org.joda.time.LocalDateTime"),
                MethodSignature("org.joda.time.LocalDateTime", "toDate", List.empty, "java.util.Date")
            ))

            val jar = JarSearcher(new URL("https://repo1.maven.org/maven2/joda-time/joda-time/2.10.3/joda-time-2.10.3.jar"))
            jar.findPath(targetInput, targetOutput) shouldBe expectedResult
        }

        scenario("no matches"){

        }
    }

}

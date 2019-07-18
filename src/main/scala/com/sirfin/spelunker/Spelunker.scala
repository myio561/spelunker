package com.sirfin.spelunker

        import org.springframework.boot.SpringApplication
        import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Spelunker

object Spelunker {

    def main(args: Array[String]): Unit = {
        SpringApplication.run(classOf[Spelunker], args:_*)
    }
}

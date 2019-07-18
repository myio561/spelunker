package com.sirfin.spelunker

import java.io.File
import java.net.{URL, URLClassLoader}

import org.clapper.classutil.{ClassFinder, ClassInfo}

case class JarSearcher (jarFile: File) {

    private val finder: ClassFinder = ClassFinder(Seq(jarFile))
    private val classInfoList: List[ClassInfo] = finder.getClasses().toList
    private val ucl: URLClassLoader = new URLClassLoader(Array(jarFile.toURI.toURL))


    private val classList: List[Class[_]] = classInfoList.map { info: ClassInfo =>
        ucl.loadClass(info.name)
    }

    private val methodList: List[MethodSignature] = classList.flatMap { c: Class[_] =>
        c.getMethods.map { m =>
            m.setAccessible(true)
            MethodSignature(
                c.getName,
                m.getName,
                m.getParameters.map(_.getType.getName).toList,
                m.getReturnType.getName)
        }.toList
    }

    def returnTypes: List[String] = methodList.map(_.returnType).distinct

    def findMethodChain(input: List[String], output: String): List[List[MethodSignature]] = {
        findPath(input, output).filter(chain => chain.last.returnType == output)
    }

    def findPath(
                    input: List[String],
                    output: String,
                    depth: Int=1,
                    solutions: List[List[MethodSignature]]=List.empty,
                    chain: List[MethodSignature]=List.empty): List[List[MethodSignature]] = {

        if (depth == JarSearcher.maxDepth)
            solutions
        else {
            // find all the methods matching the input
            // forall works great unless the list is empty,
            // then it always returns true
            // adding exists to the test makes an empty list fail
            val methodsMatchingParams = methodList.filter { m =>
                m.parameters.exists(input.contains) &&
                m.parameters.forall(input.contains)
            }
            if (methodsMatchingParams.isEmpty)
                solutions
            else {
                // for each method test if it matches the output
                val matches: List[MethodSignature] = methodsMatchingParams.filter(_.returnType == output)
                if (matches.nonEmpty) {
                    // we found a solution, exit
                    solutions :+ (chain ++ matches)
                } else {
                    // no method matches the output, keep looking...
                    // for each of the methods matching the input
                    methodsMatchingParams.flatMap { method =>
                        // look for a method accepting the methodsMatchingParams output as an input, that produces the target output
                        findPath(
                            // find all the methods with inputs matching the output of each initial-input-method
                            List(method.returnType),
                            output,
                            depth + 1,
                            solutions,
                            chain :+ method)
                    }
                }
            }
        }
    }

}

object JarSearcher {

    val maxDepth = 10

    def apply(url: URL): JarSearcher = {
        val jarFileName: String = s"${url.hashCode().toString}.jar"
        new JarSearcher(fileDownloader(url.toString, jarFileName))
    }

    def fileDownloader(url: String, filename: String): File = {
        import sys.process._
        val file: File = new File(filename)
        (new URL(url) #> file !!)
        file
    }

}
package com.github.plippe

import scala.annotation.tailrec

import sbt._, Keys._

object Files {
  def in(file: File): List[File] = {
    @tailrec
    def in(files: List[File], acc: List[File]): List[File] = files match {
      case Nil => acc
      case head :: tail if head.isFile => in(tail, head :: acc)
      case head :: tail if head.isDirectory => in(head.listFiles.toList ::: tail, acc)
    }

    in(List(file), Nil)
  }
}
